package cn.entertech.biomoduledemo.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import cn.entertech.biomoduledemo.R
import cn.entertech.biomoduledemo.entity.AuthResponseBody
import cn.entertech.biomoduledemo.entity.RequestBody
import cn.entertech.biomoduledemo.fragment.MessageReceiveFragment
import cn.entertech.biomoduledemo.fragment.MessageSendFragment
import cn.entertech.biomoduledemo.utils.ConvertUtil
import cn.entertech.biomoduledemo.utils.MD5Encode
import cn.entertech.biomoduledemo.utils.PagerSlidingTabStrip
import cn.entertech.biomoduledemo.websocket.WebSocketManager
import cn.entertech.ble.FlowtimeBleManager
import com.google.gson.Gson
import com.orhanobut.logger.Logger
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var socketManager: WebSocketManager
    private lateinit var flowtimeBleManager: FlowtimeBleManager

    val APP_KEY: String = "6eabf68e-760e-11e9-bd82-0242ac140006"
    val APP_SECRET: String = "68a09cf8e4e06718b037c399f040fb7e"
    val USER_NAME: String = "test1"
    val SERVER_SESSION = "session"
    val SERVER_BIO_DATA = "biodata"
    val SERVER_AFFECTIVE = "affective"

    lateinit var sessionId: String
    lateinit var sign: String
    private lateinit var messageReceiveFragment: MessageReceiveFragment
    private lateinit var messageSendFragment: MessageSendFragment
    lateinit var vpContainer: ViewPager
    lateinit var pagerSlidingTabStrip: PagerSlidingTabStrip

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        flowtimeBleManager = FlowtimeBleManager.getInstance(this)
        flowtimeBleManager.addRawDataListener(rawListener)
        socketManager = WebSocketManager.getInstance()
        initPermission()
        initView()
    }

    var brainDataCallback = fun(result: String?) {
        if (result == null) {
            return
        }
        if (result!!.contains("session_id")) {
            var startResponse = Gson().fromJson<AuthResponseBody>(result, AuthResponseBody::class.java)
            Logger.d("start response is $startResponse")
            if (startResponse != null && startResponse.data != null) {
                sessionId = startResponse.data.session_id
            }
        }
        messageReceiveFragment.appendMessageToScreen(result)
    }

    fun initView() {
        vpContainer = findViewById(R.id.vp_contain)
        socketManager.addBrainDataListener(brainDataCallback)
        pagerSlidingTabStrip = findViewById(R.id.message_tabs)
        val listFragment = mutableListOf<Fragment>()
        messageReceiveFragment = MessageReceiveFragment()
        messageSendFragment = MessageSendFragment()
        listFragment.add(messageReceiveFragment)
        listFragment.add(messageSendFragment)
        val listTitles = listOf<String>("接受消息", "发送消息")
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
            Manifest.permission.ACCESS_COARSE_LOCATION
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
        flowtimeBleManager.scanNearDeviceAndConnect(fun() {
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
        flowtimeBleManager.disConnect()
    }

    fun onClear(view: View) {
        messageSendFragment.clearScreen()
    }

    fun onPause(view: View) {
        flowtimeBleManager.stopBrainCollection()
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
        var md5Params = "app_key=$APP_KEY&username=$USER_NAME&app_secret=$APP_SECRET"
        sign = MD5Encode(md5Params)
        var requestBodyMap = HashMap<Any, Any>()
        requestBodyMap["app_key"] = APP_KEY
        requestBodyMap["sign"] = sign
        requestBodyMap["user_id"] = MD5Encode("123456789@qq.com")
        var requestBody = RequestBody(SERVER_SESSION, "create", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))
    }

    fun onSessionClose(view: View) {
        var requestBody = RequestBody(SERVER_SESSION, "close", null)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))
    }

    fun onSessionRestore(view: View) {
        socketManager.close()
        socketManager.connect {
            var requestBodyMap = HashMap<Any, Any>()
            requestBodyMap["session_id"] = sessionId
            requestBodyMap["app_key"] = APP_KEY
            requestBodyMap["sign"] = sign
            var requestBody = RequestBody(SERVER_SESSION, "restore", requestBodyMap)
            var requestJson = Gson().toJson(requestBody)
            messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
            socketManager.sendMessage(ConvertUtil.compress(requestJson))
        }
    }

    fun onInitBiodataServer(view: View) {
        var requestBodyMap = HashMap<Any, Any>()
        requestBodyMap["bio_data_type"] = listOf("eeg")
        var requestBody = RequestBody(SERVER_BIO_DATA, "init", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))
    }


    var socketBuffer = ArrayList<Int>()
    var rawListener = fun(bytes: ByteArray) {
        Logger.d("brain data is " + Arrays.toString(bytes))
        for (byte in bytes) {
            socketBuffer.add(ConvertUtil.converUnchart(byte))
            if (socketBuffer.size >= 600) {
                var dataMap = HashMap<Any, Any>()
                dataMap["eeg"] = socketBuffer.toIntArray()
                var requestBody =
                    RequestBody(SERVER_BIO_DATA, "upload", dataMap)
                var requestJson = Gson().toJson(requestBody)
                messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
                socketManager.sendMessage(ConvertUtil.compress(requestJson))
                socketBuffer.clear()
            }
        }
    }

    fun onUploadBiodata(view: View) {
        flowtimeBleManager.startBrainCollection()
    }

    fun onSubscribeBiodata(view: View) {
        var requestBodyMap = HashMap<Any, Any>()
        requestBodyMap["eeg"] = listOf(
            "eegl_wave", "eegr_wave", "eeg_alpha_power", "eeg_beta_power",
            "eeg_theta_power", "eeg_delta_power", "eeg_gamma_power", "eeg_progress"
        )
        var requestBody =
            RequestBody(SERVER_BIO_DATA, "subscribe", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))
    }

    fun onBiodataReport(view: View) {
        var requestBodyMap = HashMap<Any, Any>()
        requestBodyMap["bio_data_type"] = listOf("eeg")
        var requestBody =
            RequestBody(SERVER_BIO_DATA, "report", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))
    }

    fun onUnsubscribeBiodata(view: View) {
        var requestBodyMap = HashMap<Any, Any>()
        requestBodyMap["eeg"] = listOf(
            "eegl_wave", "eegr_wave", "eeg_alpha_power", "eeg_beta_power",
            "eeg_theta_power", "eeg_delta_power", "eeg_gamma_power", "eeg_progress"
        )
        var requestBody =
            RequestBody(SERVER_BIO_DATA, "unsubscribe", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))
    }

    fun onStartAffective(view: View) {
        var requestBodyMap = HashMap<Any, Any>()
        requestBodyMap["cloud_services"] = listOf("attention")
        var requestBody =
            RequestBody(SERVER_AFFECTIVE, "start", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))
    }


    fun onSubscribeAffective(view: View) {
        var requestBodyMap = HashMap<Any, Any>()
        requestBodyMap["attention"] = listOf("attention")
        var requestBody =
            RequestBody(SERVER_AFFECTIVE, "subscribe", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))
    }


    fun onAffectiveReport(view: View) {
        var requestBodyMap = HashMap<Any, Any>()
        requestBodyMap["attention"] = listOf("attention")
        var requestBody =
            RequestBody(SERVER_AFFECTIVE, "report", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))
    }


    fun onUnsubscribeAffective(view: View) {
        var requestBodyMap = HashMap<Any, Any>()
        requestBodyMap["attention"] = listOf("attention")
        var requestBody =
            RequestBody(SERVER_AFFECTIVE, "unsubscribe", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))
    }

    fun onFinishAffective(view: View) {
        var requestBodyMap = HashMap<Any, Any>()
        requestBodyMap["attention"] = listOf("attention")
        var requestBody =
            RequestBody(SERVER_AFFECTIVE, "finish", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))
    }

    fun isStatusOk(): Boolean {
        if (!flowtimeBleManager.isConnected() || !socketManager.isOpen()) {
            Toast.makeText(this, "Ble or Socket Disconnected!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

}
