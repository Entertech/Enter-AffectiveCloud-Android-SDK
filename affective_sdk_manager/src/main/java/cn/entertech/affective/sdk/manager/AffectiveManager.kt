package cn.entertech.affective.sdk.manager

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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

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


    fun connectAndStartAffectiveService(key: Key, failure: (String) -> Unit = {}) {
        getAffectiveService(key)?.apply {
            if (hasConnectAffectiveService()) {
                if (hasStartAffectiveService()) {
                    affectiveStartServiceLister.forEach {
                        it.hasStarted(key)
                    }
                } else {
                    startAffectiveService(key, this)
                }
            } else {
                getConnectionServiceListener(key, this)?.apply {
                    connectAffectiveServiceConnection(this, EnterAffectiveConfigProxy())
                }
            }
        } ?: kotlin.run {
            failure("  $key affective Service is null")
        }
    }


    private fun getAffectiveService(
        key: Key, serviceType: AffectiveServiceWay = AffectiveServiceWay.AffectiveLocalService
    ): IAffectiveDataAnalysisService? {
        return affectiveServiceMap.getValueWithInit(key, initValue = {
            IAffectiveDataAnalysisService.getService(serviceType)
        })
    }

    private fun startAffectiveService(key: Key, service: IAffectiveDataAnalysisService) {
        getStartAffectiveServiceLister(key, service)?.apply {
            service.startAffectiveService(this)
        }
    }

    private fun getConnectionServiceListener(
        key: Key, service: IAffectiveDataAnalysisService
    ): IConnectionServiceListener? {
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
                    startAffectiveService(key, service)
                }

                override fun hasConnected() {
                    super.hasConnected()
                    affectionConnectionServiceListeners.forEach {
                        it.hasConnected(key)
                    }
                }
            }
        })
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
        key: Key, eegData: List<Int>, success: () -> Unit = {}, failure: (String) -> Unit = {}
    ) {
        getAffectiveService(key)?.apply {
            this.appendEEGData(eegData)
            success()
        } ?: kotlin.run {
            failure("key $key service is null")
        }
    }

    fun subscribeData(
        key: Key,
        service: IAffectiveDataAnalysisService? = getAffectiveService(key),
    ) {
        val bioDataListener = bioDataListenerMap.getValueWithInit(key, initValue = {
            { bioData ->
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
        service?.subscribeData(bioDataListener, affectiveDataListener)
    }

    fun unSubscribeData(
        key: Key,
        service: IAffectiveDataAnalysisService? = getAffectiveService(key),
    ) {
        service?.unSubscribeData(bioDataListenerMap[key], affectiveDataListenerMap[key])
    }


    private fun getStartAffectiveServiceLister(
        key: Key, service: IAffectiveDataAnalysisService
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

    fun getAffectiveReport(key: Key, needFinishService: Boolean = false) {
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
                getReport(this, needFinishService)
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