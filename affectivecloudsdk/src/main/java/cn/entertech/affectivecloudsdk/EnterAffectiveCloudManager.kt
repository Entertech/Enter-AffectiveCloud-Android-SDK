package cn.entertech.affectivecloudsdk

import cn.entertech.affectivecloudsdk.entity.*
import cn.entertech.affectivecloudsdk.interfaces.*
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.lang.IllegalStateException
import java.util.concurrent.CopyOnWriteArrayList

class EnterAffectiveCloudManager(var config: EnterAffectiveCloudConfig) : IEnterAffectiveCloudManager {
    var mApi: BaseApi
    var mBiodataRealtimeListener = CopyOnWriteArrayList<(RealtimeBioData?) -> Unit>()
    var mAffectiveRealtimeListener = CopyOnWriteArrayList<(RealtimeAffectiveData?) -> Unit>()

    init {
        mApi = EnterAffectiveCloudApiImpl(
            config.uri!!,
            config.websocketTimeout!!,
            config.appKey!!,
            config.appSecret!!,
            config.userName!!,
            config.userId!!
        )
        if (config.availableBiodataServices == null) {
            throw IllegalStateException("biodata services must not be null")
        }
    }

    override fun openWebSocket(webSocketCallback: WebSocketCallback) {
        mApi.openWebSocket(webSocketCallback)
    }

    private fun initBiodata(callback: Callback) {
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
        })
    }

    private fun initAffective(callback: Callback) {
        mApi.startAffectiveServices(config.availableAffectiveServices!!, object : Callback {
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
                    }
                })
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                if (reason != null && reason != ""){
                    callback.onError(Error(-1, reason.toString()))
                }
            }

            override fun onError(e: Exception?) {
                e?.printStackTrace()
                callback.onError(Error(-1, e.toString()))
            }

        })
    }

    override fun appendBrainData(bytes: ByteArray, triggerCount: Int) {
        mApi.appendBrainData(bytes)
    }

    override fun appendHeartRateData(heartRateData: Int, triggerCount: Int) {
        mApi.appendHeartData(heartRateData)
    }

    override fun addBiodataRealtimeListener(listener: (RealtimeBioData?) -> Unit) {
        mBiodataRealtimeListener.add(listener)
    }

    override fun addAffectiveRealtimeListener(listener: (RealtimeAffectiveData?) -> Unit) {
        mAffectiveRealtimeListener.add(listener)
    }

    override fun removeBiodataRealtimeListener(listener: (RealtimeBioData?) -> Unit) {
        mBiodataRealtimeListener.remove(listener)
    }

    override fun removeAffectiveRealtimeListener(listener: (RealtimeAffectiveData?) -> Unit) {
        mAffectiveRealtimeListener.remove(listener)
    }

    override fun reportBiodata(callback: Callback2<HashMap<Any, Any?>>) {
        mApi.reportBiodata(config.availableBiodataServices!!, callback)
    }

    override fun reportAffective(callback: Callback2<HashMap<Any, Any?>>) {
        mApi.reportAffective(config.availableAffectiveServices!!, callback)
    }

    override fun restore(callback: Callback) {
        if (mApi.isWebSocketOpen()){
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
        }else{
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
                    if (reason != null && reason != ""){
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
            mApi.finishAffectiveServices(config.availableAffectiveServices!!, object : Callback {
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

}