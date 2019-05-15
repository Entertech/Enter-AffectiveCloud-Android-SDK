package cn.entertech.biomoduledemo.activity

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.View
import android.widget.Toast
import cn.entertech.biomoduledemo.*
import cn.entertech.biomoduledemo.app.Constant.Companion.SERVICE_PRESSURE
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
import kotlin.collections.HashMap

class DataActivity : AppCompatActivity() {
    private lateinit var flowtimeBleManager: FlowtimeBleManager
    private lateinit var socketManager: WebSocketManager
    private lateinit var messageReceiveFragment: MessageReceiveFragment
    private lateinit var messageSendFragment: MessageSendFragment
    lateinit var vpContainer: ViewPager
    lateinit var pagerSlidingTabStrip: PagerSlidingTabStrip
    lateinit var sessionId: String
    val APP_KEY: String = "6eabf68e-760e-11e9-bd82-0242ac140006"
    val APP_SECRET: String = "68a09cf8e4e06718b037c399f040fb7e"
    val USER_NAME: String = "test1"
    lateinit var service: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data)
        flowtimeBleManager = FlowtimeBleManager.getInstance(this)
        flowtimeBleManager.addHeartRateListener(heartRateListener)
        flowtimeBleManager.addRawDataListener(rawListener)
        socketManager = WebSocketManager.getInstance()
        if (!socketManager.isOpen()) {
            socketManager.connect()
        }
        service = intent.getStringExtra("service")
        initView()
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

    fun onClear(view: View) {
        messageReceiveFragment.clearScreen()
        messageSendFragment.clearScreen()
    }

    fun login(view: View) {
        if (!isStatusOk()) {
            return
        }
        var md5Params = "app_key=$APP_KEY&username=$USER_NAME&app_secret=$APP_SECRET"
        var sign = MD5Encode(md5Params)
        var requestBodyMap = HashMap<Any, Any>()
        requestBodyMap["app_key"] = APP_KEY
        requestBodyMap["sign"] = sign
        var requestBody = RequestBody("auth", "login", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))

    }

    fun start(view: View) {
        if (!isStatusOk()) {
            return
        }
        var requestBodyMap = HashMap<Any, Any>()
        requestBodyMap["device"] = "A0"
        var requestBody = RequestBody(service, "start", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))
    }

    fun process(view: View) {
        if (!isStatusOk()) {
            return
        }
        startCollection()
    }


    fun finish(view: View) {
        if (!isStatusOk()) {
            return
        }
        stopCollection()
        var requestBodyMap = HashMap<Any, Any>()
        if (service == SERVICE_PRESSURE){
            requestBodyMap["gr"] = "hr"
        }else{
            requestBodyMap["gr"] = "eeg"
        }
        var requestBody = RequestBody(service, "finish", requestBodyMap)
        var requestJson = Gson().toJson(requestBody)
        messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
        socketManager.sendMessage(ConvertUtil.compress(requestJson))
        socketManager.connect()
    }

    fun restore(view: View) {
        stopCollection()
        socketManager.close()
        socketManager.connect {
            var requestBodyMap = HashMap<Any, Any>()
            requestBodyMap["session_id"] = sessionId
            var requestBody = RequestBody(service, "restore", requestBodyMap)
            var requestJson = Gson().toJson(requestBody)
            messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
            socketManager.sendMessage(ConvertUtil.compress(requestJson))
        }
    }

    fun isStatusOk():Boolean{
        if (!flowtimeBleManager.isConnected() || !socketManager.isOpen()) {
            Toast.makeText(this, "Ble or Socket Disconnected!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun startCollection() {
        if (service == SERVICE_PRESSURE) {
            flowtimeBleManager.startHeartRateCollection()
        } else {
            flowtimeBleManager.startBrainCollection()
        }
    }

    fun stopCollection() {
        if (service == SERVICE_PRESSURE) {
            flowtimeBleManager.stopHeartRateCollection()
        } else {
            flowtimeBleManager.stopBrainCollection()
        }
    }


    var socketBuffer = ArrayList<Int>()
    var heartRateBuffer = ArrayList<Int>()

    var heartRateListener = fun(heartRate: Int) {
        Log.d("DataActivity", "heart rate is " + heartRate)
        heartRateBuffer.add(heartRate)
        if (heartRateBuffer.size >= 2) {
            var dataMap = HashMap<Any, Any>()
            dataMap["hr"] = heartRateBuffer.toIntArray()
            var requestBodyMap = HashMap<Any, Any>()
            requestBodyMap["data"] = dataMap
            var requestBody = RequestBody(service, "process", requestBodyMap)
            var requestJson = Gson().toJson(requestBody)
            messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
            socketManager.sendMessage(ConvertUtil.compress(requestJson))
            heartRateBuffer.clear()
        }
    }
    var rawListener = fun(bytes: ByteArray) {
        Logger.d("brain data is " + Arrays.toString(bytes))
        for (byte in bytes) {
            socketBuffer.add(ConvertUtil.converUnchart(byte))
            if (socketBuffer.size >= 600) {
                var dataMap = HashMap<Any, Any>()
                dataMap["eeg"] = socketBuffer.toIntArray()
                var requestBodyMap = HashMap<Any, Any>()
                requestBodyMap["data"] = dataMap
                var requestBody =
                    RequestBody(service, "process", requestBodyMap)
                var requestJson = Gson().toJson(requestBody)
                messageSendFragment.appendMessageToScreen(requestJson + "\r\n")
                socketManager.sendMessage(ConvertUtil.compress(requestJson))
                socketBuffer.clear()
            }
        }
    }

    override fun onDestroy() {
        stopCollection()
        socketManager.close()
        flowtimeBleManager.removeRawDataListener(rawListener)
        flowtimeBleManager.removeHeartRateListener(heartRateListener)
        socketManager.removeBrainDataListener(brainDataCallback)
        super.onDestroy()
    }

}
