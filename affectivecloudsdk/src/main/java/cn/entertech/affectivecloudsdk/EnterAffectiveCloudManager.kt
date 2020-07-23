package cn.entertech.affectivecloudsdk

import android.util.Log
import cn.entertech.affectivecloudsdk.entity.*
import cn.entertech.affectivecloudsdk.interfaces.*
import com.google.gson.annotations.SerializedName
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.lang.IllegalStateException
import java.util.concurrent.CopyOnWriteArrayList

class EnterAffectiveCloudManager(var config: EnterAffectiveCloudConfig) :
    IEnterAffectiveCloudManager {
    var mApi: BaseApi
    var mBiodataRealtimeListener = CopyOnWriteArrayList<(RealtimeBioData?) -> Unit>()
    var mAffectiveRealtimeListener = CopyOnWriteArrayList<(RealtimeAffectiveData?) -> Unit>()

    companion object {
        const val DEFAULT_UPLOAD_EEG_PACKAGE_COUNT = 30
        const val DEFAULT_UPLOAD_HR_PACKAGE_COUNT = 2
        const val BASE_UPLOAD_EEG_PACKAGE_COUNT = 50
        const val BASE_UPLOAD_HR_PACKAGE_COUNT = 3
        const val EEG_PACKAGE_LENGTH = 20
        const val HR_PACKAGE_LENGTH = 1
    }

    var uploadEEGTriggerCount = DEFAULT_UPLOAD_EEG_PACKAGE_COUNT * EEG_PACKAGE_LENGTH
    var uploadHRTriggerCount = DEFAULT_UPLOAD_HR_PACKAGE_COUNT * HR_PACKAGE_LENGTH

    init {
        if (config.websocketTimeout == null) {
            mApi = EnterAffectiveCloudApiImpl(
                config.uri!!,
                config.appKey!!,
                config.appSecret!!,
                config.userId!!
            )
        } else {
            mApi = EnterAffectiveCloudApiImpl(
                config.uri!!,
                config.websocketTimeout!!,
                config.appKey!!,
                config.appSecret!!,
                config.userId!!
            )
        }
        if (config.uploadCycle != 0) {
            uploadEEGTriggerCount =
                BASE_UPLOAD_EEG_PACKAGE_COUNT * EEG_PACKAGE_LENGTH * config.uploadCycle
            uploadHRTriggerCount =
                BASE_UPLOAD_HR_PACKAGE_COUNT * HR_PACKAGE_LENGTH * config.uploadCycle
        }
        if (config.availableBiodataServices == null) {
            throw IllegalStateException("biodata services must not be null")
        }
    }

    override fun openWebSocket(webSocketCallback: WebSocketCallback) {
        mApi.openWebSocket(webSocketCallback)
    }

    private fun initBiodata(callback: Callback) {
        var optionsMap = java.util.HashMap<String, Any?>()
        optionsMap["storage_settings"] = config.storageSettings?.body()
        optionsMap["bio_data_tolerance"] = config.biodataTolerance?.body()
        mApi.initBiodataServices(config.availableBiodataServices!!, object : Callback {
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
                                if (config.availableAffectiveServices == null) {
                                    callback.onSuccess()
                                }
                            }

                            override fun onError(error: Error?) {
                                callback.onError(error)
                            }

                        })
                } else if (config.availableAffectiveServices == null) {
                    callback.onSuccess()
                }
            }

            override fun onError(error: Error?) {
                isInit = true
                callback.onError(error)
            }
        }, optionsMap)
    }

    private fun initAffective(callback: Callback) {
        mApi.initAffectiveDataServices(config.availableAffectiveServices!!, object : Callback {
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

    fun selectAvailableAffectiveServicesInRemote(subData: SubAffectiveDataFields) {
        var affectiveServices = mutableListOf<Service>()
        if (subData.subAttentionFields != null) {
            affectiveServices.add(Service.ATTENTION)
        }
        if (subData.subRelaxationFields != null) {
            affectiveServices.add(Service.RELAXATION)
        }
        if (subData.subPressureFields != null) {
            affectiveServices.add(Service.PRESSURE)
        }
        if (subData.subPleasureFields != null) {
            affectiveServices.add(Service.PLEASURE)
        }
        if (subData.subArousalFields != null) {
            affectiveServices.add(Service.AROUSAL)
        }
        if (subData.subSleepFields != null) {
            affectiveServices.add(Service.SLEEP)
        }
        if (subData.subCoherenceFields != null) {
            affectiveServices.add(Service.COHERENCE)
        }
        config.availableAffectiveServices = affectiveServices
    }

    var isInit = false

    override fun isInited(): Boolean {
        return isInit
    }


    override fun init(callback: Callback) {
        mApi.openWebSocket(object : WebSocketCallback {
            override fun onOpen(serverHandshake: ServerHandshake?) {
                mApi.createSession(object : Callback2<String> {
                    override fun onSuccess(t: String?) {
                        initBiodata(callback)
                        if (config.availableAffectiveServices != null) {
                            initAffective(callback)
                        }
                    }

                    override fun onError(error: Error?) {
                        callback.onError(error)
                        isInit = false
                    }
                })
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                isInit = false
                if (reason != null && reason != "") {
                    callback.onError(Error(-1, reason.toString()))
                }
            }

            override fun onError(e: Exception?) {
                isInit = false
                e?.printStackTrace()
                callback.onError(Error(-1, e.toString()))
            }

        })
    }

    override fun appendEEGData(bytes: ByteArray) {
        mApi.appendEEGData(bytes, uploadEEGTriggerCount)
    }

    override fun appendHeartRateData(heartRateData: Int) {
        mApi.appendHeartData(heartRateData, uploadHRTriggerCount)
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
        mApi.getBiodataReport(config.availableBiodataServices!!, callback)
    }

    override fun getAffectiveDataReport(callback: Callback2<HashMap<Any, Any?>>) {
        mApi.getAffectivedataReport(config.availableAffectiveServices!!, callback)
    }

    override fun restore(callback: Callback) {
        if (mApi.isWebSocketOpen()) {
            mApi.restore(object : Callback {
                override fun onSuccess() {
                    initBiodata(callback)
                    if (config.availableAffectiveServices != null) {
                        initAffective(callback)
                    }
                }

                override fun onError(error: Error?) {
                    callback.onError(error)
                    isInit = false
                }
            })
        } else {
            mApi.openWebSocket(object : WebSocketCallback {
                override fun onOpen(serverHandshake: ServerHandshake?) {
                    mApi.restore(object : Callback {
                        override fun onSuccess() {
                            initBiodata(callback)
                            if (config.availableAffectiveServices != null) {
                                initAffective(callback)
                            }
                        }

                        override fun onError(error: Error?) {
                            isInit = false
                            callback.onError(error)
                        }
                    })
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    isInit = false
                    if (reason != null && reason != "") {
                        callback.onError(Error(-1, reason.toString()))
                    }
                }

                override fun onError(e: Exception?) {
                    isInit = false
                    e?.printStackTrace()
                    callback.onError(Error(-1, e.toString()))
                }

            })
        }
    }

    override fun release(callback: Callback) {
        if (config.availableAffectiveServices != null) {
            mApi.finishAffectiveDataServices(
                config.availableAffectiveServices!!,
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
        mApi.addDisconnectListener(listener)
    }

    override fun removeWebSocketConnectListener(listener: () -> Unit) {
        mApi.removeConnectListener(listener)
    }

    override fun removeWebSocketDisconnectListener(listener: (String) -> Unit) {
        mApi.removeDisconnectListener(listener)
    }

    override fun addRawJsonRequestListener(listener: (String) -> Unit) {
        mApi?.addRawJsonRequestListener(listener)
    }

    override fun addRawJsonResponseListener(listener: (String) -> Unit) {
        mApi?.addRawJsonResponseListener(listener)
    }

    override fun removeRawJsonRequestListener(listener: (String) -> Unit) {
        mApi?.removeRawJsonRequestListener(listener)
    }

    override fun removeRawJsonResponseListener(listener: (String) -> Unit) {
        mApi?.removeRawJsonResponseListener(listener)
    }

    override fun isWebSocketOpen(): Boolean {
        if (mApi == null) {
            return false
        }
        return mApi!!.isWebSocketOpen()
    }

    override fun closeWebSocket() {
        mApi?.closeWebSocket()
    }

    override fun closeConnection(code: Int, message: String) {
        mApi?.closeConnection(code, message)
    }

    override fun submit(remark: List<RecData>, callback: Callback) {
        mApi?.submit(remark, callback)
    }
}