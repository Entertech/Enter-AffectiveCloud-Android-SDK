package cn.entertech.biomoduledemo.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import cn.entertech.affective.sdk.api.IAffectiveDataAnalysisService
import cn.entertech.affective.sdk.api.IConnectionServiceListener
import cn.entertech.affective.sdk.api.IFinishAffectiveServiceListener
import cn.entertech.affective.sdk.api.IGetReportListener
import cn.entertech.affective.sdk.api.IStartAffectiveServiceLister
import cn.entertech.affective.sdk.bean.EnterAffectiveConfigProxy
import cn.entertech.affective.sdk.bean.Error
import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData
import cn.entertech.affective.sdk.bean.UploadReportEntity
import cn.entertech.affective.sdk.bean.type.BioDataCategory
import cn.entertech.affective.sdk.bean.type.PhysiologyDataCategory
import cn.entertech.biomoduledemo.R
import cn.entertech.biomoduledemo.affective.AffectiveServiceFinishCallback
import cn.entertech.biomoduledemo.affective.AffectiveServiceSwitchCallbacks
import cn.entertech.biomoduledemo.affective.AffectiveServiceSwitchCoordinator
import cn.entertech.biomoduledemo.affective.AffectiveServiceSwitchResult
import cn.entertech.biomoduledemo.affective.DemoAffectiveAlgorithmMode
import cn.entertech.biomoduledemo.affective.DemoCloudCredential
import cn.entertech.biomoduledemo.app.Constant.Companion.INTENT_APP_KEY
import cn.entertech.biomoduledemo.app.Constant.Companion.INTENT_APP_SECRET
import cn.entertech.biomoduledemo.app.Constant.Companion.INTENT_SELECT_CLOUD
import cn.entertech.biomoduledemo.fragment.MessageReceiveFragment
import cn.entertech.biomoduledemo.fragment.MessageSendFragment
import cn.entertech.biomoduledemo.utils.ConvertUtil
import cn.entertech.biomoduledemo.utils.PagerSlidingTabStrip
import cn.entertech.biomoduledemo.utils.SettingManager
import cn.entertech.affectivesdk.manager.EnterAffectiveLocalService
import cn.entertech.affectivecloudsdk.EnterAffectiveCloudService
import cn.entertech.ble.single.BiomoduleBleManager
import com.orhanobut.logger.Logger
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private var appSecret: String? = null
    private var appKey: String? = null
    private lateinit var biomoduleBleManager: BiomoduleBleManager
    private lateinit var messageReceiveFragment: MessageReceiveFragment
    private lateinit var messageSendFragment: MessageSendFragment
    private lateinit var vpContainer: ViewPager
    private lateinit var pagerSlidingTabStrip: PagerSlidingTabStrip
    private lateinit var algorithmModeGroup: RadioGroup
    private lateinit var affectiveSwitchCoordinator:
        AffectiveServiceSwitchCoordinator<IAffectiveDataAnalysisService>
    private var suppressAlgorithmModeChange = false

    private val affectiveService: IAffectiveDataAnalysisService
        get() = affectiveSwitchCoordinator.currentService

    //    var websocketAddress = "wss://server.affectivecloud.cn/ws/algorithm/v2/"
    var websocketAddress = "wss://server-test.affectivecloud.cn/ws/algorithm/v2/"

    companion object{
        private const val TAG="MainActivity"
    }
    private var availableAffectiveDataCategories =
        listOf(
            PhysiologyDataCategory.ATTENTION,
            PhysiologyDataCategory.PRESSURE,
            PhysiologyDataCategory.AROUSAL,
            PhysiologyDataCategory.RELAXATION,
            PhysiologyDataCategory.PLEASURE,
            PhysiologyDataCategory.SLEEP,
            PhysiologyDataCategory.COHERENCE,
            PhysiologyDataCategory.FLOW
        )
    private var availableBioDataCategories = listOf(BioDataCategory.EEG, BioDataCategory.HR)

    private val connectionListener by lazy {
        {
            val i = Log.d(TAG,"connectionListener")
        }
    }

    private val disconnectionListener by lazy {
        { errorMsg: String ->
            val i = Log.d(TAG, "disconnect:$errorMsg")
        }
    }

    private val startAffectiveServiceLister by lazy {
        object : IStartAffectiveServiceLister {
            override fun startSuccess() {
                Log.d(TAG, "startAffectiveServiceLister:startSuccess")
            }

            override fun startBioFail(error: Error?) {
                Log.d(TAG, "startAffectiveServiceLister:startBioFail $error")
            }

            override fun startAffectionFail(error: Error?) {
                Log.d(TAG, "startAffectiveServiceLister:startAffectionFail $error")
            }

            override fun startFail(error: Error?) {
                Log.d(TAG, "startAffectiveServiceLister:startFail $error")
            }
        }
    }

    private val authenticationInputStream: InputStream? by lazy {
        null
    }

    private val connectionServiceListener by lazy {
        object : IConnectionServiceListener {
            override fun connectionSuccess(sessionId: String?) {
                Log.d(TAG, "connectionSuccess: $sessionId")
                affectiveService.startAffectiveService(
                    authenticationInputStream,
                    this@MainActivity, startAffectiveServiceLister
                )
            }

            override fun connectionError(error: Error?) {
                Log.d(TAG, "connectionError: $error")
            }
        }
    }

    private val bdListener by lazy {
        { data: RealtimeBioData? ->
            Log.d(TAG, "bdListener: $data")
            messageReceiveFragment.appendMessageToScreen(getString(R.string.main_realtime_biodata) + data.toString())
        }
    }
    private val affectiveListener by lazy {
        { data: RealtimeAffectiveData? ->
            Log.d(TAG, "affectiveListener: $data")
            messageReceiveFragment.appendMessageToScreen(getString(R.string.main_realtime_affective_data) + data.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        refreshCloudCredential(intent)
        affectiveSwitchCoordinator = createAffectiveSwitchCoordinator()
        biomoduleBleManager = BiomoduleBleManager.getInstance(applicationContext)
        biomoduleBleManager.addRawDataListener(rawListener)
        biomoduleBleManager.addHeartRateListener(heartRateListener)
        initView()
        initAlgorithmModeSwitch()
        initPermission()
        handleIntentAlgorithmSelection(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        refreshCloudCredential(intent)
        handleIntentAlgorithmSelection(intent)
    }

    private fun refreshCloudCredential(intent: Intent?) {
        val setting = SettingManager.getInstance()
        appKey = intent?.getStringExtra(INTENT_APP_KEY).takeUnless { it.isNullOrBlank() }
            ?: setting.appKey
        appSecret = intent?.getStringExtra(INTENT_APP_SECRET).takeUnless { it.isNullOrBlank() }
            ?: setting.appSecret
    }

    private fun currentCloudCredential(): DemoCloudCredential {
        refreshCloudCredential(intent)
        return DemoCloudCredential(appKey = appKey, appSecret = appSecret)
    }

    private fun handleIntentAlgorithmSelection(intent: Intent?) {
        if (intent?.getBooleanExtra(INTENT_SELECT_CLOUD, false) == true) {
            requestAlgorithmMode(DemoAffectiveAlgorithmMode.Cloud)
        }
    }

    private fun createAffectiveSwitchCoordinator():
        AffectiveServiceSwitchCoordinator<IAffectiveDataAnalysisService> {
        return AffectiveServiceSwitchCoordinator(
            initialMode = DemoAffectiveAlgorithmMode.Local,
            initialService = EnterAffectiveLocalService(),
            callbacks = object : AffectiveServiceSwitchCallbacks<IAffectiveDataAnalysisService> {
                override fun createService(
                    mode: DemoAffectiveAlgorithmMode
                ): IAffectiveDataAnalysisService {
                    return when (mode) {
                        DemoAffectiveAlgorithmMode.Local -> EnterAffectiveLocalService()
                        DemoAffectiveAlgorithmMode.Cloud -> EnterAffectiveCloudService()
                    }
                }

                override fun hasStarted(service: IAffectiveDataAnalysisService): Boolean =
                    service.hasStartAffectiveService()

                override fun hasConnected(service: IAffectiveDataAnalysisService): Boolean =
                    service.hasConnectAffectiveService()

                override fun finishService(
                    service: IAffectiveDataAnalysisService,
                    callback: AffectiveServiceFinishCallback
                ) {
                    service.finishAffectiveService(object : IFinishAffectiveServiceListener {
                        override fun finishBioFail(error: Error?) {
                            callback.finishFailed(error?.toString())
                        }

                        override fun finishAffectiveFail(error: Error?) {
                            callback.finishFailed(error?.toString())
                        }

                        override fun finishError(error: Error?) {
                            callback.finishFailed(error?.toString())
                        }

                        override fun finishSuccess() {
                            callback.finishSuccess()
                        }
                    })
                }

                override fun closeService(service: IAffectiveDataAnalysisService) {
                    service.closeAffectiveServiceConnection()
                }
            },
        )
    }

    private fun initAlgorithmModeSwitch() {
        updateAlgorithmModeChecked(affectiveSwitchCoordinator.currentMode)
        algorithmModeGroup.setOnCheckedChangeListener { _, checkedId ->
            if (suppressAlgorithmModeChange) return@setOnCheckedChangeListener
            when (checkedId) {
                R.id.algorithm_mode_local -> requestAlgorithmMode(DemoAffectiveAlgorithmMode.Local)
                R.id.algorithm_mode_cloud -> requestAlgorithmMode(DemoAffectiveAlgorithmMode.Cloud)
            }
        }
    }

    private fun requestAlgorithmMode(mode: DemoAffectiveAlgorithmMode) {
        val previousMode = affectiveSwitchCoordinator.currentMode
        var resultReceived = false
        affectiveSwitchCoordinator.switchTo(
            targetMode = mode,
            cloudCredential = currentCloudCredential(),
        ) { result ->
            resultReceived = true
            runOnUiThread {
                handleAlgorithmSwitchResult(result)
            }
        }
        if (!resultReceived && previousMode != mode && affectiveSwitchCoordinator.currentMode == previousMode) {
            updateAlgorithmModeChecked(previousMode)
            Toast.makeText(
                this,
                getString(R.string.main_algorithm_switching),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    private fun handleAlgorithmSwitchResult(result: AffectiveServiceSwitchResult) {
        when (result) {
            is AffectiveServiceSwitchResult.Switched -> {
                updateAlgorithmModeChecked(result.mode)
                messageReceiveFragment.appendMessageToScreen(
                    getString(
                        R.string.main_algorithm_switched,
                        algorithmModeText(result.mode),
                    ),
                )
            }

            is AffectiveServiceSwitchResult.AlreadySelected -> {
                updateAlgorithmModeChecked(result.mode)
            }

            AffectiveServiceSwitchResult.CloudConfigRequired -> {
                updateAlgorithmModeChecked(affectiveSwitchCoordinator.currentMode)
                Toast.makeText(
                    this,
                    getString(R.string.main_cloud_config_required),
                    Toast.LENGTH_LONG,
                ).show()
                startActivity(Intent(this, AuthActivity::class.java))
            }

            is AffectiveServiceSwitchResult.FinishFailed -> {
                updateAlgorithmModeChecked(affectiveSwitchCoordinator.currentMode)
                Toast.makeText(
                    this,
                    getString(
                        R.string.main_algorithm_finish_failed,
                        result.reason ?: "",
                    ),
                    Toast.LENGTH_LONG,
                ).show()
            }

            AffectiveServiceSwitchResult.SwitchingInProgress -> {
                updateAlgorithmModeChecked(affectiveSwitchCoordinator.currentMode)
                Toast.makeText(
                    this,
                    getString(R.string.main_algorithm_switching),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    private fun updateAlgorithmModeChecked(mode: DemoAffectiveAlgorithmMode) {
        suppressAlgorithmModeChange = true
        algorithmModeGroup.check(
            when (mode) {
                DemoAffectiveAlgorithmMode.Local -> R.id.algorithm_mode_local
                DemoAffectiveAlgorithmMode.Cloud -> R.id.algorithm_mode_cloud
            },
        )
        suppressAlgorithmModeChange = false
    }

    private fun algorithmModeText(mode: DemoAffectiveAlgorithmMode): String {
        return when (mode) {
            DemoAffectiveAlgorithmMode.Local -> getString(R.string.main_algorithm_local)
            DemoAffectiveAlgorithmMode.Cloud -> getString(R.string.main_algorithm_cloud)
        }
    }

    private fun initEnterAffectiveService() {
        val credential = currentCloudCredential()
        if (
            affectiveSwitchCoordinator.currentMode == DemoAffectiveAlgorithmMode.Cloud &&
            !credential.isConfigured
        ) {
            handleAlgorithmSwitchResult(AffectiveServiceSwitchResult.CloudConfigRequired)
            return
        }
        val proxy = EnterAffectiveConfigProxy(
            availableBioDataCategories,
            availableAffectiveDataCategories,
            userId = "-1",
            appSecret = appSecret.orEmpty(),
            appKey = appKey.orEmpty()
        )

        affectiveService.addServiceConnectStatueListener(
            connectionListener,
            disconnectionListener
        )
        affectiveService.connectAffectiveServiceConnection(
            configProxy = proxy,
            listener = connectionServiceListener
        )
        affectiveService.subscribeData(bdListener, affectiveListener)
    }

    private fun initView() {
        vpContainer = findViewById(R.id.vp_contain)
        pagerSlidingTabStrip = findViewById(R.id.message_tabs)
        algorithmModeGroup = findViewById(R.id.algorithm_mode_group)
        val listFragment = mutableListOf<Fragment>()
        messageReceiveFragment = MessageReceiveFragment()
        messageSendFragment = MessageSendFragment()
        listFragment.add(messageReceiveFragment)
        listFragment.add(messageSendFragment)
        val listTitles = listOf(getString(R.string.main_receive), getString(R.string.main_send))
        val adapter = MessageAdapter(
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
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return titles[position]
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun initPermissionS() {
        val needPermission = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
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


    /**
     * Android6.0 auth
     */
    private fun initPermission() {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            initPermissionS()
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
        },{msg->
            Logger.d("连接失败")
            messageReceiveFragment.appendMessageToScreen(getString(R.string.main_ble_connect_failed) + msg)
            runOnUiThread {
                Toast.makeText(this@MainActivity, "设备连接失败：$msg", Toast.LENGTH_SHORT).show()
            }
        },0,)
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

    private var brainDataBuffer = ArrayList<Int>()
    private var writeFileDataBuffer = ArrayList<Int>()
    private var rawListener = fun(bytes: ByteArray) {
        affectiveService.appendEEGData(bytes, false)
        for (byte in bytes) {
            val brainData = ConvertUtil.converUnchart(byte)
            brainDataBuffer.add(brainData)
            writeFileDataBuffer.add((brainData))
            if (writeFileDataBuffer.size >= 20) {
                writeFileDataBuffer.clear()
            }
        }
    }

    private var heartRateListener = fun(heartRate: Int) {
        affectiveService.appendHeartRateData(heartRate)
    }


    fun onInit(@Suppress("UNUSED_PARAMETER") view: View) {
        initEnterAffectiveService()
    }

    fun onStartUpload(@Suppress("UNUSED_PARAMETER") view: View) {
        biomoduleBleManager.startHeartAndBrainCollection()
        messageReceiveFragment.appendMessageToScreen(getString(R.string.main_start_uploading))
    }

    fun onReport(@Suppress("UNUSED_PARAMETER") view: View) {
        affectiveService.getReport(object : IGetReportListener {
            override fun onError(error: Error?) {
                Log.d(TAG, "getReport: onError $error")
                messageReceiveFragment.appendMessageToScreen("getReport onError" + error.toString())
            }

            override fun onSuccess(entity: UploadReportEntity?) {
                Log.d(TAG, "getReport: onSuccess $entity")
                messageReceiveFragment.appendMessageToScreen("getReport onSuccess" + entity.toString())
            }

            override fun getBioReportError(error: Error?) {
                Log.d(TAG, "getReport: getBioReportError $error")
                messageReceiveFragment.appendMessageToScreen("getReport getBioReportError" + error.toString())
            }

            override fun getAffectiveReportError(error: Error?) {
                Log.d(TAG, "getReport: getAffectiveReportError $error")
                messageReceiveFragment.appendMessageToScreen("getReport getAffectiveReportError" + error.toString())
            }
        }, false, false)

    }

    fun onFinish(@Suppress("UNUSED_PARAMETER") view: View) {
        affectiveService.finishAffectiveService(object : IFinishAffectiveServiceListener {
            override fun finishBioFail(error: Error?) {
                Log.d(TAG, "onFinish: finishBioFail $error")
            }

            override fun finishAffectiveFail(error: Error?) {
                Log.d(TAG, "onFinish: finishAffectiveFail $error")
            }

            override fun finishError(error: Error?) {
                Log.d(TAG, "onFinish: finishError $error")
                affectiveService.closeAffectiveServiceConnection()
            }

            override fun finishSuccess() {
                Log.d(TAG, "onFinish: finishSuccess ")
                affectiveService.closeAffectiveServiceConnection()
            }
        })
    }

    fun onSubmit(@Suppress("UNUSED_PARAMETER") view: View) {
       
    }

    fun onRestore(@Suppress("UNUSED_PARAMETER") view: View) {
        affectiveService.restoreAffectiveService(startAffectiveServiceLister)
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
