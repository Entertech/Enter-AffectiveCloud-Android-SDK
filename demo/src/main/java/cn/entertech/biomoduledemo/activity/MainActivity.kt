package cn.entertech.biomoduledemo.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import cn.entertech.affective.sdk.api.AffectiveInput
import cn.entertech.affective.sdk.api.AffectiveServiceResult
import cn.entertech.affective.sdk.api.IAffectiveDataAnalysisService
import cn.entertech.affective.sdk.api.IFinishAffectiveServiceListener
import cn.entertech.affective.sdk.api.IStartAffectiveServiceLister
import cn.entertech.affective.sdk.api.awaitConnectAffectiveService
import cn.entertech.affective.sdk.api.awaitFinishAffectiveService
import cn.entertech.affective.sdk.api.awaitGetReport
import cn.entertech.affective.sdk.api.awaitStartAffectiveService
import cn.entertech.affective.sdk.bean.EnterAffectiveConfigProxy
import cn.entertech.affective.sdk.bean.Error
import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData
import cn.entertech.affective.sdk.bean.type.BioDataCategory
import cn.entertech.affective.sdk.bean.type.PhysiologyDataCategory
import cn.entertech.affectivecloudsdk.EnterAffectiveCloudService
import cn.entertech.affectivesdk.manager.EnterAffectiveLocalService
import cn.entertech.biomoduledemo.R
import cn.entertech.biomoduledemo.affective.AffectiveServiceFinishCallback
import cn.entertech.biomoduledemo.affective.AffectiveServiceSwitchCallbacks
import cn.entertech.biomoduledemo.affective.AffectiveServiceSwitchCoordinator
import cn.entertech.biomoduledemo.affective.AffectiveServiceSwitchResult
import cn.entertech.biomoduledemo.affective.DemoAffectiveAlgorithmMode
import cn.entertech.biomoduledemo.affective.DemoCloudCredential
import cn.entertech.biomoduledemo.affective.DemoControlState
import cn.entertech.biomoduledemo.app.Constant.Companion.INTENT_APP_KEY
import cn.entertech.biomoduledemo.app.Constant.Companion.INTENT_APP_SECRET
import cn.entertech.biomoduledemo.app.Constant.Companion.INTENT_SELECT_CLOUD
import cn.entertech.biomoduledemo.fragment.MessageReceiveFragment
import cn.entertech.biomoduledemo.fragment.MessageSendFragment
import cn.entertech.biomoduledemo.utils.ConvertUtil
import cn.entertech.biomoduledemo.utils.PagerSlidingTabStrip
import cn.entertech.biomoduledemo.utils.SettingManager
import cn.entertech.ble.single.BiomoduleBleManager
import com.orhanobut.logger.Logger
import java.io.InputStream
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var appSecret: String? = null
    private var appKey: String? = null
    private lateinit var biomoduleBleManager: BiomoduleBleManager
    private lateinit var messageReceiveFragment: MessageReceiveFragment
    private lateinit var messageSendFragment: MessageSendFragment
    private lateinit var vpContainer: ViewPager
    private lateinit var pagerSlidingTabStrip: PagerSlidingTabStrip
    private lateinit var algorithmModeGroup: RadioGroup
    private lateinit var connectBleButton: Button
    private lateinit var disconnectBleButton: Button
    private lateinit var pauseUploadButton: Button
    private lateinit var initAlgorithmButton: Button
    private lateinit var startUploadButton: Button
    private lateinit var getReportButton: Button
    private lateinit var finishAlgorithmButton: Button
    private lateinit var restoreAlgorithmButton: Button
    private lateinit var affectiveSwitchCoordinator:
        AffectiveServiceSwitchCoordinator<IAffectiveDataAnalysisService>
    private var suppressAlgorithmModeChange = false
    private val controlState = DemoControlState()

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

    private val startAffectiveServiceLister by lazy {
        object : IStartAffectiveServiceLister {
            override fun startSuccess() {
                Log.d(TAG, "startAffectiveServiceLister:startSuccess")
                controlState.onAlgorithmInitialized()
                updateControlAvailability()
            }

            override fun startBioFail(error: Error?) {
                Log.d(TAG, "startAffectiveServiceLister:startBioFail $error")
                controlState.onAlgorithmFinished()
                updateControlAvailability()
            }

            override fun startAffectionFail(error: Error?) {
                Log.d(TAG, "startAffectiveServiceLister:startAffectionFail $error")
                controlState.onAlgorithmFinished()
                updateControlAvailability()
            }

            override fun startFail(error: Error?) {
                Log.d(TAG, "startAffectiveServiceLister:startFail $error")
                controlState.onAlgorithmFinished()
                updateControlAvailability()
            }
        }
    }

    private val authenticationInputStream: InputStream? by lazy {
        null
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
        if (biomoduleBleManager.isConnected()) {
            controlState.onBleConnected()
        }
        biomoduleBleManager.addRawDataListener(rawListener)
        biomoduleBleManager.addHeartRateListener(heartRateListener)
        initView()
        initAlgorithmModeSwitch()
        updateControlAvailability()
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
        if (!controlState.controls.canSwitchAlgorithm) {
            updateAlgorithmModeChecked(affectiveSwitchCoordinator.currentMode)
            showUnavailableAction()
            return
        }
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
        controlState.onAlgorithmInitializing()
        updateControlAvailability()
        val proxy = EnterAffectiveConfigProxy(
            availableBioDataCategories,
            availableAffectiveDataCategories,
            userId = "-1",
            appSecret = appSecret.orEmpty(),
            appKey = appKey.orEmpty()
        )

        lifecycleScope.launch {
            when (val connect = affectiveService.awaitConnectAffectiveService(proxy)) {
                is AffectiveServiceResult.Success -> {
                    Log.d(TAG, "connectSuccess")
                }
                is AffectiveServiceResult.Failure -> {
                    Log.d(TAG, "connectError: ${connect.error}")
                    controlState.onAlgorithmFinished()
                    updateControlAvailability()
                    return@launch
                }
            }

            when (val start = affectiveService.awaitStartAffectiveService(
                authenticationInputStream,
                this@MainActivity,
            )) {
                is AffectiveServiceResult.Success -> {
                    Log.d(TAG, "startSuccess")
                    affectiveService.subscribeData(bdListener, affectiveListener)
                    controlState.onAlgorithmInitialized()
                }
                is AffectiveServiceResult.Failure -> {
                    Log.d(TAG, "startFail: ${start.error}")
                    controlState.onAlgorithmFinished()
                }
            }
            updateControlAvailability()
        }
    }

    private fun initView() {
        vpContainer = findViewById(R.id.vp_contain)
        pagerSlidingTabStrip = findViewById(R.id.message_tabs)
        algorithmModeGroup = findViewById(R.id.algorithm_mode_group)
        connectBleButton = findViewById(R.id.btn_connect_ble)
        disconnectBleButton = findViewById(R.id.btn_disconnect_ble)
        pauseUploadButton = findViewById(R.id.btn_pause_upload)
        initAlgorithmButton = findViewById(R.id.btn_init_algorithm)
        startUploadButton = findViewById(R.id.btn_start_upload)
        getReportButton = findViewById(R.id.btn_get_report)
        finishAlgorithmButton = findViewById(R.id.btn_finish_algorithm)
        restoreAlgorithmButton = findViewById(R.id.btn_restore_algorithm)
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

    private fun updateControlAvailability() {
        val controls = controlState.controls
        connectBleButton.isEnabled = controls.canConnectBle
        disconnectBleButton.isEnabled = controls.canDisconnectBle
        pauseUploadButton.isEnabled = controls.canPauseUpload
        initAlgorithmButton.isEnabled = controls.canInitializeAlgorithm
        startUploadButton.isEnabled = controls.canStartUpload
        getReportButton.isEnabled = controls.canGetReport
        finishAlgorithmButton.isEnabled = controls.canFinishAlgorithm
        restoreAlgorithmButton.isEnabled = controls.canRestoreAlgorithm
        setAlgorithmModeGroupEnabled(controls.canSwitchAlgorithm)
    }

    private fun setAlgorithmModeGroupEnabled(enabled: Boolean) {
        algorithmModeGroup.isEnabled = enabled
        for (index in 0 until algorithmModeGroup.childCount) {
            algorithmModeGroup.getChildAt(index).isEnabled = enabled
        }
    }

    private fun showUnavailableAction() {
        Toast.makeText(this, getString(R.string.main_action_not_available), Toast.LENGTH_SHORT).show()
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
        if (!controlState.controls.canConnectBle) {
            showUnavailableAction()
            return
        }
        controlState.onBleConnecting()
        updateControlAvailability()
        messageReceiveFragment.appendMessageToScreen(getString(R.string.main_ble_scaning))
        biomoduleBleManager.scanNearDeviceAndConnect(fun() {
            messageReceiveFragment.appendMessageToScreen(getString(R.string.main_scan_success))
            Logger.d("扫描设备成功")
        }, fun(exception: Exception) {
            controlState.onBleDisconnected()
            updateControlAvailability()
            messageReceiveFragment.appendMessageToScreen(getString(R.string.main_ble_connect_failed) + exception.message)
        }, fun(mac: String) {
            controlState.onBleConnected()
            messageReceiveFragment.appendMessageToScreen(getString(R.string.main_connect_to_ble_success))
            Logger.d("连接成功$mac")
            runOnUiThread {
                updateControlAvailability()
                Toast.makeText(this@MainActivity, "设备连接成功", Toast.LENGTH_SHORT).show()
            }
        },{msg->
            Logger.d("连接失败")
            controlState.onBleDisconnected()
            messageReceiveFragment.appendMessageToScreen(getString(R.string.main_ble_connect_failed) + msg)
            runOnUiThread {
                updateControlAvailability()
                Toast.makeText(this@MainActivity, "设备连接失败：$msg", Toast.LENGTH_SHORT).show()
            }
        },0,)
    }

    fun onDisconnectDevice(@Suppress("UNUSED_PARAMETER") view: View) {
        if (!controlState.controls.canDisconnectBle) {
            showUnavailableAction()
            return
        }
        messageReceiveFragment.appendMessageToScreen(getString(R.string.main_disconnect_ble))
        biomoduleBleManager.disConnect()
        controlState.onBleDisconnected()
        updateControlAvailability()
    }

    fun onClear(@Suppress("UNUSED_PARAMETER") view: View) {
        messageSendFragment.clearScreen()
        messageReceiveFragment.clearScreen()
    }

    fun onPause(@Suppress("UNUSED_PARAMETER") view: View) {
        if (!controlState.controls.canPauseUpload) {
            showUnavailableAction()
            return
        }
        biomoduleBleManager.stopHeartAndBrainCollection()
        controlState.onUploadStopped()
        updateControlAvailability()
    }

    private var brainDataBuffer = ArrayList<Int>()
    private var writeFileDataBuffer = ArrayList<Int>()
    private var rawListener = fun(bytes: ByteArray) {
        affectiveService.append(AffectiveInput.EegBytes(bytes))
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
        affectiveService.append(AffectiveInput.HeartRate(heartRate))
    }


    fun onInit(@Suppress("UNUSED_PARAMETER") view: View) {
        if (!controlState.controls.canInitializeAlgorithm) {
            showUnavailableAction()
            return
        }
        initEnterAffectiveService()
    }

    fun onStartUpload(@Suppress("UNUSED_PARAMETER") view: View) {
        if (!controlState.controls.canStartUpload) {
            showUnavailableAction()
            return
        }
        biomoduleBleManager.startHeartAndBrainCollection()
        controlState.onUploadStarted()
        updateControlAvailability()
        messageReceiveFragment.appendMessageToScreen(getString(R.string.main_start_uploading))
    }

    fun onReport(@Suppress("UNUSED_PARAMETER") view: View) {
        if (!controlState.controls.canGetReport) {
            showUnavailableAction()
            return
        }
        lifecycleScope.launch {
            when (val report = affectiveService.awaitGetReport(isEar = false, needFinishService = false)) {
                is AffectiveServiceResult.Success -> {
                    Log.d(TAG, "getReport: onSuccess ${report.value}")
                    messageReceiveFragment.appendMessageToScreen("getReport onSuccess" + report.value.toString())
                }
                is AffectiveServiceResult.Failure -> {
                    Log.d(TAG, "getReport: onError ${report.error}")
                    messageReceiveFragment.appendMessageToScreen("getReport onError" + report.error.toString())
                }
            }
        }
    }

    fun onFinish(@Suppress("UNUSED_PARAMETER") view: View) {
        if (!controlState.controls.canFinishAlgorithm) {
            showUnavailableAction()
            return
        }
        if (controlState.controls.canPauseUpload) {
            biomoduleBleManager.stopHeartAndBrainCollection()
        }
        controlState.onAlgorithmFinishing()
        updateControlAvailability()
        lifecycleScope.launch {
            when (val finish = affectiveService.awaitFinishAffectiveService()) {
                is AffectiveServiceResult.Success -> {
                    Log.d(TAG, "onFinish: finishSuccess")
                    affectiveService.closeAffectiveServiceConnection()
                    controlState.onAlgorithmFinished()
                }
                is AffectiveServiceResult.Failure -> {
                    Log.d(TAG, "onFinish: finishError ${finish.error}")
                    controlState.onAlgorithmFinishFailed()
                }
            }
            updateControlAvailability()
        }
    }

    fun onSubmit(@Suppress("UNUSED_PARAMETER") view: View) {
       
    }

    fun onRestore(@Suppress("UNUSED_PARAMETER") view: View) {
        if (!controlState.controls.canRestoreAlgorithm) {
            showUnavailableAction()
            return
        }
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
