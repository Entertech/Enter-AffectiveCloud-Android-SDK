package cn.entertech.affectivecloudsdk

import cn.entertech.affective.sdk.api.Callback
import cn.entertech.affective.sdk.api.Callback2
import cn.entertech.affective.sdk.api.IConnectionServiceListener
import cn.entertech.affective.sdk.api.IFinishAffectiveServiceListener
import cn.entertech.affective.sdk.api.IStartAffectiveServiceLister
import cn.entertech.affective.sdk.bean.AffectiveDataCategory
import cn.entertech.affective.sdk.bean.Error
import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData
import cn.entertech.affective.sdk.utils.AffectiveLogHelper
import cn.entertech.affectivecloudsdk.entity.*
import cn.entertech.affectivecloudsdk.interfaces.*
import java.lang.IllegalStateException
import java.util.concurrent.CopyOnWriteArrayList


/**
 * 步骤：
 * 1.建立webSocket连接，并创建session会话
 * 2.启动、初始化生物数据基础分析服务&& 若配置感云计算服务，则启动情感云计算服务
 * 3.启动服务成功后，各自订阅自己的数据
 * 4.取消订阅----不必须
 * 5.结束服务，释放资源
 * 6.关闭会话，关闭webSocket连接
 *
 * */
class EnterAffectiveCloudManager(var config: EnterAffectiveCloudConfig) :
    IEnterAffectiveCloudManager {
    companion object {
        private const val TAG = "EnterAffectiveCloudManager"
    }

    private var mApi: BaseApi
    private var mBiodataRealtimeListener =
        CopyOnWriteArrayList<(RealtimeBioData?) -> Unit>()
    private var mAffectiveRealtimeListener =
        CopyOnWriteArrayList<(RealtimeAffectiveData?) -> Unit>()
    private var disconnectListeners = CopyOnWriteArrayList<(String) -> Unit>()
    private var isInit = false

    /**
     * 为了不把isInit暴露出去，只能获取，不能设置
     * */
    private val realDisconnectListener = { msg: String ->
        disconnectListeners.forEach {
            isInit = false
            it(msg)
        }
    }

    init {
        if (config.websocketTimeout == null) {
            mApi = EnterAffectiveCloudApiImpl(
                config.uri!!,
                config.appKey!!,
                config.appSecret!!,
                config.userId!!,
                config.uploadCycle
            )
        } else {
            mApi = EnterAffectiveCloudApiImpl(
                config.uri!!,
                config.websocketTimeout!!,
                config.appKey!!,
                config.appSecret!!,
                config.userId!!,
                config.uploadCycle
            )
        }
        if (config.availableBioDataCategories == null) {
            throw IllegalStateException("biodata services must not be null")
        }
    }

    private fun initBiodata(callback: Callback) {
        AffectiveLogHelper.i(TAG,"initBiodata")
        var optionsMap = java.util.HashMap<String, Any?>()
        optionsMap["storage_settings"] = config.storageSettings?.body()
        optionsMap["bio_data_tolerance"] = config.biodataTolerance?.body()
        optionsMap["algorithm_params"] = config.algorithmParams?.body()
        mApi.initBiodataServices(config.availableBioDataCategories!!, object : Callback {
            override fun onSuccess() {
                isInit = true
                if (config.mBiodataSubscribeParams != null) {
                    mApi.subscribeBioData(config.mBiodataSubscribeParams!!,
                        object : Callback2<RealtimeBioData> {
                            override fun onSuccess(t: RealtimeBioData?) {
                                mBiodataRealtimeListener.forEach {
                                    it.invoke(t)
                                }
                            }

                            override fun onError(error: Error?) {
                                callback.onError(error)
                            }

                        },
                        object : Callback2<SubBiodataFields> {
                            override fun onSuccess(t: SubBiodataFields?) {
                                if (config.availableAffectiveDataCategories == null) {
                                    callback.onSuccess()
                                }
                            }

                            override fun onError(error: Error?) {
                                callback.onError(error)
                            }

                        })
                } else if (config.availableAffectiveDataCategories == null) {
                    callback.onSuccess()
                }
            }

            override fun onError(error: Error?) {
                isInit = false
                callback.onError(error)
            }
        }, optionsMap)
    }

    private fun initAffective(callback: Callback) {
        AffectiveLogHelper.d(TAG,"initBiodata")
        mApi.initAffectiveDataServices(
            config.availableAffectiveDataCategories!!,
            object : Callback {
                override fun onSuccess() {
                    if (config.mAffectiveSubscribeParams != null) {
                        mApi.subscribeAffectiveData(config.mAffectiveSubscribeParams!!,
                            object : Callback2<RealtimeAffectiveData> {
                                override fun onSuccess(t: RealtimeAffectiveData?) {
                                    mAffectiveRealtimeListener.forEach {
                                        it.invoke(t)
                                    }
                                }

                                override fun onError(error: Error?) {
                                    callback.onError(error)
                                }
                            },
                            object : Callback2<SubAffectiveDataFields> {
                                override fun onSuccess(t: SubAffectiveDataFields?) {
                                    if (t != null) {
                                        selectAvailableAffectiveServicesInRemote(t)
                                    }
                                    callback.onSuccess()
                                }

                                override fun onError(error: Error?) {
                                    callback.onError(error)
                                }
                            })
                    } else {
                        callback.onSuccess()
                    }
                }

                override fun onError(error: Error?) {
                    callback.onError(error)
                }
            })
    }

    private fun selectAvailableAffectiveServicesInRemote(subData: SubAffectiveDataFields) {
        var affectiveDataCategories = mutableListOf<AffectiveDataCategory>()
        if (subData.subAttentionFields != null) {
            affectiveDataCategories.add(AffectiveDataCategory.ATTENTION)
        }
        if (subData.subRelaxationFields != null) {
            affectiveDataCategories.add(AffectiveDataCategory.RELAXATION)
        }
        if (subData.subPressureFields != null) {
            affectiveDataCategories.add(AffectiveDataCategory.PRESSURE)
        }
        if (subData.subPleasureFields != null) {
            affectiveDataCategories.add(AffectiveDataCategory.PLEASURE)
        }
        if (subData.subArousalFields != null) {
            affectiveDataCategories.add(AffectiveDataCategory.AROUSAL)
        }
        if (subData.subSleepFields != null) {
            affectiveDataCategories.add(AffectiveDataCategory.SLEEP)
        }
        if (subData.subCoherenceFields != null) {
            affectiveDataCategories.add(AffectiveDataCategory.COHERENCE)
        }
        if (subData.subFlowFields != null) {
            affectiveDataCategories.add(AffectiveDataCategory.FLOW)
        }
        if (subData.subSsvepMultiClassifyFields != null) {
            affectiveDataCategories.add(AffectiveDataCategory.SSVEP_MULTI_CLASSIFY)
        }
        config.availableAffectiveDataCategories = affectiveDataCategories
    }


    override fun isInited(): Boolean {
        return isInit
    }

    fun openWebSocket(listener: IConnectionServiceListener) {
        mEnterWebSocketCallback.onOpen = {
            mApi.createSession(object : Callback2<String> {
                override fun onSuccess(t: String?) {
                    listener.connectionSuccess(t)
                }

                override fun onError(error: Error?) {
                    listener.connectionError(error)
                    isInit = false
                }
            })
        }
        mEnterWebSocketCallback.onError = { e ->
            isInit = false
            e?.printStackTrace()
            listener.connectionError(Error(-1, e.toString()))
        }
        mEnterWebSocketCallback.onClose = {
            isInit = false
        }
        mApi.openWebSocket(mEnterWebSocketCallback)
    }


    override fun getSessionId(): String? {
        return mApi.getSessionId()
    }

    private val mEnterWebSocketCallback by lazy {
        EnterWebSocketCallback()
    }

    override fun init(initListener: IStartAffectiveServiceLister) {
        initBiodata(object : Callback {
            override fun onSuccess() {
                AffectiveLogHelper.d(TAG,"initBiodata onSuccess")
                if (config.availableAffectiveDataCategories != null) {
                    initAffective(object : Callback {
                        override fun onSuccess() {
                            initListener.startSuccess()
                        }

                        override fun onError(error: Error?) {
                            initListener.startAffectionFail(error)
                            initListener.startSuccess()
                        }
                    })
                } else {
                    initListener.startAffectionFail(
                        Error(
                            -1,
                            "availableAffectiveDataCategories is null"
                        )
                    )
                }
            }

            override fun onError(error: Error?) {
                initListener.startBioFail(error)
                if (config.availableAffectiveDataCategories != null) {
                    initAffective(object : Callback {
                        override fun onSuccess() {
                            initListener.startSuccess()
                        }

                        override fun onError(error: Error?) {
                            initListener.startFail(error)
                        }
                    })
                } else {
                    initListener.startAffectionFail(
                        Error(
                            -1,
                            "availableAffectiveDataCategories is null"
                        )
                    )
                }
            }
        })

    }

    override fun appendMCEEGData(mceegData: ByteArray) {
        mApi.appendMCEEGData(mceegData)
    }

    override fun appendPEPRData(peprData: ByteArray) {
        mApi.appendPEPRData(peprData)
    }

    override fun appendBCGData(bcgData: ByteArray, packageCount: Int) {
        mApi.appendBCGData(bcgData, packageCount)
    }

    override fun appendGyroData(gyroData: ByteArray, packageCount: Int) {
        mApi.appendGyroData(gyroData, packageCount)
    }

    override fun appendEEGData(brainData: ByteArray) {
        mApi.appendEEGData(brainData)
    }

    override fun appendDCEEGData(brainData: ByteArray) {
        mApi.appendDCEEGData(brainData)
    }

    override fun appendSCEEGData(brainData: ByteArray) {

    }

    override fun appendHeartRateData(heartRateData: Int) {
        mApi.appendHeartData(heartRateData)
    }

    override fun addBiodataRealtimeListener(listener: (RealtimeBioData?) -> Unit) {
        mBiodataRealtimeListener.add(listener)
    }

    override fun addAffectiveDataRealtimeListener(listener: (RealtimeAffectiveData?) -> Unit) {
        mAffectiveRealtimeListener.add(listener)
    }

    override fun removeBiodataRealtimeListener(listener: (RealtimeBioData?) -> Unit) {
        mBiodataRealtimeListener.remove(listener)
    }

    override fun removeAffectiveRealtimeListener(listener: (RealtimeAffectiveData?) -> Unit) {
        mAffectiveRealtimeListener.remove(listener)
    }

    override fun getBiodataReport(callback: Callback2<HashMap<Any, Any?>>) {
        mApi.getBiodataReport(config.availableBioDataCategories!!, callback)
    }

    override fun getAffectiveDataReport(callback: Callback2<HashMap<Any, Any?>>) {
        mApi.getAffectivedataReport(config.availableAffectiveDataCategories!!, callback)
    }

    override fun restore(listener: IStartAffectiveServiceLister) {
        if (mApi.isWebSocketOpen()) {
            mApi.restore(object : Callback {
                override fun onSuccess() {
                    init(listener)
                }

                override fun onError(error: Error?) {
                    listener.startFail(error)
                    isInit = false
                }
            })
        } else {
            mEnterWebSocketCallback.onOpen = {
                mApi.restore(object : Callback {
                    override fun onSuccess() {
                        init(listener)
                    }

                    override fun onError(error: Error?) {
                        isInit = false
                        listener.startFail(error)
                    }
                })
            }
            mEnterWebSocketCallback.onClose = {
                isInit = false
            }
            mEnterWebSocketCallback.onError = { e ->
                isInit = false
                e?.printStackTrace()
                listener.startFail(Error(-1, e.toString()))
            }

            mApi.openWebSocket(mEnterWebSocketCallback)
        }
    }

    override fun release(listener: IFinishAffectiveServiceListener) {
        if (config.availableAffectiveDataCategories != null) {
            mApi.finishAffectiveDataServices(
                config.availableAffectiveDataCategories!!, object : Callback {
                    override fun onSuccess() {
                        listener.finishSuccess()
                    }

                    override fun onError(error: Error?) {
                        listener.finishAffectiveFail(error)
                    }
                }
            )
        } else {
            listener.finishAffectiveFail(Error(-1, "affective is null"))
            listener.finishSuccess()
        }
    }

    override fun closeSession(callback: Callback) {
        mApi.destroySessionAndCloseWebSocket(callback)
    }


    override fun addWebSocketConnectListener(listener: () -> Unit) {
        mApi.addConnectListener(listener)
    }

    override fun addWebSocketDisconnectListener(listener: (String) -> Unit) {
        disconnectListeners.add(listener)
        if (disconnectListeners.size == 1) {
            mApi.addDisconnectListener(realDisconnectListener)
        }

    }

    override fun removeWebSocketConnectListener(listener: () -> Unit) {
        mApi.removeConnectListener(listener)
    }

    override fun removeWebSocketDisconnectListener(listener: (String) -> Unit) {
        disconnectListeners.remove(listener)
        if (disconnectListeners.isEmpty()) {
            mApi.removeDisconnectListener(realDisconnectListener)
        }
    }

    override fun addRawJsonRequestListener(listener: (String) -> Unit) {
        mApi.addRawJsonRequestListener(listener)
    }

    override fun addRawJsonResponseListener(listener: (String) -> Unit) {
        mApi.addRawJsonResponseListener(listener)
    }

    override fun removeRawJsonRequestListener(listener: (String) -> Unit) {
        mApi.removeRawJsonRequestListener(listener)
    }

    override fun removeRawJsonResponseListener(listener: (String) -> Unit) {
        mApi.removeRawJsonResponseListener(listener)
    }

    override fun isWebSocketOpen(): Boolean {
        return mApi.isWebSocketOpen()
    }

    override fun closeWebSocket() {
        closeSession(object : Callback {
            override fun onSuccess() {
                mApi.closeWebSocket()
            }

            override fun onError(error: Error?) {
                mApi.closeWebSocket()
            }
        })

    }

    fun closeConnection(code: Int, message: String) {
        mApi.closeConnection(code, message)
    }

    /**
     * 无效指令
     * @see https://docs.affectivecloud.cn/%F0%9F%8E%99%E6%8E%A5%E5%8F%A3%E5%8D%8F%E8%AE%AE/%E7%94%9F%E7%89%A9%E6%95%B0%E6%8D%AE%E5%9F%BA%E7%A1%80%E5%88%86%E6%9E%90%E6%9C%8D%E5%8A%A1%E5%8D%8F%E8%AE%AE
     * */
    @Deprecated("")
    fun submit(remark: List<RecData>, callback: Callback) {
        mApi.submit(remark, callback)
    }

}