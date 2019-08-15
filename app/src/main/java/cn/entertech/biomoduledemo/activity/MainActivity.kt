package cn.entertech.biomoduledemo.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import cn.entertech.affectivecloudsdk.AffectiveObservable
import cn.entertech.affectivecloudsdk.BiodataObservable
import cn.entertech.affectivecloudsdk.EnterAffectiveCloudApiFactory
import cn.entertech.affectivecloudsdk.entity.*
import cn.entertech.affectivecloudsdk.interfaces.*
import cn.entertech.affectivecloudsdk.interfaces.Observer
import cn.entertech.biomoduledemo.R
import cn.entertech.biomoduledemo.fragment.MessageReceiveFragment
import cn.entertech.biomoduledemo.fragment.MessageSendFragment
import cn.entertech.biomoduledemo.utils.*
import cn.entertech.ble.BiomoduleBleManager
import com.orhanobut.logger.Logger
import org.java_websocket.handshake.ServerHandshake
import java.io.File
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    private var affectiveObservable: AffectiveObservable? = null
    private var mEnterAffectiveCloudApi: BaseApi? = null
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
    var websocketAddress = "wss://server.affectivecloud.com/ws/algorithm/v0.1/"
    var availableAffectiveServices = listOf(Service.ATTENTION, Service.PRESSURE, Service.AROUSAL, Service.SLEEP)
    var availableBioServices = listOf(Service.EEG, Service.HR)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        biomoduleBleManager = BiomoduleBleManager.getInstance(this)
        biomoduleBleManager.addRawDataListener(rawListener)
        biomoduleBleManager.addHeartRateListener(heartRateListener)
        mEnterAffectiveCloudApi =
            EnterAffectiveCloudApiFactory.createApi(websocketAddress, APP_KEY, APP_SECRET, USER_NAME, USER_ID)

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

    fun initView() {
        vpContainer = findViewById(R.id.vp_contain)
//        socketManager.addReceiveDataListener(receiveDataCallback)
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
        mEnterAffectiveCloudApi?.addRawJsonRequestListener {
            messageSendFragment.appendMessageToScreen(it)
        }
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
        mEnterAffectiveCloudApi?.openWebSocket(object : WebSocketCallback {
            override fun onOpen(serverHandshake: ServerHandshake?) {
                messageReceiveFragment.appendMessageToScreen("情感云平台连接成功!")
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                messageReceiveFragment.appendMessageToScreen("情感云已断开连接：$reason")
            }

            override fun onError(e: Exception?) {
                e?.printStackTrace()
                messageReceiveFragment.appendMessageToScreen("情感云连接异常：${e.toString()}")
            }

        })
    }

    fun onSessionCreate(view: View) {
        if (!isStatusOk()) {
            return
        }
        mEnterAffectiveCloudApi?.createSession(object : Callback2<String> {
            override fun onSuccess(t: String?) {
                messageReceiveFragment.appendMessageToScreen("情感云Session已创建，session id:$t")
            }

            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen("情感云Session创建异常:${error.toString()}")
            }
        })
    }

    fun onSessionClose(view: View) {
        if (!isStatusOk()) {
            return
        }
        mEnterAffectiveCloudApi?.destroySessionAndCloseWebSocket(object : Callback {
            override fun onSuccess() {
                messageReceiveFragment.appendMessageToScreen("情感云会话已关闭")
            }

            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen("情感云会话失败：${error.toString()}")
            }
        })
    }

    fun onSessionRestore(view: View) {
        mEnterAffectiveCloudApi?.restore(object : Callback {
            override fun onSuccess() {
                messageReceiveFragment.appendMessageToScreen("情感云会话已重连")
            }

            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen("情感云会话重连失败：${error.toString()}")
            }

        })
    }

    fun onInitBiodataServer(view: View) {
        if (!isStatusOk()) {
            return
        }
        mEnterAffectiveCloudApi?.initBiodataServices(availableBioServices, object : Callback {
            override fun onSuccess() {
                messageReceiveFragment.appendMessageToScreen("情感云基础服务已初始化成功")
            }

            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen("情感云基础服务初始化失败：${error.toString()}")
            }

        })
    }

    var brainDataBuffer = ArrayList<Int>()
    var writeFileDataBuffer = ArrayList<Int>()
    var rawListener = fun(bytes: ByteArray) {
        mEnterAffectiveCloudApi?.appendBrainData(bytes)
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

    var heartRateDataBuffer = ArrayList<Int>()
    var heartRateListener = fun(heartRate: Int) {
//        FileHelper.getInstance().writeHr("$heartRate,")
        heartRateDataBuffer.add(heartRate)
        mEnterAffectiveCloudApi?.appendHeartData(heartRate)
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
        BiodataObservable.create(mEnterAffectiveCloudApi!!)
            .requestAllEEGData()
            .requestAllHrData()
            .subscribe(object : Observer<RealtimeBioData, SubBiodataFields> {
                override fun onRealtimeDataResponseSuccess(data: RealtimeBioData?) {
                    messageReceiveFragment.appendMessageToScreen("基础服务实时数据：${data.toString()}")
                }

                override fun onRealtimeDataResponseError(error: Error?) {
                    messageReceiveFragment.appendMessageToScreen("实时数据返回异常：${error.toString()}")
                }

                override fun onSubscribeSuccess(subField: SubBiodataFields?) {
                    messageReceiveFragment.appendMessageToScreen("基础服务订阅成功，当前已订阅内容：${subField.toString()}")
                }

                override fun onSubscribeError(error: Error?) {
                    messageReceiveFragment.appendMessageToScreen("基础服务订阅失败：${error.toString()}")
                }
            })
    }

    fun onBiodataReport(view: View) {
        if (!isStatusOk()) {
            return
        }
        mEnterAffectiveCloudApi?.reportBiodata(availableBioServices, object : Callback2<HashMap<Any, Any?>> {
            override fun onSuccess(t: HashMap<Any, Any?>?) {
                messageReceiveFragment.appendMessageToScreen("基础服务报表：${t.toString()}")
            }

            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen("获取基础服务报表：${error.toString()}")
            }

        })
    }

    fun onUnsubscribeBiodata(view: View) {
        if (!isStatusOk()) {
            return
        }
        BiodataObservable.create(mEnterAffectiveCloudApi!!)
            .requestAllEEGData()
            .requestAllHrData()
            .unsubscribe(object : Callback2<SubBiodataFields> {
                override fun onSuccess(t: SubBiodataFields?) {
                    messageReceiveFragment.appendMessageToScreen("基础服务取消订阅成功，当前已订阅内容：${t.toString()}")
                }

                override fun onError(error: Error?) {
                    messageReceiveFragment.appendMessageToScreen("基础服务取消订阅失败：${error.toString()}")
                }
            })
    }

    fun onStartAffective(view: View) {
        if (!isStatusOk()) {
            return
        }
        mEnterAffectiveCloudApi?.startAffectiveServices(availableAffectiveServices,
            object : Callback {
                override fun onSuccess() {
                    messageReceiveFragment.appendMessageToScreen("情感服务已开启")
                }

                override fun onError(error: Error?) {
                    messageReceiveFragment.appendMessageToScreen("情感服务开启失败：$error")
                }

            })
    }


    fun onSubscribeAffective(view: View) {
        if (!isStatusOk()) {
            return
        }
        AffectiveObservable.create(mEnterAffectiveCloudApi!!)
            .requestAllSleepData()
            .requestArousal()
            .requestAttention()
            .requestPleasure()
            .requestPressure()
            .requestRelaxation()
            .subscribe(object : Observer<RealtimeAffectiveData, SubAffectiveDataFields> {
                override fun onRealtimeDataResponseSuccess(data: RealtimeAffectiveData?) {
                    messageReceiveFragment.appendMessageToScreen("实时情感数据：${data.toString()}")
                }

                override fun onRealtimeDataResponseError(error: Error?) {
                    messageReceiveFragment.appendMessageToScreen("情感数据返回异常：${error.toString()}")
                }

                override fun onSubscribeSuccess(subField: SubAffectiveDataFields?) {
                    messageReceiveFragment.appendMessageToScreen("情感服务订阅成功，当前已订阅内容：${subField.toString()}")
                }

                override fun onSubscribeError(error: Error?) {
                    messageReceiveFragment.appendMessageToScreen("情感服务订阅失败：${error.toString()}")
                }
            })
    }


    fun onAffectiveReport(view: View) {
        if (!isStatusOk()) {
            return
        }
        mEnterAffectiveCloudApi?.reportAffective(availableAffectiveServices,
            object : Callback2<HashMap<Any, Any?>> {
                override fun onSuccess(t: HashMap<Any, Any?>?) {
                    messageReceiveFragment.appendMessageToScreen("情感报表数据：${t.toString()}")
                }

                override fun onError(error: Error?) {
                    messageReceiveFragment.appendMessageToScreen("获取情感报表数据失败：${error.toString()}")
                }

            })
    }


    fun onUnsubscribeAffective(view: View) {
        if (!isStatusOk()) {
            return
        }
        AffectiveObservable.create(mEnterAffectiveCloudApi!!)
            .requestAllSleepData()
            .requestArousal()
            .requestAttention()
            .requestPleasure()
            .requestPressure()
            .requestRelaxation()
            .unsubscribe(object : Callback2<SubAffectiveDataFields> {
                override fun onSuccess(t: SubAffectiveDataFields?) {
                    messageReceiveFragment.appendMessageToScreen("情感服务取消订阅成功，当前已订阅内容：${t.toString()}")
                }

                override fun onError(error: Error?) {
                    messageReceiveFragment.appendMessageToScreen("情感服务取消订阅失败：${error.toString()}")
                }
            })
    }

    fun onFinishAffective(view: View) {
        if (!isStatusOk()) {
            return
        }
        mEnterAffectiveCloudApi?.finishAllAffectiveServices(object : Callback {
            override fun onSuccess() {
                messageReceiveFragment.appendMessageToScreen("情感服务已结束")
            }

            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen("情感服务结束失败")
            }

        })
    }

    fun isStatusOk(): Boolean {
        if (!biomoduleBleManager.isConnected() || !mEnterAffectiveCloudApi!!.isWebSocketOpen()) {
            Toast.makeText(this, "Ble or Socket Disconnected!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

}
