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
import cn.entertech.affective.sdk.api.IFinishAffectiveServiceListener
import cn.entertech.affective.sdk.api.IStartAffectiveServiceLister
import cn.entertech.affective.sdk.bean.AffectiveDataCategory
import cn.entertech.affective.sdk.bean.BioDataCategory
import cn.entertech.affective.sdk.bean.Error
import cn.entertech.affectivecloudsdk.*
import cn.entertech.affectivecloudsdk.entity.*
import cn.entertech.affectivecloudsdk.interfaces.*
import cn.entertech.biomoduledemo.R
import cn.entertech.biomoduledemo.app.Constant.Companion.INTENT_APP_KEY
import cn.entertech.biomoduledemo.app.Constant.Companion.INTENT_APP_SECRET
import cn.entertech.biomoduledemo.fragment.MessageReceiveFragment
import cn.entertech.biomoduledemo.fragment.MessageSendFragment
import cn.entertech.biomoduledemo.utils.*
import cn.entertech.ble.single.BiomoduleBleManager
import com.orhanobut.logger.Logger
import java.io.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    private var appSecret: String? = null
    private var appKey: String? = null
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


    var saveRootPath: String = ""
    var saveRawDataPath: String = ""
    var saveRealtimeDataPath: String = ""
    var saveReportDataPath: String = ""
    var fileName: String = ""

//    var websocketAddress = "wss://server.affectivecloud.cn/ws/algorithm/v2/"
    var websocketAddress = "wss://server-test.affectivecloud.cn/ws/algorithm/v2/"

    //    var EEG_TEST_FILE_PATH =
//        "/Users/Enter/Code/Android/Entertech/Enter-AffectiveCloud-Android-SDK/affectivecloudsdk/src/test/java/cn/entertech/affectivecloudsdk/testfiles/flowtime_eegdata.txt"
    var EEG_TEST_FILE_PATH =
        Environment.getExternalStorageDirectory().path + File.separator + "flowtime_eegdata.txt"
    var rawEEGFileHelper = FileHelper()
    var rawHRFileHelper = FileHelper()
    var realtimeEEGLeftFileHelper = FileHelper()
    var realtimeEEGRightFileHelper = FileHelper()
    var realtimeGammaFileHelper = FileHelper()
    var realtimeBetaFileHelper = FileHelper()
    var realtimeAlphaFileHelper = FileHelper()
    var realtimeThetaFileHelper = FileHelper()
    var realtimeDeltaFileHelper = FileHelper()
    var reportFileHelper = FileHelper()
    var availableAffectiveDataCategories =
        listOf(
            AffectiveDataCategory.ATTENTION,
            AffectiveDataCategory.PRESSURE,
            AffectiveDataCategory.AROUSAL,
            AffectiveDataCategory.RELAXATION,
            AffectiveDataCategory.PLEASURE,
            AffectiveDataCategory.SLEEP,
            AffectiveDataCategory.COHERENCE,
            AffectiveDataCategory.FLOW
        )
    var availableBioDataCategories = listOf(BioDataCategory.EEG, BioDataCategory.HR)
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
        biodataSubscribeParams = BiodataSubscribeParams.Builder()
            .requestEEG()
            .requestHR()
            .build()

        affectiveSubscribeParams = AffectiveSubscribeParams.Builder()
            .requestAttention()
            .requestRelaxation()
            .requestPressure()
            .requestPleasure()
            .requestArousal()
            .requestCoherence()
            .requestFlow()
            .build()

        var storageSettings = StorageSettings.Builder()
            .allowStoreRawData(true)// Whether to allow the raw data to be saved on the server
            .build()
        var algorithmParamsEEG =
            AlgorithmParamsEEG.Builder()
                .tolerance(AlgorithmParams.Tolerance.LEVEL_2)
                .filterMode(AlgorithmParams.FilterMode.SMART)
                .powerMode(AlgorithmParams.PowerMode.DB)
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
                .availableBiodataServices(availableBioDataCategories)
                .availableAffectiveServices(availableAffectiveDataCategories)
                .biodataSubscribeParams(biodataSubscribeParams!!)
                .affectiveSubscribeParams(affectiveSubscribeParams!!)
                .storageSettings(storageSettings)
                .algorithmParams(algorithmParams)
                .uploadCycle(3)
//                .biodataTolerance(biodataTolerance)
                .build()
        enterAffectiveCloudManager = EnterAffectiveCloudManager(enterAffectiveCloudConfig)
        enterAffectiveCloudManager!!.addBiodataRealtimeListener {
            if (it?.realtimeEEGData != null){
                realtimeEEGLeftFileHelper.writeData(list2String(it.realtimeEEGData!!.leftwave!!)+",")
                realtimeEEGRightFileHelper.writeData(list2String(it.realtimeEEGData!!.rightwave!!)+",")
                realtimeAlphaFileHelper.writeData("${it.realtimeEEGData!!.alphaPower!!},")
                realtimeBetaFileHelper.writeData("${it.realtimeEEGData!!.betaPower!!},")
                realtimeGammaFileHelper.writeData("${it.realtimeEEGData!!.gammaPower!!},")
                realtimeThetaFileHelper.writeData("${it.realtimeEEGData!!.thetaPower!!},")
                realtimeDeltaFileHelper.writeData("${it.realtimeEEGData!!.deltaPower!!},")
            }
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
        enterAffectiveCloudManager?.init(object :IStartAffectiveServiceLister{
            override fun startSuccess() {
                
            }

            override fun startBioFail(error: Error?) {
                
            }

            override fun startAffectionFail(error: Error?) {
                
            }

            override fun startFail(error: Error?) {
                
            }
        })
    }


    fun initSaveFiledir() {
        saveRootPath = getExternalFilesDir(fileName)?.absolutePath?:""
        saveRealtimeDataPath = saveRootPath + File.separator + "realtime" + File.separator
        saveReportDataPath = saveRootPath + File.separator + "report" + File.separator
        saveRawDataPath = saveRootPath + File.separator + "raw" + File.separator
        var file = File(saveRootPath)
        var rawDir = File(saveRawDataPath)
        var realtimeDir = File(saveRealtimeDataPath)
        var reportDir = File(saveReportDataPath)
        if (!file.exists()) {
            file.mkdirs()
        }
        if (!rawDir.exists()) {
            rawDir.mkdirs()
        }
        if (!realtimeDir.exists()) {
            realtimeDir.mkdirs()
        }
        if (!reportDir.exists()) {
            reportDir.mkdirs()
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
        enterAffectiveCloudManager?.appendEEGData(bytes)
        for (byte in bytes) {
            var brainData = ConvertUtil.converUnchart(byte)
            brainDataBuffer.add(brainData)
            writeFileDataBuffer.add((brainData))
            if (writeFileDataBuffer.size >= 20) {
                rawEEGFileHelper.writeData("${list2String(writeFileDataBuffer)},")
                writeFileDataBuffer.clear()
            }
        }
    }

    var heartRateDataBuffer = ArrayList<Int>()
    var heartRateListener = fun(heartRate: Int) {
        rawHRFileHelper.writeData("$heartRate,")
        heartRateDataBuffer.add(heartRate)
        enterAffectiveCloudManager?.appendHeartRateData(heartRate)
    }

    fun list2String(data:ArrayList<out Number>):String{
        return "${Arrays.toString(data.toArray())}".replace("[", "").replace("]", "")
    }

    fun onInit(@Suppress("UNUSED_PARAMETER") view: View) {
        initEnterAffectiveCloudManager()
    }

    fun onStartUpload(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.startHeartAndBrainCollection()
        messageReceiveFragment.appendMessageToScreen(getString(R.string.main_start_uploading))
    }

    fun onReport(@Suppress("UNUSED_PARAMETER") view: View) {
        enterAffectiveCloudManager?.getBiodataReport(object :
            cn.entertech.affective.sdk.api.Callback2<HashMap<Any, Any?>> {
            override fun onSuccess(t: HashMap<Any, Any?>?) {
                reportFileHelper.writeData(getCurrentTime() + "<--" + t.toString() + "\n")
                messageReceiveFragment.appendMessageToScreen(getString(R.string.main_bio_report) + t.toString())
            }

            override fun onError(error: cn.entertech.affective.sdk.bean.Error?) {
                messageReceiveFragment.appendMessageToScreen(getString(R.string.main_get_bio_report_failed) + error.toString())
            }

        })
        enterAffectiveCloudManager?.getAffectiveDataReport(object :
            cn.entertech.affective.sdk.api.Callback2<HashMap<Any, Any?>> {
            override fun onSuccess(t: HashMap<Any, Any?>?) {
                reportFileHelper.writeData(getCurrentTime() + "<--" + t.toString() + "\n")
                messageReceiveFragment.appendMessageToScreen(getString(R.string.main_get_affective_report) + t.toString())
            }

            override fun onError(error: cn.entertech.affective.sdk.bean.Error?) {
                messageReceiveFragment.appendMessageToScreen(getString(R.string.mian_get_affective_report_failed) + error?.msg)
            }

        })
    }

    fun onFinish(@Suppress("UNUSED_PARAMETER") view: View) {
        enterAffectiveCloudManager?.release(object :IFinishAffectiveServiceListener{
            override fun finishBioFail(error: Error?) {
                
            }

            override fun finishAffectiveFail(error: Error?) {
                
            }

            override fun finishError(error: Error?) {
                
            }

            override fun finishSuccess() {
                
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
            object : cn.entertech.affective.sdk.api.Callback {
                override fun onSuccess() {
                    messageReceiveFragment.appendMessageToScreen(getString(R.string.main_submit_comment_success))
                }

                override fun onError(error: cn.entertech.affective.sdk.bean.Error?) {
                    messageReceiveFragment.appendMessageToScreen(getString(R.string.main_submit_comment_failed) + error?.msg)
                }

            })
    }

    fun onRestore(@Suppress("UNUSED_PARAMETER") view: View) {
        enterAffectiveCloudManager?.restore(object :IStartAffectiveServiceLister{
            override fun startSuccess() {
                
            }

            override fun startBioFail(error: Error?) {
                
            }

            override fun startAffectionFail(error: Error?) {
                
            }

            override fun startFail(error: Error?) {
                
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
