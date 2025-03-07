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
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * 情感云服务管理类（泛型类）
 *
 * @param Key 设备/服务标识类型，用于区分不同设备/服务的实例
 */
@Suppress("unused")
open class AffectiveManager<Key> {

    private val affectiveServiceMap: MutableMap<Key, IAffectiveDataAnalysisService?> by lazy {
        ConcurrentHashMap()
    }

    private val connectionServiceListenerMap: MutableMap<Key, IConnectionServiceListener?> by lazy {
        ConcurrentHashMap()
    }

    private val startAffectiveServiceListerMap: MutableMap<Key, IStartAffectiveServiceLister?> by lazy {
        ConcurrentHashMap()
    }
    private val finishAffectiveServiceListenerMap: MutableMap<Key, IFinishAffectiveServiceListener?> by lazy {
        ConcurrentHashMap()
    }

    private val bioDataListenerMap: MutableMap<Key, ((RealtimeBioData?) -> Unit)?> by lazy {
        ConcurrentHashMap()
    }

    private val affectiveDataListenerMap: MutableMap<Key, ((RealtimeAffectiveData?) -> Unit)?> by lazy {
        ConcurrentHashMap()
    }
    private val getReportListenerMap: MutableMap<Key, IGetReportListener?> by lazy {
        ConcurrentHashMap()
    }

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
    private val affectiveStartTimeMap: MutableMap<Key, Long?> by lazy {
        ConcurrentHashMap()
    }
    private val affectiveEndTimeMap: MutableMap<Key, Long?> by lazy {
        ConcurrentHashMap()
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
                connectAffectiveServiceConnection(getConnectionServiceListener(key), configProxy)
            }
        } ?: kotlin.run {
            affectionConnectionServiceListeners.forEach {
                it.connectionError(key, Error(-1, "key $key affective service is null"))
            }
        }
    }


    suspend fun suspendConnectAndStartAffectiveService(key: Key) {
        suspendCoroutine { continuation ->
            getAffectiveService(key)?.apply {
                if (hasStartAffectiveService()) {
                    continuation.resume(Unit)
                } else {
                    val startAffectiveServiceLister = object : IStartAffectiveServiceLister {
                        override fun startSuccess() {
                            super.startSuccess()
                            continuation.resume(Unit)
                        }

                        override fun hasStarted() {
                            super.hasStarted()
                            continuation.resume(Unit)
                        }

                        override fun startFail(error: Error?) {
                            super.startFail(error)
                            continuation.resumeWithException(RuntimeException("key $key service start error"))
                        }
                    }
                    if (hasConnectAffectiveService()) {
                        startAffectiveService(key, startAffectiveServiceLister)
                    } else {
                        val connectionListener = object : IConnectionServiceListener {
                            override fun connectionSuccess(sessionId: String?) {
                                startAffectiveService(key, startAffectiveServiceLister)
                            }

                            override fun connectionError(error: Error?) {
                                continuation.resumeWithException(RuntimeException("key $key service connect error"))
                            }

                            override fun hasConnected(sessionId: String) {
                                super.hasConnected(sessionId)
                                startAffectiveService(key, startAffectiveServiceLister)
                            }
                        }
                        connectAffectiveServiceConnection(
                            connectionListener, EnterAffectiveConfigProxy()
                        )
                    }
                }
            }
        }
    }


    suspend fun suspendConnectAndStartAffectiveService(key: Key ,authenticationInputStream: InputStream?,
                                                       context: Context?,) {
        suspendCoroutine { continuation ->
            getAffectiveService(key)?.apply {
                if (hasStartAffectiveService()) {
                    continuation.resume(Unit)
                } else {
                    val startAffectiveServiceLister = object : IStartAffectiveServiceLister {
                        override fun startSuccess() {
                            super.startSuccess()
                            continuation.resume(Unit)
                        }

                        override fun hasStarted() {
                            super.hasStarted()
                            continuation.resume(Unit)
                        }

                        override fun startFail(error: Error?) {
                            super.startFail(error)
                            continuation.resumeWithException(RuntimeException("key $key service start error"))
                        }
                    }
                    if (hasConnectAffectiveService()) {
                        startAffectiveService(key,authenticationInputStream,context, startAffectiveServiceLister)
                    } else {
                        val connectionListener = object : IConnectionServiceListener {
                            override fun connectionSuccess(sessionId: String?) {
                                startAffectiveService(key,authenticationInputStream,context, startAffectiveServiceLister)
                            }

                            override fun connectionError(error: Error?) {
                                continuation.resumeWithException(RuntimeException("key $key service connect error"))
                            }

                            override fun hasConnected(sessionId: String) {
                                super.hasConnected(sessionId)
                                startAffectiveService(key,authenticationInputStream,context, startAffectiveServiceLister)
                            }
                        }
                        connectAffectiveServiceConnection(
                            connectionListener, EnterAffectiveConfigProxy()
                        )
                    }
                }
            }
        }
    }

    fun getAffectiveService(
        key: Key, serviceType: AffectiveServiceWay = AffectiveServiceWay.AffectiveLocalService
    ): IAffectiveDataAnalysisService? {
        return affectiveServiceMap.getValueWithInit(key, initValue = {
            IAffectiveDataAnalysisService.getService(serviceType)
        })
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
        return connectionServiceListenerMap.getValueWithInit(key, initValue = {
            object : IConnectionServiceListener {
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
            }
        })
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

    fun subscribeData(key: Key) {
        val bioDataListener = bioDataListenerMap.getValueWithInit(key, initValue = {
            { bioData ->
                /*     var startTime= affectiveStartTimeMap[key]
                     if (startTime == null) {
                         startTime = System.currentTimeMillis()
                         affectiveStartTimeMap[key] = startTime
                     }*/
                bioDataListeners.forEach {
                    it(key, bioData)
                }
            }
        })

        val affectiveDataListener = affectiveDataListenerMap.getValueWithInit(key, initValue = {
            { affectiveData ->
                affectiveDataListeners.forEach {
                    it(key, affectiveData)
                }
            }
        })
        getAffectiveService(key)?.subscribeData(bioDataListener, affectiveDataListener)
    }

    fun unSubscribeData(key: Key) {/*var endTime = affectiveEndTimeMap[key]
        if (endTime == null) {
            endTime = System.currentTimeMillis()
            affectiveStartTimeMap[key] = endTime
        }*/
        getAffectiveService(key)?.unSubscribeData(
            bioDataListenerMap[key], affectiveDataListenerMap[key]
        )
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
        affectiveServiceMap.remove(key)
    }

    private fun getStartAffectiveServiceLister(
        key: Key
    ): IStartAffectiveServiceLister? {
        return startAffectiveServiceListerMap.getValueWithInit(key, initValue = {
            object : IStartAffectiveServiceLister {
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
            }
        })
    }

    suspend fun suspendGetAffectiveReport(key: Key): UploadReportEntity? {
        return getAffectiveService(key)?.suspendGetReport(false)
    }

    fun getAffectiveReport(key: Key, isEar: Boolean = false, needFinishService: Boolean = false) {
        getAffectiveService(key)?.apply {
            getReportListenerMap.getValueWithInit(key, initValue = {
                object : IGetReportListener {
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
                }
            })?.apply {
                getReport(this, isEar, needFinishService)
            }

        } ?: run {
            affectiveIGetReportListener.forEach {
                it.onError(key, Error(-1, "key service is null"))
            }
        }
    }

    suspend fun suspendFinishService(key: Key) {
        getAffectiveService(key)?.apply {
            suspendFinishAffectiveService()
        }
        affectiveServiceMap.remove(key)
    }

    fun finishService(key: Key) {
        getAffectiveService(key)?.apply {
            finishAffectiveServiceListenerMap.getValueWithInit(key, initValue = {
                object : IFinishAffectiveServiceListener {
                    override fun finishError(error: Error?) {
                        affectiveFinishServiceListener.forEach {
                            it.finishError(key, error)
                        }
                    }

                    override fun finishSuccess() {
                        affectiveFinishServiceListener.forEach {
                            it.finishSuccess(key)
                        }
                    }
                }
            })?.apply {
                finishAffectiveService(this)
            }

        } ?: kotlin.run {
            affectiveFinishServiceListener.forEach {
                it.finishError(key, Error(-1, "key service is null"))
            }
        }
        affectiveServiceMap.remove(key)
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

    fun subscribeData(
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

    fun unSubscribeData(
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

    private inline fun <reified T> MutableList<T>.addWithoutRepeat(t: T) {
        if (!contains(t)) {
            add(t)
        }
    }

    private inline fun <reified K> MutableMap<Key, K?>.getValueWithInit(
        key: Key,
        initValue: () -> K?,
        checkValidValue: (K?) -> Boolean = {
            it != null
        },
    ): K? {
        var value = get(key)
        if (!checkValidValue(value)) {
            value = initValue()
            this[key] = value
        }
        return value
    }
}