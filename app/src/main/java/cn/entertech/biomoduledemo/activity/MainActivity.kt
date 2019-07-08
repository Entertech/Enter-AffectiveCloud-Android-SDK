package cn.entertech.biomoduledemo.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import cn.entertech.biomoduledemo.R
import cn.entertech.biomoduledemo.entity.RequestBody
import cn.entertech.biomoduledemo.entity.ResponseBody
import cn.entertech.biomoduledemo.fragment.MessageReceiveFragment
import cn.entertech.biomoduledemo.fragment.MessageSendFragment
import cn.entertech.biomoduledemo.utils.*
import cn.entertech.biomoduledemo.websocket.WebSocketManager
import cn.entertech.ble.BiomoduleBleManager
import com.google.gson.Gson
import com.orhanobut.logger.Logger
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var socketManager: WebSocketManager
    private lateinit var biomoduleBleManager: BiomoduleBleManager

    /*需要向情感云平台申请 :APP_KEY、APP_SECRET、USER_NAME*/
    val APP_KEY: String = "6eabf68e-760e-11e9-bd82-0242ac140006"
    val APP_SECRET: String = "68a09cf8e4e06718b037c399f040fb7e"
    val USER_NAME: String = "test1"
    /*自己的用户ID：邮箱或者手机号码*/
    val USER_ID: String = "123456789@qq.com"

    /*情感云平台服务分类：session（会话）、biodata（基础数据服务）、affective（情感数据服务）*/
    val SERVER_SESSION = "session"
    val SERVER_BIO_DATA = "biodata"
    val SERVER_AFFECTIVE = "affective"

    /*只上传分析脑波数据*/
    val TEST_BIODATA_EEG = "eeg"
    /*只上传分析心率数据*/
    val TEST_BIODATA_HR = "hr"
    /*同时上传分析脑波和心率数据*/
    val TEST_BIODATA_BOTH = "both"
    /*需要分析的生物数据类型：目前采集到的生物数据包括脑波和心率数据*/
    var mTestBiodataType = TEST_BIODATA_BOTH

    var sessionId: String? = null
    lateinit var sign: String
    private lateinit var messageReceiveFragment: MessageReceiveFragment
    private lateinit var messageSendFragment: MessageSendFragment
    lateinit var vpContainer: ViewPager
    lateinit var pagerSlidingTabStrip: PagerSlidingTabStrip
    var saveRootPath: String = Environment.getExternalStorageDirectory().path + File.separator + "biorawdata"
    var saveEEGPath: String =
        Environment.getExternalStorageDirectory().path + File.separator + "biorawdata" + File.separator + "eeg" + File.separator
    var saveHRPath: String =
        Environment.getExternalStorageDirectory().path + File.separator + "biorawdata" + File.separator + "hr" + File.separator
    var fileName: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        biomoduleBleManager = BiomoduleBleManager.getInstance(this)
        biomoduleBleManager.addRawDataListener(rawListener)
        biomoduleBleManager.addHeartRateListener(heartRateListener)
        socketManager = WebSocketManager.getInstance()
        initPermission()
        initView()
        initSaveFiledir()
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

    var receiveDataCallback = fun(result: String?) {
        if (result == null) {
            return
        }

        var response = Gson().fromJson<ResponseBody>(result, ResponseBody::class.java)
        if (response.getSessionId() != null) {
            sessionId = response.getSessionId()
        }
        Log.d("####", "session id is " + sessionId)
        messageReceiveFragment.appendMessageToScreen(result)
        Log.d("####", "left wave is " + response.getLeftBrainwave())
//        Log.d("####", "right wave is " + response.getRightBrainwave())
//        Log.d("####", "alpha power is " + response.getEEGAlphaPower())
//        Log.d("####", "beta power is " + response.getEEGBetaPower())
//        Log.d("####", "theta power is " + response.getEEGThetaPower())
//        Log.d("####", "delta power is " + response.getEEGDeltaPower())
//        Log.d("####", "gamma power is " + response.getEEGGammaPower())
//        Log.d("####", "eeg progress is " + response.getEEGProgress())
//        Log.d("####", "hr is " + response.getHeartRate())
//        Log.d("####", "hrv is " + response.getHeartRateVariability())
//        Log.d("####", "getEEGAlphaCurve is " + response.getEEGAlphaCurve())
//        Log.d("####", "getEEGBetaCurve is " + response.getEEGBetaCurve())
//        Log.d("####", "getEEGThetaCurve is " + response.getEEGThetaCurve())
//        Log.d("####", "getEEGDeltaCurve is " + response.getEEGDeltaCurve())
//        Log.d("####", "getEEGGammaCurve is " + response.getEEGGammaCurve())
//        Log.d("####", "getHeartRateAvg is " + response.getHeartRateAvg())
//        Log.d("####", "getHeartRateMax is " + response.getHeartRateMax())
//        Log.d("####", "getHeartRateMin is " + response.getHeartRateMin())
//        Log.d("####", "getHeartRateRec is " + response.getHeartRateRec())
//        Log.d("####", "getHeartRateVariabilityRec is " + response.getHeartRateVariabilityRec())
//        Log.d("####", "getAttention is " + response.getAttention())
//        Log.d("####", "getRelaxation is " + response.getRelaxation())
//        Log.d("####", "getPressure is " + response.getPressure())
//        Log.d("####", "getAttentionAvg is " + response.getAttentionAvg())
//        Log.d("####", "getAttentionRec is " + response.getAttentionRec())
//        Log.d("####", "getRelaxationAvg is " + response.getRelaxationAvg())
//        Log.d("####", "getRelaxationRec is " + response.getRelaxationRec())
//        Log.d("####", "getPressureAvg is " + response.getPressureAvg())
//        Log.d("####", "getPressureRec is " + response.getPressureRec())
    }

    fun initView() {
        vpContainer = findViewById(R.id.vp_contain)
        socketManager.addReceiveDataListener(receiveDataCallback)
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
    }

    class MessageAdapter(fragmentManager: FragmentManager, fragments: List<Fragment>, titles: List<String>) :
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
            if (ActivityCompat.checkSelfPermission(this, needPermission[i]) != PackageManager.PERMISSION_GRANTED) {
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

    fun onConnectSocket(view: View) {
        messageReceiveFragment.appendMessageToScreen("正在连接情感云平台...")
        socketManager.connect {
            messageReceiveFragment.appendMessageToScreen("情感云平台连接成功!")
        }
    }

    fun onSessionCreate(view: View) {
        if (!isStatusOk()) {
            return
        }
        var md5Params = "app_key=$APP_KEY&app_secret=$APP_SECRET&username=$USER_NAME"
        sign = MD5Encode(md5Params).toUpperCase()
        var requestBodyMap = HashMap<Any, Any>()
        requestBodyMap["app_key"] = APP_KEY
        requestBodyMap["sign"] = sign
        requestBodyMap["user_id"] = MD5Encode(USER_ID)
        var requestBody = RequestBody(SERVER_SESSION, "create", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))
    }

    fun onSessionClose(view: View) {
        if (!isStatusOk()) {
            return
        }
        var requestBody = RequestBody(SERVER_SESSION, "close", null)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))
    }

    fun onSessionRestore(view: View) {
        if (!isStatusOk()) {
            return
        }
        if (sessionId == null) {
            Toast.makeText(this, "session id is null!", Toast.LENGTH_SHORT).show()
            return
        }
        socketManager.close()
        messageReceiveFragment.appendMessageToScreen("情感云平台已断开，正在尝试重新连接...")
        socketManager.connect {
            messageReceiveFragment.appendMessageToScreen("情感云平台重连成功！")
            var requestBodyMap = HashMap<Any, Any>()
            requestBodyMap["session_id"] = sessionId!!
            requestBodyMap["app_key"] = APP_KEY
            requestBodyMap["sign"] = sign
            requestBodyMap["user_id"] = MD5Encode(USER_ID)
            var requestBody = RequestBody(SERVER_SESSION, "restore", requestBodyMap)
            var requestJson = Gson().toJson(requestBody)
            messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
            socketManager.sendMessage(ConvertUtil.compress(requestJson))
        }
    }

    fun onInitBiodataServer(view: View) {
        if (!isStatusOk()) {
            return
        }
        var requestBodyMap = HashMap<Any, Any>()
        when (mTestBiodataType) {
            TEST_BIODATA_EEG -> {
                requestBodyMap["bio_data_type"] = listOf("eeg")
            }
            TEST_BIODATA_HR -> {
                requestBodyMap["bio_data_type"] = listOf("hr")
            }
            TEST_BIODATA_BOTH -> {
                requestBodyMap["bio_data_type"] = listOf("eeg", "hr")
            }
        }
        var requestBody = RequestBody(SERVER_BIO_DATA, "init", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))
    }


    var brainDataBuffer = ArrayList<Int>()
    var writeFileDataBuffer = ArrayList<Int>()
    var rawListener = fun(bytes: ByteArray) {
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
            if (brainDataBuffer.size >= 600) {
                var dataMap = HashMap<Any, Any>()
                dataMap["eeg"] = brainDataBuffer.toIntArray()
                var requestBody =
                    RequestBody(SERVER_BIO_DATA, "upload", dataMap)
                var requestJson = Gson().toJson(requestBody)
                messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
                socketManager.sendMessage(ConvertUtil.compress(requestJson))
                brainDataBuffer.clear()
            }
        }
    }

    var heartRateDataBuffer = ArrayList<Int>()
    var heartRateListener = fun(heartRate: Int) {
//        FileHelper.getInstance().writeHr("$heartRate,")
        heartRateDataBuffer.add(heartRate)
        if (heartRateDataBuffer.size >= 2) {
            var dataMap = HashMap<Any, Any>()

            dataMap["hr"] = heartRateDataBuffer.toIntArray()
            var requestBody =
                RequestBody(SERVER_BIO_DATA, "upload", dataMap)
            var requestJson = Gson().toJson(requestBody)
            messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
            socketManager.sendMessage(ConvertUtil.compress(requestJson))
            heartRateDataBuffer.clear()
        }
    }

    fun onUploadBiodata(view: View) {
        if (!isStatusOk()) {
            return
        }
        when (mTestBiodataType) {
            TEST_BIODATA_EEG -> {
                biomoduleBleManager.startBrainCollection()
            }
            TEST_BIODATA_HR -> {
                biomoduleBleManager.startHeartRateCollection()
            }
            TEST_BIODATA_BOTH -> {
                fileName = "${System.currentTimeMillis()}.txt"
                FileHelper.getInstance().setEEGPath(saveEEGPath + fileName)
                FileHelper.getInstance().setHRPath(saveHRPath + fileName)
                biomoduleBleManager.startHeartAndBrainCollection()
            }
        }
    }

    fun onStartContact(view: View) {
        biomoduleBleManager.startContact()
    }

    fun onStopContact(view: View) {
        biomoduleBleManager.stopContact()
    }

    fun onSubscribeBiodata(view: View) {
        if (!isStatusOk()) {
            return
        }
        var requestBodyMap = HashMap<Any, Any>()
        when (mTestBiodataType) {
            TEST_BIODATA_EEG -> {
                requestBodyMap["eeg"] = listOf(
                    "eegl_wave", "eegr_wave", "eeg_alpha_power", "eeg_beta_power",
                    "eeg_theta_power", "eeg_delta_power", "eeg_gamma_power", "eeg_progress"
                )
            }
            TEST_BIODATA_HR -> {
                requestBodyMap["hr"] = listOf("hr", "hrv")
            }
            TEST_BIODATA_BOTH -> {
                requestBodyMap["hr"] = listOf("hr", "hrv")
                requestBodyMap["eeg"] = listOf(
                    "eegl_wave", "eegr_wave", "eeg_alpha_power", "eeg_beta_power",
                    "eeg_theta_power", "eeg_delta_power", "eeg_gamma_power", "eeg_progress"
                )
            }
        }
        var requestBody =
            RequestBody(SERVER_BIO_DATA, "subscribe", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))
    }

    fun onBiodataReport(view: View) {
        if (!isStatusOk()) {
            return
        }
        var requestBodyMap = HashMap<Any, Any>()
        when (mTestBiodataType) {
            TEST_BIODATA_EEG -> {
                requestBodyMap["bio_data_type"] = listOf("eeg")
            }
            TEST_BIODATA_HR -> {
                requestBodyMap["bio_data_type"] = listOf("hr")
            }
            TEST_BIODATA_BOTH -> {
                requestBodyMap["bio_data_type"] = listOf("hr", "eeg")
            }
        }
        var requestBody =
            RequestBody(SERVER_BIO_DATA, "report", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))
    }

    fun onUnsubscribeBiodata(view: View) {
        if (!isStatusOk()) {
            return
        }
        var requestBodyMap = HashMap<Any, Any>()
        when (mTestBiodataType) {
            TEST_BIODATA_EEG -> {
                requestBodyMap["eeg"] = listOf(
                    "eegl_wave", "eegr_wave", "eeg_alpha_power", "eeg_beta_power",
                    "eeg_theta_power", "eeg_delta_power", "eeg_gamma_power", "eeg_progress"
                )
            }
            TEST_BIODATA_HR -> {
                requestBodyMap["hr"] = listOf("hr", "hrv")
            }
            TEST_BIODATA_BOTH -> {
                requestBodyMap["hr"] = listOf("hr", "hrv")
                requestBodyMap["eeg"] = listOf(
                    "eegl_wave", "eegr_wave", "eeg_alpha_power", "eeg_beta_power",
                    "eeg_theta_power", "eeg_delta_power", "eeg_gamma_power", "eeg_progress"
                )
            }
        }
        var requestBody =
            RequestBody(SERVER_BIO_DATA, "unsubscribe", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))
    }

    fun onStartAffective(view: View) {
        if (!isStatusOk()) {
            return
        }
        var requestBodyMap = HashMap<Any, Any>()
        when (mTestBiodataType) {
            TEST_BIODATA_EEG -> {
                requestBodyMap["cloud_services"] = listOf("attention")
            }
            TEST_BIODATA_HR -> {
                requestBodyMap["cloud_services"] = listOf("pressure")
            }
            TEST_BIODATA_BOTH -> {
                requestBodyMap["cloud_services"] = listOf("attention", "pressure", "arousal", "sleep")
            }
        }
        var requestBody =
            RequestBody(SERVER_AFFECTIVE, "start", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))
    }


    fun onSubscribeAffective(view: View) {
        if (!isStatusOk()) {
            return
        }
        var requestBodyMap = HashMap<Any, Any>()
        when (mTestBiodataType) {
            TEST_BIODATA_EEG -> {
                requestBodyMap["attention"] = listOf("attention")
            }
            TEST_BIODATA_HR -> {
                requestBodyMap["pressure"] = listOf("pressure")
            }
            TEST_BIODATA_BOTH -> {
                requestBodyMap["attention"] = listOf("attention")
                requestBodyMap["pressure"] = listOf("pressure")
                requestBodyMap["arousal"] = listOf("arousal")
                requestBodyMap["sleep"] = listOf("sleep_degree", "sleep_state")
            }
        }
        var requestBody =
            RequestBody(SERVER_AFFECTIVE, "subscribe", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))
    }


    fun onAffectiveReport(view: View) {
        if (!isStatusOk()) {
            return
        }
        var requestBodyMap = HashMap<Any, Any>()
        when (mTestBiodataType) {
            TEST_BIODATA_EEG -> {
                requestBodyMap["cloud_services"] = listOf("attention")
            }
            TEST_BIODATA_HR -> {
                requestBodyMap["cloud_services"] = listOf("pressure")
            }
            TEST_BIODATA_BOTH -> {
                requestBodyMap["cloud_services"] = listOf("attention", "pressure", "arousal", "sleep")
            }
        }
        var requestBody =
            RequestBody(SERVER_AFFECTIVE, "report", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))
    }


    fun onUnsubscribeAffective(view: View) {
        if (!isStatusOk()) {
            return
        }
        var requestBodyMap = HashMap<Any, Any>()
        when (mTestBiodataType) {
            TEST_BIODATA_EEG -> {
                requestBodyMap["attention"] = listOf("attention")
            }
            TEST_BIODATA_HR -> {
                requestBodyMap["pressure"] = listOf("pressure")
            }
            TEST_BIODATA_BOTH -> {
                requestBodyMap["attention"] = listOf("attention")
                requestBodyMap["pressure"] = listOf("pressure")
                requestBodyMap["arousal"] = listOf("arousal")
                requestBodyMap["sleep"] = listOf("sleep_degree", "sleep_state")
            }
        }
        var requestBody =
            RequestBody(SERVER_AFFECTIVE, "unsubscribe", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))
    }

    fun onFinishAffective(view: View) {
        if (!isStatusOk()) {
            return
        }
        var requestBodyMap = HashMap<Any, Any>()
        when (mTestBiodataType) {
            TEST_BIODATA_EEG -> {
                requestBodyMap["cloud_services"] = listOf("attention")
            }
            TEST_BIODATA_HR -> {
                requestBodyMap["cloud_services"] = listOf("pressure")
            }
            TEST_BIODATA_BOTH -> {
                requestBodyMap["cloud_services"] = listOf("pressure", "attention", "arousal", "sleep")
            }
        }
        var requestBody =
            RequestBody(SERVER_AFFECTIVE, "finish", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))
    }

    fun isStatusOk(): Boolean {
        if (!biomoduleBleManager.isConnected() || !socketManager.isOpen()) {
            Toast.makeText(this, "Ble or Socket Disconnected!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

}
