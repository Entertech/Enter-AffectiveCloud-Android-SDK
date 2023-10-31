package cn.entertech.affectivecloudsdk

import cn.entertech.affective.sdk.api.Callback
import cn.entertech.affective.sdk.api.Callback2
import cn.entertech.affective.sdk.bean.Error
import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData
import cn.entertech.affective.sdk.bean.BioOrAffectiveDataCategory
import cn.entertech.affectivecloudsdk.entity.*
import cn.entertech.affectivecloudsdk.interfaces.*
import java.lang.IllegalStateException
import java.util.concurrent.CopyOnWriteArrayList



/**
 * 步骤：
 * 1.建立webSocket连接
 * 2.启动、初始化生物数据基础分析服务&& 若配置感云计算服务，则启动情感云计算服务
 * 3.启动服务成功后，各自订阅自己的数据
 * 4.取消订阅----不必须
 * 5.结束服务
 * 6.关闭webSocket连接
 *
 *
 *
 * 对于业务来说 只需要
 * 启动服务
 * 订阅数据
 * 发送数据
 * 取消订阅
 * 关闭服务
 * */
class EnterAffectiveCloudManager(var config: EnterAffectiveCloudConfig) :
    IEnterAffectiveCloudManager {
    companion object{
        private const val TAG="EnterAffectiveCloudManager"
    }
    private var mApi: BaseApi
    private var mBiodataRealtimeListener =
        CopyOnWriteArrayList<(cn.entertech.affective.sdk.bean.RealtimeBioData?) -> Unit>()
    private var mAffectiveRealtimeListener =
        CopyOnWriteArrayList<(cn.entertech.affective.sdk.bean.RealtimeAffectiveData?) -> Unit>()
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
        if (config.availableBiodataBioOrAffectiveDataCategories == null) {
            throw IllegalStateException("biodata services must not be null")
        }
    }

    private fun openWebSocket(webSocketCallback: WebSocketCallback) {
        mApi.openWebSocket(webSocketCallback)
    }

    private fun initBiodata(callback: Callback) {
        var optionsMap = java.util.HashMap<String, Any?>()
        optionsMap["storage_settings"] = config.storageSettings?.body()
        optionsMap["bio_data_tolerance"] = config.biodataTolerance?.body()
        optionsMap["algorithm_params"] = config.algorithmParams?.body()
        mApi.initBiodataServices(config.availableBiodataBioOrAffectiveDataCategories!!, object : Callback {
            override fun onSuccess() {
                isInit = true
                if (config.mBiodataSubscribeParams != null) {
                    mApi.subscribeBioData(config.mBiodataSubscribeParams!!,
                        object : Callback2<RealtimeBioData> {
                            override fun onSuccess(t: cn.entertech.affective.sdk.bean.RealtimeBioData?) {
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
                                if (config.availableAffectiveBioOrAffectiveDataCategories == null) {
                                    callback.onSuccess()
                                }
                            }

                            override fun onError(error: Error?) {
                                callback.onError(error)
                            }

                        })
                } else if (config.availableAffectiveBioOrAffectiveDataCategories == null) {
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
        mApi.initAffectiveDataServices(config.availableAffectiveBioOrAffectiveDataCategories!!, object : Callback {
            override fun onSuccess() {
                if (config.mAffectiveSubscribeParams != null) {
                    mApi.subscribeAffectiveData(config.mAffectiveSubscribeParams!!,
                        object : Callback2<RealtimeAffectiveData> {
                            override fun onSuccess(t: cn.entertech.affective.sdk.bean.RealtimeAffectiveData?) {
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
        var affectiveBioOrAffectiveDataCategories = mutableListOf<BioOrAffectiveDataCategory>()
        if (subData.subAttentionFields != null) {
            affectiveBioOrAffectiveDataCategories.add(BioOrAffectiveDataCategory.ATTENTION)
        }
        if (subData.subRelaxationFields != null) {
            affectiveBioOrAffectiveDataCategories.add(BioOrAffectiveDataCategory.RELAXATION)
        }
        if (subData.subPressureFields != null) {
            affectiveBioOrAffectiveDataCategories.add(BioOrAffectiveDataCategory.PRESSURE)
        }
        if (subData.subPleasureFields != null) {
            affectiveBioOrAffectiveDataCategories.add(BioOrAffectiveDataCategory.PLEASURE)
        }
        if (subData.subArousalFields != null) {
            affectiveBioOrAffectiveDataCategories.add(BioOrAffectiveDataCategory.AROUSAL)
        }
        if (subData.subSleepFields != null) {
            affectiveBioOrAffectiveDataCategories.add(BioOrAffectiveDataCategory.SLEEP)
        }
        if (subData.subCoherenceFields != null) {
            affectiveBioOrAffectiveDataCategories.add(BioOrAffectiveDataCategory.COHERENCE)
        }
        if (subData.subFlowFields != null) {
            affectiveBioOrAffectiveDataCategories.add(BioOrAffectiveDataCategory.FLOW)
        }
        if (subData.subSsvepMultiClassifyFields != null) {
            affectiveBioOrAffectiveDataCategories.add(BioOrAffectiveDataCategory.SSVEP_MULTI_CLASSIFY)
        }
        config.availableAffectiveBioOrAffectiveDataCategories = affectiveBioOrAffectiveDataCategories
    }


    override fun isInited(): Boolean {
        return isInit
    }

    override fun getSessionId(): String? {
        return mApi.getSessionId()
    }

    private val mEnterWebSocketCallback by lazy {
        EnterWebSocketCallback()
    }

    override fun init(callback: Callback2<String>) {
        val cb=object :Callback{
            override fun onSuccess() {
                callback.onSuccess(mApi.getSessionId())
            }

            override fun onError(error: Error?) {
                callback.onError(error)
            }

            override fun log(msg: String) {
                callback.log(msg)
            }
        }
        mEnterWebSocketCallback.callback = cb
        mEnterWebSocketCallback.onOpen = {
            mApi.createSession(object : Callback2<String> {
                override fun onSuccess(t: String?) {
                    callback.onSuccess(t)
                    initBiodata(cb)
                    if (config.availableAffectiveBioOrAffectiveDataCategories != null) {
                        initAffective(cb)
                    }
                }

                override fun onError(error: Error?) {
                    callback.onError(error)
                    isInit = false
                }
            })
        }
        mEnterWebSocketCallback.onError = { e ->
            isInit = false
            e?.printStackTrace()
            callback.onError(Error(-1, e.toString()))
        }
        mEnterWebSocketCallback.onClose = {
            isInit = false
        }
        mApi.openWebSocket(mEnterWebSocketCallback)
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

    override fun addBiodataRealtimeListener(listener: (cn.entertech.affective.sdk.bean.RealtimeBioData?) -> Unit) {
        mBiodataRealtimeListener.add(listener)
    }

    override fun addAffectiveDataRealtimeListener(listener: (cn.entertech.affective.sdk.bean.RealtimeAffectiveData?) -> Unit) {
        mAffectiveRealtimeListener.add(listener)
    }

    override fun removeBiodataRealtimeListener(listener: (cn.entertech.affective.sdk.bean.RealtimeBioData?) -> Unit) {
        mBiodataRealtimeListener.remove(listener)
    }

    override fun removeAffectiveRealtimeListener(listener: (cn.entertech.affective.sdk.bean.RealtimeAffectiveData?) -> Unit) {
        mAffectiveRealtimeListener.remove(listener)
    }

    override fun getBiodataReport(callback: Callback2<HashMap<Any, Any?>>) {
        mApi.getBiodataReport(config.availableBiodataBioOrAffectiveDataCategories!!, callback)
    }

    override fun getAffectiveDataReport(callback: Callback2<HashMap<Any, Any?>>) {
        mApi.getAffectivedataReport(config.availableAffectiveBioOrAffectiveDataCategories!!, callback)
    }

    override fun restore(callback: Callback) {
        if (mApi.isWebSocketOpen()) {
            mApi.restore(object : Callback {
                override fun onSuccess() {
                    initBiodata(callback)
                    if (config.availableAffectiveBioOrAffectiveDataCategories != null) {
                        initAffective(callback)
                    }
                }

                override fun onError(error: Error?) {
                    callback.onError(error)
                    isInit = false
                }
            })
        } else {
            mEnterWebSocketCallback.callback = callback
            mEnterWebSocketCallback.onOpen = {
                mApi.restore(object : Callback {
                    override fun onSuccess() {
                        initBiodata(callback)
                        if (config.availableAffectiveBioOrAffectiveDataCategories != null) {
                            initAffective(callback)
                        }
                    }

                    override fun onError(error: Error?) {
                        isInit = false
                        callback.onError(error)
                    }
                })
            }
            mEnterWebSocketCallback.onClose = {
                isInit = false
            }
            mEnterWebSocketCallback.onError = { e ->
                isInit = false
                e?.printStackTrace()
                callback.onError(Error(-1, e.toString()))
            }

            mApi.openWebSocket(mEnterWebSocketCallback)
        }
    }

    override fun release(callback: Callback) {
        if (config.availableAffectiveBioOrAffectiveDataCategories != null) {
            mApi.finishAffectiveDataServices(
                config.availableAffectiveBioOrAffectiveDataCategories!!,
                object : Callback {
                    override fun onSuccess() {
                        mApi.destroySessionAndCloseWebSocket(object : Callback {
                            override fun onSuccess() {
                                callback.onSuccess()
                            }

                            override fun onError(error: Error?) {
                                callback.onError(error)
                            }
                        })
                    }

                    override fun onError(error: Error?) {
                        callback.onError(error)
                    }

                })
        }
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
        mApi.closeWebSocket()
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