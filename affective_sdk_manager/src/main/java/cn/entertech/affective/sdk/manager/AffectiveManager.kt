package cn.entertech.affective.sdk.manager

import android.content.Context
import cn.entertech.affective.sdk.api.IAffectiveDataAnalysisService
import cn.entertech.affective.sdk.api.IConnectionServiceListener
import cn.entertech.affective.sdk.api.IFinishAffectiveServiceListener
import cn.entertech.affective.sdk.api.IGetReportListener
import cn.entertech.affective.sdk.api.IStartAffectiveServiceLister
import cn.entertech.affective.sdk.bean.AffectiveServiceWay
import cn.entertech.affective.sdk.bean.EnterAffectiveConfigProxy
import cn.entertech.affective.sdk.bean.Error
import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData
import cn.entertech.affective.sdk.bean.UploadReportEntity
import cn.entertech.affective.sdk.manager.internal.AlgorithmRegistry
import java.io.InputStream
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 情感云服务管理类（泛型类）
 *
 * @param Key 设备/服务标识类型，用于区分不同设备/服务的实例
 */
@Suppress("unused")
open class AffectiveManager<Key : Any> {

    private val algorithmRegistry = AlgorithmRegistry<Key>()

    private val affectionConnectionServiceListeners by lazy {
        CopyOnWriteArrayList<IAffectionConnectionServiceListener<Key>>()
    }
    private val affectiveStartServiceLister by lazy {
        CopyOnWriteArrayList<IAffectiveStartServiceLister<Key>>()
    }
    private val bioDataListeners: MutableList<(Key, RealtimeBioData?) -> Unit> by lazy {
        CopyOnWriteArrayList()
    }

    private val affectiveDataListeners: MutableList<(Key, RealtimeAffectiveData?) -> Unit> by lazy {
        CopyOnWriteArrayList()
    }
    private val affectiveIGetReportListener: MutableList<IAffectiveIGetReportListener<Key>> by lazy {
        CopyOnWriteArrayList()
    }
    private val affectiveFinishServiceListener: MutableList<IAffectiveFinishServiceListener<Key>> by lazy {
        CopyOnWriteArrayList()
    }

    fun connectAffectiveServiceConnection(
        key: Key,
        connectionServiceListener: IConnectionServiceListener,
        configProxy: EnterAffectiveConfigProxy
    ) {
        getAffectiveService(key)?.apply {
            connectAffectiveServiceConnection(connectionServiceListener, configProxy)
        } ?: kotlin.run {
            connectionServiceListener.connectionError(Error(-1, "key service is null"))
        }
    }

    fun startAffectiveService(
        key: Key, initListener: IStartAffectiveServiceLister
    ) {
        getAffectiveService(key)?.apply {
            startAffectiveService(initListener)
        } ?: kotlin.run {
            initListener.startFail(Error(-1, "key service is null"))
        }
    }

    fun startAffectiveService(
        key: Key,
        authenticationInputStream: InputStream?,
        context: Context?,
        initListener: IStartAffectiveServiceLister
    ) {
        getAffectiveService(key)?.apply {
            startAffectiveService(authenticationInputStream, context, initListener)
        } ?: kotlin.run {
            initListener.startFail(Error(-1, "key service is null"))
        }
    }


    fun connectAndStartAffectiveService(
        key: Key, configProxy: EnterAffectiveConfigProxy = EnterAffectiveConfigProxy()
    ) {
        getAffectiveService(key)?.apply {
            if (hasConnectAffectiveService()) {
                if (hasStartAffectiveService()) {
                    affectiveStartServiceLister.forEach {
                        it.hasStarted(key)
                    }
                } else {
                    startAffectiveService(key)
                }
            } else {
                connectAffectiveServiceConnection(getConnectionServiceListener(key), configProxy)
            }
        } ?: kotlin.run {
            affectionConnectionServiceListeners.forEach {
                it.connectionError(key, Error(-1, "key $key affective service is null"))
            }
        }
    }

    fun connectAndStartAffectiveService(
        key: Key,
        authenticationInputStream: InputStream?,
        context: Context?,
        configProxy: EnterAffectiveConfigProxy = EnterAffectiveConfigProxy()
    ) {
        getAffectiveService(key)?.apply {
            if (hasConnectAffectiveService()) {
                if (hasStartAffectiveService()) {
                    affectiveStartServiceLister.forEach {
                        it.hasStarted(key)
                    }
                } else {
                    startAffectiveService(key, authenticationInputStream, context)
                }
            } else {
                connectAffectiveServiceConnection(
                    getConnectionServiceListener(
                        key, authenticationInputStream, context
                    ), configProxy
                )
            }
        } ?: kotlin.run {
            affectionConnectionServiceListeners.forEach {
                it.connectionError(key, Error(-1, "key $key affective service is null"))
            }
        }
    }


    suspend fun suspendConnectAndStartAffectiveService(key: Key) {
        suspendConnectAndStart(key, null, null, useAuthentication = false)
    }


    suspend fun suspendConnectAndStartAffectiveService(
        key: Key, authenticationInputStream: InputStream?,
        context: Context?,
    ) {
        suspendConnectAndStart(key, authenticationInputStream, context, useAuthentication = true)
    }

    private suspend fun suspendConnectAndStart(
        key: Key,
        authenticationInputStream: InputStream?,
        context: Context?,
        useAuthentication: Boolean,
    ) = suspendCancellableCoroutine { continuation ->
        val completed = AtomicBoolean(false)
        val startRequested = AtomicBoolean(false)
        val service = getAffectiveService(key)

        fun succeed() {
            if (completed.compareAndSet(false, true)) continuation.resume(Unit)
        }

        fun fail(stage: String) {
            if (completed.compareAndSet(false, true)) {
                continuation.resumeWithException(RuntimeException("key $key service $stage error"))
            }
        }

        if (service == null) {
            fail("lookup")
            return@suspendCancellableCoroutine
        }
        if (service.hasStartAffectiveService()) {
            succeed()
            return@suspendCancellableCoroutine
        }

        val startListener = object : IStartAffectiveServiceLister {
            override fun startSuccess() = succeed()
            override fun hasStarted() = succeed()
            override fun startFail(error: Error?) = fail("start")
        }
        fun start() {
            if (!startRequested.compareAndSet(false, true)) return
            if (useAuthentication) {
                service.startAffectiveService(authenticationInputStream, context, startListener)
            } else {
                service.startAffectiveService(startListener)
            }
        }
        if (service.hasConnectAffectiveService()) {
            start()
        } else {
            service.connectAffectiveServiceConnection(
                object : IConnectionServiceListener {
                    override fun connectionSuccess(sessionId: String?) = start()
                    override fun hasConnected(sessionId: String) = start()
                    override fun connectionError(error: Error?) = fail("connect")
                },
                EnterAffectiveConfigProxy(),
            )
        }
    }

    fun getAffectiveService(
        key: Key, serviceType: AffectiveServiceWay = AffectiveServiceWay.AffectiveLocalService
    ): IAffectiveDataAnalysisService? {
        return algorithmRegistry.getOrCreate(key) { initAffectiveService(serviceType) }?.service
    }

    fun getAffectiveService(
        key: Key, default: IAffectiveDataAnalysisService?
    ): IAffectiveDataAnalysisService? {
        return algorithmRegistry.getOrCreate(key) { default }?.service
    }

    fun registerAffectiveService(
        key: Key,
        service: IAffectiveDataAnalysisService,
    ): IAffectiveDataAnalysisService = algorithmRegistry.register(key, service).service

    fun unregisterAffectiveService(key: Key): IAffectiveDataAnalysisService? =
        algorithmRegistry.remove(key)?.service

    protected open fun initAffectiveService(serviceType: AffectiveServiceWay): IAffectiveDataAnalysisService? {
        return IAffectiveDataAnalysisService.getService(serviceType)
    }

    private fun startAffectiveService(key: Key) {
        getStartAffectiveServiceLister(key)?.apply {
            getAffectiveService(key)?.startAffectiveService(this)
        }
    }

    private fun startAffectiveService(
        key: Key, authenticationInputStream: InputStream?, context: Context?
    ) {
        getStartAffectiveServiceLister(key)?.apply {
            getAffectiveService(key)?.startAffectiveService(
                authenticationInputStream, context, this
            )
        }
    }

    private fun getConnectionServiceListener(key: Key): IConnectionServiceListener? {
        val slot = algorithmRegistry.get(key) ?: return null
        slot.connectionListener?.let { return it }
        return object : IConnectionServiceListener {
                override fun connectionError(error: Error?) {
                    affectionConnectionServiceListeners.forEach {
                        it.connectionError(key, error)
                    }
                }

                override fun connectionSuccess(sessionId: String?) {
                    affectionConnectionServiceListeners.forEach {
                        it.connectionSuccess(key, sessionId)
                    }
                    startAffectiveService(key)
                }

                override fun hasConnected(sessionId: String) {
                    super.hasConnected(sessionId)
                    affectionConnectionServiceListeners.forEach {
                        it.hasConnected(key, sessionId)
                    }
                }
            }.also { slot.connectionListener = it }
    }

    private fun getConnectionServiceListener(
        key: Key, authenticationInputStream: InputStream? = null,
        context: Context? = null,
    ): IConnectionServiceListener? {
        val slot = algorithmRegistry.get(key) ?: return null
        slot.connectionListener?.let { return it }
        return object : IConnectionServiceListener {
                override fun connectionError(error: Error?) {
                    affectionConnectionServiceListeners.forEach {
                        it.connectionError(key, error)
                    }
                }

                override fun connectionSuccess(sessionId: String?) {
                    affectionConnectionServiceListeners.forEach {
                        it.connectionSuccess(key, sessionId)
                    }
                    if (authenticationInputStream == null || context == null) {
                        startAffectiveService(key)
                    } else {
                        startAffectiveService(key, authenticationInputStream, context)
                    }
                }

                override fun hasConnected(sessionId: String) {
                    super.hasConnected(sessionId)
                    affectionConnectionServiceListeners.forEach {
                        it.hasConnected(key, sessionId)
                    }
                }
            }.also { slot.connectionListener = it }
    }

    fun getSessionId(
        key: Key, serviceType: AffectiveServiceWay = AffectiveServiceWay.AffectiveLocalService
    ): String? {
        return getAffectiveService(key, serviceType)?.getSessionId()
    }

    fun appendHeartRateData(
        key: Key, hrData: List<Int>, success: () -> Unit = {}, failure: (String) -> Unit = {}
    ) {
        getAffectiveService(key)?.apply {
            hrData.forEach {
                appendHeartRateData(it)
            }
            success()
        } ?: run {
            failure("key $key service is null")
        }
    }

    fun appendEEGData(
        key: Key,
        eegData: List<Int>,
        isEar: Boolean = false,
        success: () -> Unit = {},
        failure: (String) -> Unit = {}
    ) {
        getAffectiveService(key)?.apply {
            this.appendEEGData(eegData, isEar)
            success()
        } ?: kotlin.run {
            failure("key $key service is null")
        }
    }

    fun appendEEGData(
        key: Key,
        eegData: ByteArray,
        isEar: Boolean = false,
        success: () -> Unit = {},
        failure: (String) -> Unit = {}
    ) {
        getAffectiveService(key)?.apply {
            this.appendEEGData(eegData, isEar)
            success()
        } ?: kotlin.run {
            failure("key $key service is null")
        }
    }

    fun appendEEGData(
        key: Key,
        eegData: Int,
        isEar: Boolean = false,
        success: () -> Unit = {},
        failure: (String) -> Unit = {}
    ) {
        getAffectiveService(key)?.apply {
            this.appendEEGData(eegData, isEar)
            success()
        } ?: kotlin.run {
            failure("key $key service is null")
        }
    }

    fun appendEEGDataToAll(eegData: List<Int>, isEar: Boolean = false) {
        algorithmRegistry.values().forEach { slot ->
            runCatching { slot.service.appendEEGData(eegData, isEar) }
        }
    }

    fun appendEEGDataToAll(eegData: ByteArray, isEar: Boolean = false) {
        algorithmRegistry.values().forEach { slot ->
            runCatching { slot.service.appendEEGData(eegData, isEar) }
        }
    }

    fun appendEEGDataToAll(eegData: Int, isEar: Boolean = false) {
        algorithmRegistry.values().forEach { slot ->
            runCatching { slot.service.appendEEGData(eegData, isEar) }
        }
    }

    fun appendHeartRateDataToAll(heartRateData: Int) {
        algorithmRegistry.values().forEach { slot ->
            runCatching { slot.service.appendHeartRateData(heartRateData) }
        }
    }

    fun appendHeartRateDataToAll(heartRateData: List<Int>) {
        algorithmRegistry.values().forEach { slot ->
            runCatching { heartRateData.forEach(slot.service::appendHeartRateData) }
        }
    }

    fun subscribeData(key: Key) {
        val service = getAffectiveService(key) ?: return
        val slot = algorithmRegistry.get(key) ?: return
        val bioDataListener: (RealtimeBioData?) -> Unit = slot.bioDataListener ?: run {
            val listener: (RealtimeBioData?) -> Unit = { bioData ->
                /*     var startTime= affectiveStartTimeMap[key]
                     if (startTime == null) {
                         startTime = System.currentTimeMillis()
                         affectiveStartTimeMap[key] = startTime
                     }*/
                bioDataListeners.forEach {
                    it(key, bioData)
                }
            }
            listener
        }.also { slot.bioDataListener = it }

        val affectiveDataListener: (RealtimeAffectiveData?) -> Unit =
            slot.affectiveDataListener ?: run {
            val listener: (RealtimeAffectiveData?) -> Unit = { affectiveData ->
                affectiveDataListeners.forEach {
                    it(key, affectiveData)
                }
            }
            listener
        }.also { slot.affectiveDataListener = it }
        service.subscribeData(bioDataListener, affectiveDataListener)
    }

    fun unSubscribeData(key: Key) {/*var endTime = affectiveEndTimeMap[key]
        if (endTime == null) {
            endTime = System.currentTimeMillis()
            affectiveStartTimeMap[key] = endTime
        }*/
        val slot = algorithmRegistry.get(key) ?: return
        slot.service.unSubscribeData(slot.bioDataListener, slot.affectiveDataListener)
    }

    suspend fun suspendCloseAffectiveServiceConnection(key: Key) {
        getAffectiveService(key)?.apply {
            if (hasConnectAffectiveService()) {
                if (hasStartAffectiveService()) {
                    suspendFinishAffectiveService()
                }
                closeAffectiveServiceConnection()
            }
        }
        algorithmRegistry.remove(key)
    }

    private fun getStartAffectiveServiceLister(
        key: Key
    ): IStartAffectiveServiceLister? {
        val slot = algorithmRegistry.get(key) ?: return null
        slot.startListener?.let { return it }
        return object : IStartAffectiveServiceLister {
                override fun startAffectionFail(error: Error?) {
                    super.startAffectionFail(error)
                    affectiveStartServiceLister.forEach {
                        it.startAffectionFail(key, error)
                    }
                }

                override fun startBioFail(error: Error?) {
                    super.startBioFail(error)
                    affectiveStartServiceLister.forEach {
                        it.startBioFail(key, error)
                    }
                }

                override fun startFail(error: Error?) {
                    super.startFail(error)
                    affectiveStartServiceLister.forEach {
                        it.startFail(key, error)
                    }
                }

                override fun startSuccess() {
                    super.startSuccess()
                    affectiveStartServiceLister.forEach {
                        it.startSuccess(key)
                    }
//                    subscribeData(key, service)
                }

                override fun hasStarted() {
                    super.hasStarted()
                    affectiveStartServiceLister.forEach {
                        it.hasStarted(key)
                    }
                }
            }.also { slot.startListener = it }
    }

    suspend fun suspendGetAffectiveReport(key: Key): UploadReportEntity? {
        return getAffectiveService(key)?.suspendGetReport(false)
    }

    fun getAffectiveReport(key: Key, isEar: Boolean = false, needFinishService: Boolean = false) {
        val service = getAffectiveService(key)
        val slot = algorithmRegistry.get(key)
        if (service != null && slot != null) {
            val listener = slot.reportListener ?: object : IGetReportListener {
                    override fun getAffectiveReportError(error: Error?) {
                        affectiveIGetReportListener.forEach {
                            it.getAffectiveReportError(key, error)
                        }
                    }

                    override fun getBioReportError(error: Error?) {
                        affectiveIGetReportListener.forEach {
                            it.getBioReportError(key, error)
                        }
                    }

                    override fun onError(error: Error?) {
                        affectiveIGetReportListener.forEach {
                            it.onError(key, error)
                        }
                    }

                    override fun onSuccess(entity: UploadReportEntity?) {
                        affectiveIGetReportListener.forEach {
                            it.onSuccess(key, entity)
                        }
                    }
                }.also { slot.reportListener = it }
            service.getReport(listener, isEar, needFinishService)
        } else {
            affectiveIGetReportListener.forEach {
                it.onError(key, Error(-1, "key service is null"))
            }
        }
    }

    suspend fun suspendFinishService(key: Key) {
        try {
            getAffectiveService(key)?.suspendFinishAffectiveService()
        } finally {
            algorithmRegistry.remove(key)
        }
    }

    fun finishService(key: Key) {
        val service = getAffectiveService(key)
        val slot = algorithmRegistry.get(key)
        if (service != null && slot != null) {
            val listener = slot.finishListener ?: object : IFinishAffectiveServiceListener {
                    override fun finishError(error: Error?) {
                        affectiveFinishServiceListener.forEach {
                            it.finishError(key, error)
                        }
                        algorithmRegistry.remove(key)
                    }

                    override fun finishSuccess() {
                        affectiveFinishServiceListener.forEach {
                            it.finishSuccess(key)
                        }
                        algorithmRegistry.remove(key)
                    }
                }.also { slot.finishListener = it }
            service.finishAffectiveService(listener)
        } else {
            affectiveFinishServiceListener.forEach {
                it.finishError(key, Error(-1, "key service is null"))
            }
        }
    }

    fun addAffectionConnectionServiceListener(listener: IAffectionConnectionServiceListener<Key>) {
        affectionConnectionServiceListeners.addWithoutRepeat(listener)
    }

    fun removeAffectionConnectionServiceListener(listener: IAffectionConnectionServiceListener<Key>) {
        affectionConnectionServiceListeners.remove(listener)
    }

    fun addAffectiveStartServiceLister(listener: IAffectiveStartServiceLister<Key>) {
        affectiveStartServiceLister.addWithoutRepeat(listener)
    }

    fun removeAffectiveStartServiceLister(listener: IAffectiveStartServiceLister<Key>) {
        affectiveStartServiceLister.remove(listener)
    }

    fun addAffectiveIGetReportListener(listener: IAffectiveIGetReportListener<Key>) {
        affectiveIGetReportListener.addWithoutRepeat(listener)
    }

    fun removeAffectiveIGetReportListener(listener: IAffectiveIGetReportListener<Key>) {
        affectiveIGetReportListener.remove(listener)
    }

    fun addAffectiveFinishServiceListener(listener: IAffectiveFinishServiceListener<Key>) {
        affectiveFinishServiceListener.addWithoutRepeat(listener)
    }

    fun removeAffectiveFinishServiceListener(listener: IAffectiveFinishServiceListener<Key>) {
        affectiveFinishServiceListener.remove(listener)
    }

    fun addSubscribeDataListener(
        bioDataListener: ((Key, RealtimeBioData?) -> Unit)? = null,
        affectiveDataListener: ((Key, RealtimeAffectiveData?) -> Unit)? = null
    ) {
        bioDataListener?.apply {
            bioDataListeners.addWithoutRepeat(this)
        }
        affectiveDataListener?.apply {
            affectiveDataListeners.addWithoutRepeat(this)
        }
    }

    fun removeSubscribeDataListener(
        bioDataListener: ((Key, RealtimeBioData?) -> Unit)? = null,
        affectiveDataListener: ((Key, RealtimeAffectiveData?) -> Unit)? = null,
    ) {
        bioDataListener?.apply {
            bioDataListeners.remove(this)
        }
        affectiveDataListener?.apply {
            affectiveDataListeners.remove(this)
        }
    }


    @Deprecated("")
    fun subscribeData(
        bioDataListener: ((Key, RealtimeBioData?) -> Unit)? = null,
        affectiveDataListener: ((Key, RealtimeAffectiveData?) -> Unit)? = null
    ) {
        addSubscribeDataListener(bioDataListener, affectiveDataListener)
    }

    @Deprecated("")
    fun unSubscribeData(
        bioDataListener: ((Key, RealtimeBioData?) -> Unit)? = null,
        affectiveDataListener: ((Key, RealtimeAffectiveData?) -> Unit)? = null,
    ) {
        removeSubscribeDataListener(bioDataListener, affectiveDataListener)
    }

    private inline fun <reified T> MutableList<T>.addWithoutRepeat(t: T) {
        if (!contains(t)) {
            add(t)
        }
    }

}
