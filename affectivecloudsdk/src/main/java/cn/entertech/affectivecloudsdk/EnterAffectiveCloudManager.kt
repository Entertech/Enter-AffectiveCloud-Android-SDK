package cn.entertech.affectivecloudsdk

import android.util.Log
import cn.entertech.affectivecloudsdk.entity.*
import cn.entertech.affectivecloudsdk.interfaces.*
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.lang.IllegalStateException
import java.util.concurrent.CopyOnWriteArrayList

class EnterAffectiveCloudManager(var config: EnterAffectiveCloudConfig) :
    IEnterAffectiveCloudManager {
    var mApi: BaseApi
    var mBiodataRealtimeListener = CopyOnWriteArrayList<(RealtimeBioData?) -> Unit>()
    var mAffectiveRealtimeListener = CopyOnWriteArrayList<(RealtimeAffectiveData?) -> Unit>()

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

    var isInit = false

    override fun isInited(): Boolean {
        return isInit
    }


    override fun init(callback: Callback) {
        mApi.openWebSocket(object : WebSocketCallback {
            override fun onOpen(serverHandshake: ServerHandshake?) {
                mApi.createSession(object : Callback2<String> {
                    override fun onSuccess(t: String?) {
                        isInit = true
                        initBiodata(callback)
                        if (config.availableAffectiveServices != null) {
                            initAffective(callback)
                        }
                    }

                    override fun onError(error: Error?) {
                        callback.onError(error)
                    }
                })
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                if (reason != null && reason != "") {
                    callback.onError(Error(-1, reason.toString()))
                }
            }

            override fun onError(e: Exception?) {
                e?.printStackTrace()
                callback.onError(Error(-1, e.toString()))
            }

        })
    }

    override fun appendEEGData(bytes: ByteArray, triggerCount: Int) {
        mApi.appendEEGData(bytes)
    }

    override fun appendHeartRateData(heartRateData: Int, triggerCount: Int) {
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
                            callback.onError(error)
                        }
                    })
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    if (reason != null && reason != "") {
                        callback.onError(Error(-1, reason.toString()))
                    }
                }

                override fun onError(e: Exception?) {
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

    override fun addWebSocketDisconnectListener(listener: () -> Unit) {
        mApi.addDisconnectListener(listener)
    }

    override fun removeWebSocketConnectListener(listener: () -> Unit) {
        mApi.removeConnectListener(listener)
    }

    override fun removeWebSocketDisconnectListener(listener: () -> Unit) {
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