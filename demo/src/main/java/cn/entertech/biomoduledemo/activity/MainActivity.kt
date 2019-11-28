package cn.entertech.biomoduledemo.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import cn.entertech.affectivecloudsdk.*
import cn.entertech.affectivecloudsdk.entity.*
import cn.entertech.affectivecloudsdk.interfaces.*
import cn.entertech.affectivecloudsdk.interfaces.Observer
import cn.entertech.biomoduledemo.R
import cn.entertech.biomoduledemo.app.Constant.Companion.INTENT_APP_KEY
import cn.entertech.biomoduledemo.app.Constant.Companion.INTENT_APP_SECRET
import cn.entertech.biomoduledemo.fragment.MessageReceiveFragment
import cn.entertech.biomoduledemo.fragment.MessageSendFragment
import cn.entertech.biomoduledemo.utils.*
import cn.entertech.ble.BiomoduleBleManager
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_main.*
import org.java_websocket.handshake.ServerHandshake
import java.io.*
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    private var appSecret: String? = null
    private var appKey: String? = null
    private var currentDataType: String? = "brain"
    private var enterAffectiveCloudManager: EnterAffectiveCloudManager? = null
    private var affectiveSubscribeParams: AffectiveSubscribeParams? = null
    private var biodataSubscribeParams: BiodataSubscribeParams? = null
    private lateinit var biomoduleBleManager: BiomoduleBleManager

    /*自己的用户ID：邮箱或者手机号码*/
    val USER_ID: String = "124589@qq.com"
    private lateinit var messageReceiveFragment: MessageReceiveFragment
    private lateinit var messageSendFragment: MessageSendFragment
    lateinit var vpContainer: ViewPager
    lateinit var pagerSlidingTabStrip: PagerSlidingTabStrip
    var saveRootPath: String =
        Environment.getExternalStorageDirectory().path + File.separator + "biorawdata"
    var saveEEGPath: String =
        Environment.getExternalStorageDirectory().path + File.separator + "biorawdata" + File.separator + "eeg" + File.separator
    var saveHRPath: String =
        Environment.getExternalStorageDirectory().path + File.separator + "biorawdata" + File.separator + "hr" + File.separator
    var fileName: String = ""
    var websocketAddress = "wss://server.affectivecloud.com/ws/algorithm/v1/"
    //    var EEG_TEST_FILE_PATH =
//        "/Users/Enter/Code/Android/Entertech/Enter-AffectiveCloud-Android-SDK/affectivecloudsdk/src/test/java/cn/entertech/affectivecloudsdk/testfiles/flowtime_eegdata.txt"
    var EEG_TEST_FILE_PATH =
        Environment.getExternalStorageDirectory().path + File.separator + "flowtime_eegdata.txt"

    var availableAffectiveServices =
        listOf(
            Service.ATTENTION,
            Service.PRESSURE,
            Service.AROUSAL,
            Service.RELAXATION,
            Service.PLEASURE,
            Service.SLEEP
        )
    var availableBioServices = listOf(Service.EEG)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        verifyAppKeyAndSecret()
        biomoduleBleManager = BiomoduleBleManager.getInstance(this)
        biomoduleBleManager.addRawDataListener(rawListener)
        biomoduleBleManager.addHeartRateListener(heartRateListener)
        initView()
        initEnterAffectiveCloudManager()
        initPermission()
        initSaveFiledir()
    }

    fun verifyAppKeyAndSecret(){
        appKey = intent.getStringExtra(INTENT_APP_KEY)
        appSecret = intent.getStringExtra(INTENT_APP_SECRET)
        if (appKey == null || appSecret == null){
            Toast.makeText(
                this@MainActivity,
                "APP KEY 或 APP SECRET 有误请重新填写！",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }
    fun initEnterAffectiveCloudManager() {
        biodataSubscribeParams = BiodataSubscribeParams.Builder()
            .requestAllHrData()
            .requestAllEEGData()
            .build()

        affectiveSubscribeParams = AffectiveSubscribeParams.Builder()
            .requestAttention()
            .requestRelaxation()
            .requestPressure()
            .requestPleasure()
            .requestArousal()
            .build()
        var enterAffectiveCloudConfig =
            EnterAffectiveCloudConfig.Builder(appKey!!, appSecret!!, USER_ID)
                .url(websocketAddress)
                .availableBiodataServices(availableBioServices)
                .availableAffectiveServices(availableAffectiveServices)
                .biodataSubscribeParams(biodataSubscribeParams!!)
                .affectiveSubscribeParams(affectiveSubscribeParams!!)
                .build()
        enterAffectiveCloudManager = EnterAffectiveCloudManager(enterAffectiveCloudConfig)
        enterAffectiveCloudManager!!.addBiodataRealtimeListener {
            messageReceiveFragment.appendMessageToScreen("基础服务实时数据：${it.toString()}")
        }
        enterAffectiveCloudManager!!.addAffectiveDataRealtimeListener {
            messageReceiveFragment.appendMessageToScreen("情感服务实时数据：${it.toString()}")
        }
        enterAffectiveCloudManager!!.addRawJsonRequestListener {
            messageSendFragment.appendMessageToScreen(it)
        }
        enterAffectiveCloudManager!!.addRawJsonResponseListener {

        }
        enterAffectiveCloudManager?.init(object : Callback {
            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen("SDK初始化失败：${error.toString()}")
                if (error != null && error!!.code == 1004) {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "APP KEY 或 APP SECRET 有误请重新填写！",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }
            }

            override fun onSuccess() {
                fileName = "${System.currentTimeMillis()}.txt"
                FileHelper.getInstance().setEEGPath(saveEEGPath + fileName)
                FileHelper.getInstance().setHRPath(saveHRPath + fileName)
                messageReceiveFragment.appendMessageToScreen("SDK初始化成功，等待数据上传...")
            }

        })
    }


    fun initSaveFiledir() {
        var file = File(saveRootPath)
        var eegDir = File(saveEEGPath)
        var hrDir = File(saveHRPath)
        if (!file.exists()) {
            file.mkdirs()
        }
        if (!eegDir.exists()) {
            eegDir.mkdirs()
        }
        if (!hrDir.exists()) {
            hrDir.mkdirs()
        }
    }

    fun initView() {
        vpContainer = findViewById(R.id.vp_contain)
        pagerSlidingTabStrip = findViewById(R.id.message_tabs)
        val listFragment = mutableListOf<Fragment>()
        messageReceiveFragment = MessageReceiveFragment()
        messageSendFragment = MessageSendFragment()
        listFragment.add(messageReceiveFragment)
        listFragment.add(messageSendFragment)
        val listTitles = listOf("接受消息", "发送消息")
        var adapter = MessageAdapter(
            supportFragmentManager,
            listFragment,
            listTitles
        )
        vpContainer.adapter = adapter
        pagerSlidingTabStrip.setViewPager(vpContainer)

        rb_brain.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                currentDataType = "brain"
                availableBioServices = listOf(Service.EEG)
                initEnterAffectiveCloudManager()
            }
        }

        rb_heart.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                currentDataType = "heart"
                availableBioServices = listOf(Service.HR)
                initEnterAffectiveCloudManager()
            }
        }
    }

    class MessageAdapter(
        fragmentManager: FragmentManager,
        fragments: List<Fragment>,
        titles: List<String>
    ) :
        FragmentStatePagerAdapter(fragmentManager) {
        private var fragments: List<Fragment> = listOf()
        private var titles: List<String> = listOf()

        init {
            this.fragments = fragments
            this.titles = titles
        }

        override fun getItem(position: Int): Fragment {
            return fragments.get(position)
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return titles.get(position)
        }
    }


    /**
     * Android6.0 auth
     */
    fun initPermission() {
        val needPermission = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val needRequestPermissions = ArrayList<String>()
        for (i in needPermission.indices) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    needPermission[i]
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                needRequestPermissions.add(needPermission[i])
            }
        }
        if (needRequestPermissions.size != 0) {
            val permissions = arrayOfNulls<String>(needRequestPermissions.size)
            for (i in needRequestPermissions.indices) {
                permissions[i] = needRequestPermissions[i]
            }
            ActivityCompat.requestPermissions(this@MainActivity, permissions, 1)
        }
    }


    fun onConnectDevice(view: View) {
        messageReceiveFragment.appendMessageToScreen("正在扫描设备...")
        biomoduleBleManager.scanNearDeviceAndConnect(fun() {
            messageReceiveFragment.appendMessageToScreen("扫描成功，正在连接设备...")
            Logger.d("扫描设备成功")
        }, fun(error: Exception) {

        }, fun(mac: String) {
            messageReceiveFragment.appendMessageToScreen("设备连接成功!")
            Logger.d("连接成功$mac")
            runOnUiThread {
                Toast.makeText(this@MainActivity, "设备连接成功", Toast.LENGTH_SHORT).show()
            }
        }) { msg ->
            Logger.d("连接失败")
            messageReceiveFragment.appendMessageToScreen("设备连接失败：$msg")
            runOnUiThread {
                Toast.makeText(this@MainActivity, "设备连接失败：$msg", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onDisconnetDevice(view: View) {
        biomoduleBleManager.disConnect()
    }

    fun onClear(view: View) {
        messageSendFragment.clearScreen()
        messageReceiveFragment.clearScreen()
    }

    fun onPause(view: View) {
        biomoduleBleManager.stopHeartAndBrainCollection()
    }

    var brainDataBuffer = ArrayList<Int>()
    var writeFileDataBuffer = ArrayList<Int>()
    var rawListener = fun(bytes: ByteArray) {
        if (currentDataType == "brain") {
            enterAffectiveCloudManager?.appendEEGData(bytes)
//        以下是原始脑波文件保存逻辑，如无需该功能可忽略
            for (byte in bytes) {
                var brainData = ConvertUtil.converUnchart(byte)
                brainDataBuffer.add(brainData)
                writeFileDataBuffer.add((brainData))
                if (writeFileDataBuffer.size >= 20) {
                    var writeString = "${Arrays.toString(writeFileDataBuffer.toArray())}"
                    writeString = writeString.replace("[", "").replace("]", "")
                    FileHelper.getInstance().writeEEG(writeString + ",")
                    writeFileDataBuffer.clear()
                }
            }
        }
    }

    var heartRateDataBuffer = ArrayList<Int>()
    var heartRateListener = fun(heartRate: Int) {
        if (currentDataType == "heart") {
            Log.d("####", "心率：" + heartRate)
            FileHelper.getInstance().writeHr("$heartRate,")
            heartRateDataBuffer.add(heartRate)
            enterAffectiveCloudManager?.appendHeartRateData(heartRate)
        }
    }

    fun onStartContact(view: View) {
        biomoduleBleManager.startContact()
    }

    fun onStopContact(view: View) {
        biomoduleBleManager.stopContact()
    }


    fun onInit(view: View) {
        initEnterAffectiveCloudManager()
    }

    fun onStartUpload(view: View) {
        biomoduleBleManager.startHeartAndBrainCollection()
        messageReceiveFragment.appendMessageToScreen("开始上传数据...")
    }

    fun onReport(view: View) {
        enterAffectiveCloudManager?.getBiodataReport(object : Callback2<HashMap<Any, Any?>> {
            override fun onSuccess(t: HashMap<Any, Any?>?) {
                messageReceiveFragment.appendMessageToScreen("基础报表：${t.toString()}")
            }

            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen("基础报表出错：${error.toString()}")
            }

        })
        enterAffectiveCloudManager?.getAffectiveDataReport(object : Callback2<HashMap<Any, Any?>> {
            override fun onSuccess(t: HashMap<Any, Any?>?) {
                messageReceiveFragment.appendMessageToScreen("情感报表：${t.toString()}")
            }

            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen("情感报表出错：${error?.msg}")
            }

        })
    }

    fun onFinish(view: View) {
        enterAffectiveCloudManager?.release(object : Callback {
            override fun onSuccess() {
                messageReceiveFragment.appendMessageToScreen("情感云已成功断开！")
            }

            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen("情感云断开失败：${error?.msg}")
            }

        })
    }

    fun onRestore(view: View) {
        enterAffectiveCloudManager?.restore(object : Callback {
            override fun onSuccess() {

                messageReceiveFragment.appendMessageToScreen("情感云已重连成功，请重新上传数据")
            }

            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen("情感云重连失败：${error?.msg}")
            }

        })
    }

    fun toApiDetail(view: View) {
        startActivity(Intent(this, ApiDetailActivity::class.java))
    }

    fun isStatusOk(): Boolean {
        if (!biomoduleBleManager.isConnected()) {
            Toast.makeText(this, "Ble or Socket Disconnected!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

}
