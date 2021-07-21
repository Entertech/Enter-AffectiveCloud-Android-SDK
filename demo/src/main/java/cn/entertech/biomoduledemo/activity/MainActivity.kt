package cn.entertech.biomoduledemo.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import androidx.core.app.ActivityCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import cn.entertech.affectivecloudsdk.*
import cn.entertech.affectivecloudsdk.entity.*
import cn.entertech.affectivecloudsdk.interfaces.*
import cn.entertech.affectivecloudsdk.sourcedataapi.AffectiveCloudSourceDataApiFactory
import cn.entertech.biomoduledemo.R
import cn.entertech.biomoduledemo.app.Constant.Companion.INTENT_APP_KEY
import cn.entertech.biomoduledemo.app.Constant.Companion.INTENT_APP_SECRET
import cn.entertech.biomoduledemo.fragment.MessageReceiveFragment
import cn.entertech.biomoduledemo.fragment.MessageSendFragment
import cn.entertech.biomoduledemo.utils.*
import cn.entertech.ble.single.BiomoduleBleManager
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_main.*
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

    /*userId:your email or phone num;自己的用户ID：邮箱或者手机号码*/
    val USER_ID: String = "1245489@qq.com"
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

    //    var websocketAddress = "wss://server.affectivecloud.cn/ws/algorithm/v1/"
    var websocketAddress = "wss://server-test.affectivecloud.cn/ws/algorithm/v2/"

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
            Service.SLEEP,
            Service.COHERENCE
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

    fun verifyAppKeyAndSecret() {
        appKey = intent.getStringExtra(INTENT_APP_KEY)
        appSecret = intent.getStringExtra(INTENT_APP_SECRET)
        if (appKey == null || appSecret == null) {
            Toast.makeText(
                this@MainActivity,
                getString(R.string.auth_page_title),
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }
    fun initEnterAffectiveCloudManager() {
        if (availableBioServices.contains(Service.EEG)) {
            biodataSubscribeParams = BiodataSubscribeParams.Builder()
                .requestEEG()
                .build()
        } else {
            biodataSubscribeParams = BiodataSubscribeParams.Builder()
                .requestHR()
                .build()
        }

        affectiveSubscribeParams = AffectiveSubscribeParams.Builder()
            .requestAttention()
            .requestRelaxation()
            .requestPressure()
            .requestPleasure()
            .requestArousal()
            .requestCoherence()
            .build()

        var storageSettings = StorageSettings.Builder()
            .allowStoreRawData(true)// Whether to allow the raw data to be saved on the server
            .build()
        var algorithmParamsEEG =
            AlgorithmParamsEEG.Builder()
                .tolerance(AlgorithmParamsEEG.Tolerance.LEVEL_2)
                .filterMode(AlgorithmParamsEEG.FilterMode.SMART)
                .powerMode(AlgorithmParamsEEG.PowerMode.DB)
                .channelPowerVerbose(false)
                .build()
        var algorithmParams = AlgorithmParams.Builder()
            .eeg(algorithmParamsEEG)
            .build()
//        var biodataTolerance = BiodataTolerance.Builder()
//            .eeg(2).build()
        var enterAffectiveCloudConfig =
            EnterAffectiveCloudConfig.Builder(appKey!!, appSecret!!, USER_ID)
                .url(websocketAddress)
                .availableBiodataServices(availableBioServices)
                .availableAffectiveServices(availableAffectiveServices)
                .biodataSubscribeParams(biodataSubscribeParams!!)
                .affectiveSubscribeParams(affectiveSubscribeParams!!)
                .storageSettings(storageSettings)
                .algorithmParams(algorithmParams)
                .uploadCycle(1)
//                .biodataTolerance(biodataTolerance)
                .build()
        enterAffectiveCloudManager = EnterAffectiveCloudManager(enterAffectiveCloudConfig)
        enterAffectiveCloudManager!!.addBiodataRealtimeListener {
            messageReceiveFragment.appendMessageToScreen(getString(R.string.main_realtime_biodata) + it.toString())
        }
        enterAffectiveCloudManager!!.addAffectiveDataRealtimeListener {
            messageReceiveFragment.appendMessageToScreen(getString(R.string.main_realtime_affective_data) + it.toString())
        }
        enterAffectiveCloudManager!!.addRawJsonRequestListener {
            messageSendFragment.appendMessageToScreen(it)
        }
        enterAffectiveCloudManager!!.addRawJsonResponseListener {

        }
        enterAffectiveCloudManager?.addWebSocketDisconnectListener {
            Log.d("######", "websocket disconnect:$it")
        }
        enterAffectiveCloudManager?.init(object : Callback {
            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen(getString(R.string.main_sdk_init_failed) + error.toString())
                if (error != null && error.code == 1004) {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            getText(R.string.auth_page_title),
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
                messageReceiveFragment.appendMessageToScreen(getString(R.string.main_sdk_init_success))
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
        val listTitles = listOf(getString(R.string.main_receive), getString(R.string.main_send))
        var adapter = MessageAdapter(
            supportFragmentManager,
            listFragment,
            listTitles
        )
        vpContainer.adapter = adapter
        pagerSlidingTabStrip.setViewPager(vpContainer)

        rb_brain.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentDataType = "brainwave"
                availableBioServices = listOf(Service.EEG)
                initEnterAffectiveCloudManager()
            }
        }

        rb_heart.setOnCheckedChangeListener { _, isChecked ->
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


    fun onConnectDevice(@Suppress("UNUSED_PARAMETER") view: View) {
        messageReceiveFragment.appendMessageToScreen(getString(R.string.main_ble_scaning))
        biomoduleBleManager.scanNearDeviceAndConnect(fun() {
            messageReceiveFragment.appendMessageToScreen(getString(R.string.main_scan_success))
            Logger.d("扫描设备成功")
        }, fun(_: Exception) {

        }, fun(mac: String) {
            messageReceiveFragment.appendMessageToScreen(getString(R.string.main_connect_to_ble_success))
            Logger.d("连接成功$mac")
            runOnUiThread {
                Toast.makeText(this@MainActivity, "设备连接成功", Toast.LENGTH_SHORT).show()
            }
        }) { msg ->
            Logger.d("连接失败")
            messageReceiveFragment.appendMessageToScreen(getString(R.string.main_ble_connect_failed) + msg)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "设备连接失败：$msg", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onDisconnectDevice(@Suppress("UNUSED_PARAMETER") view: View) {
        messageReceiveFragment.appendMessageToScreen(getString(R.string.main_ble_connect_failed))
        biomoduleBleManager.disConnect()
    }

    fun onClear(@Suppress("UNUSED_PARAMETER") view: View) {
        messageSendFragment.clearScreen()
        messageReceiveFragment.clearScreen()
    }

    fun onPause(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.stopHeartAndBrainCollection()
    }

    var brainDataBuffer = ArrayList<Int>()
    var writeFileDataBuffer = ArrayList<Int>()
    var rawListener = fun(bytes: ByteArray) {
        if (currentDataType == "brain") {
            enterAffectiveCloudManager?.appendEEGData(bytes)
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
            FileHelper.getInstance().writeHr("$heartRate,")
            heartRateDataBuffer.add(heartRate)
            enterAffectiveCloudManager?.appendHeartRateData(heartRate)
        }
    }

    fun onInit(@Suppress("UNUSED_PARAMETER") view: View) {
        initEnterAffectiveCloudManager()
    }

    fun onStartUpload(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.startHeartAndBrainCollection()
        messageReceiveFragment.appendMessageToScreen(getString(R.string.main_start_uploading))
    }

    fun onReport(@Suppress("UNUSED_PARAMETER") view: View) {
        enterAffectiveCloudManager?.getBiodataReport(object : Callback2<HashMap<Any, Any?>> {
            override fun onSuccess(t: HashMap<Any, Any?>?) {
                messageReceiveFragment.appendMessageToScreen(getString(R.string.main_bio_report) + t.toString())
            }

            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen(getString(R.string.main_get_bio_report_failed) + error.toString())
            }

        })
        enterAffectiveCloudManager?.getAffectiveDataReport(object : Callback2<HashMap<Any, Any?>> {
            override fun onSuccess(t: HashMap<Any, Any?>?) {
                messageReceiveFragment.appendMessageToScreen(getString(R.string.main_get_affective_report) + t.toString())
            }

            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen(getString(R.string.mian_get_affective_report_failed) + error?.msg)
            }

        })
    }

    fun onFinish(@Suppress("UNUSED_PARAMETER") view: View) {
        enterAffectiveCloudManager?.release(object : Callback {
            override fun onSuccess() {
                messageReceiveFragment.appendMessageToScreen(getString(R.string.main_disconnected_from_cloud))
            }

            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen(getString(R.string.main_disconnected_from_cloud_failed) + error?.msg)
            }

        })
    }

    fun onSubmit(@Suppress("UNUSED_PARAMETER") view: View) {
        var datas = ArrayList<RecData>()
        var tagMap = HashMap<String, Float>()
        tagMap["pleasure"] = 0.75f
        var recData = RecData()
        recData.st = 0.1f
        recData.et = 180f
        recData.tag = tagMap
        recData.note = listOf("asdf", "sfdfsf")
        datas.add(recData)
        enterAffectiveCloudManager?.submit(
            datas,
            object : Callback {
                override fun onSuccess() {
                    messageReceiveFragment.appendMessageToScreen(getString(R.string.main_submit_comment_success))
                }

                override fun onError(error: Error?) {
                    messageReceiveFragment.appendMessageToScreen(getString(R.string.main_submit_comment_failed) + error?.msg)
                }

            })
    }

    fun onRestore(@Suppress("UNUSED_PARAMETER") view: View) {
        enterAffectiveCloudManager?.restore(object : Callback {
            override fun onSuccess() {
                messageReceiveFragment.appendMessageToScreen(getString(R.string.mian_cloud_restore_success))
            }

            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen(getString(R.string.main_cloud_restore_failed) + error?.msg)
            }

        })
    }

    fun toApiDetail(@Suppress("UNUSED_PARAMETER") view: View) {
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
