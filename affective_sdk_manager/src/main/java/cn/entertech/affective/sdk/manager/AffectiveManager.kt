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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class AffectiveManager<Key> {

    /**
     * 实例化回调
     * */
    var mInstanceListener: ((Key) -> IAffectiveDataAnalysisService?) = {
        IAffectiveDataAnalysisService.getService(AffectiveServiceWay.AffectiveLocalService)
    }

    private val managerMap: MutableMap<Key, IAffectiveDataAnalysisService?> by lazy {
        ConcurrentHashMap<Key, IAffectiveDataAnalysisService?>()
    }

    private val connectionListeners by lazy {
        CopyOnWriteArrayList<(Key) -> Unit>()
    }

    private val disconnectListeners by lazy {
        CopyOnWriteArrayList<(Key, String) -> Unit>()
    }

    private val bdListeners by lazy {
        CopyOnWriteArrayList<(Key, RealtimeBioData?) -> Unit>()
    }

    private val adListeners by lazy {
        CopyOnWriteArrayList<(Key, RealtimeAffectiveData?) -> Unit>()
    }
    private var bdListener: ((RealtimeBioData?) -> Unit)? = null

    private val adListener: ((RealtimeAffectiveData?) -> Unit)? = null

    fun getAffectiveService(key: Key): IAffectiveDataAnalysisService? {
        return mInstanceListener.let {
            var service = managerMap[key]
            if (service == null) {
                service = it.invoke(key)
                service?.addServiceConnectStatueListener(connectionListener = {
                    connectionListeners.forEach { listener ->
                        listener(key)
                    }
                }, disconnectListener = {
                    disconnectListeners.forEach { listener ->
                        listener(key, it)
                    }
                })
                managerMap[key] = service
            }
            service
        }
    }

    fun addServiceConnectStatueListener(
        connectionListener: ((Key) -> Unit), disconnectListener: ((Key, String) -> Unit)
    ) {
        connectionListeners.add(connectionListener)
        disconnectListeners.add(disconnectListener)
    }

    fun removeServiceConnectStatueListener(
        connectionListener: ((Key) -> Unit), disconnectListener: ((Key, String) -> Unit)?
    ) {
        connectionListeners.remove(connectionListener)
        disconnectListeners.remove(disconnectListener)
    }

    fun connectAffectiveServiceConnection(
        key: Key, listener: IConnectionServiceListener, configProxy: EnterAffectiveConfigProxy
    ) {
        getAffectiveService(key)?.connectAffectiveServiceConnection(listener, configProxy)
    }

    fun closeAffectiveServiceConnection(key: Key) {
        getAffectiveService(key)?.closeAffectiveServiceConnection()
    }

    fun startAffectiveService(key: Key, listener: IStartAffectiveServiceLister) {
        getAffectiveService(key)?.startAffectiveService(listener)
    }

    fun finishAffectiveService(key: Key, listener: IFinishAffectiveServiceListener) {
        getAffectiveService(key)?.finishAffectiveService(listener)
    }

    fun getReport(key: Key, listener: IGetReportListener, needFinishService: Boolean) {
        getAffectiveService(key)?.getReport(listener, needFinishService)
    }

    fun subscribeData(
        key: Key,
        bdListener: ((Key, RealtimeBioData?) -> Unit),
        listener: ((Key, RealtimeAffectiveData?) -> Unit)
    ) {
        if (bdListeners.isEmpty() && adListeners.isEmpty()) {
            getAffectiveService(key)?.subscribeData({
                bdListeners.forEach { listener ->
                    listener(key, it)
                }
            }, {
                adListeners.forEach { listener ->
                    listener(key, it)
                }
            })
        }
        if (!bdListeners.contains(bdListener)) {
            bdListeners.add(bdListener)
        }
        if (!adListeners.contains(listener)) {
            adListeners.add(listener)
        }
    }

    /**
     * 取消订阅
     * */
    fun unSubscribeData(
        key: Key,
        bdListener: ((Key, RealtimeBioData?) -> Unit),
        listener: ((Key, RealtimeAffectiveData?) -> Unit)
    ) {
        bdListeners.remove(bdListener)
        adListeners.remove(listener)
        if (bdListeners.isEmpty() && adListeners.isEmpty()) {
            getAffectiveService(key)?.unSubscribeData(this.bdListener, this.adListener)
        }
    }


    /**
     * 一键订阅数据
     * */
    fun oneClickSubscribeData(
        key: Key,
        listener: IConnectionServiceListener? = null,
        startListener: IStartAffectiveServiceLister,
        bdListener: ((Key, RealtimeBioData?) -> Unit),
        adListener: ((Key, RealtimeAffectiveData?) -> Unit),
        configProxy: EnterAffectiveConfigProxy = EnterAffectiveConfigProxy()
    ) {
        getAffectiveService(key)?.apply {
            if (hasConnectAffectiveService()) {
                startAffectiveService(key, this, startListener, bdListener, adListener)
            } else {
                connectAffectiveServiceConnection(object : IConnectionServiceListener {
                    override fun connectionSuccess(sessionId: String?) {
                        listener?.connectionSuccess(sessionId)
                        startAffectiveService(
                            key, this@apply, startListener, bdListener, adListener
                        )
                    }

                    override fun connectionError(error: Error?) {
                        listener?.connectionError(error)
                    }

                }, configProxy)
            }
        } ?: kotlin.run {
            listener?.connectionError(
                Error(
                    -1, "affectiveService is null"
                )
            )
        }
    }

    private fun startAffectiveService(
        key: Key,
        affective: IAffectiveDataAnalysisService,
        listener: IStartAffectiveServiceLister,
        bdListener: ((Key, RealtimeBioData?) -> Unit),
        adListener: ((Key, RealtimeAffectiveData?) -> Unit)
    ) {
        if (affective.hasStartAffectiveService()) {
            subscribeData(key, bdListener, adListener)
        } else {
            affective.startAffectiveService(object : IStartAffectiveServiceLister {
                override fun startSuccess() {
                    listener.startSuccess()
                    subscribeData(key, bdListener, adListener)
                }

                override fun startBioFail(error: Error?) {
                    listener.startBioFail(error)
                }

                override fun startAffectionFail(error: Error?) {
                    listener.startAffectionFail(error)
                }

                override fun startFail(error: Error?) {
                    listener.startFail(error)
                }
            })
        }
    }

}