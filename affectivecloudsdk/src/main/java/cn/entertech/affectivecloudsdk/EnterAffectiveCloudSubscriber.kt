package cn.entertech.affectivecloudsdk

import cn.entertech.affectivecloudsdk.entity.Error
import cn.entertech.affectivecloudsdk.entity.RealtimeAffectiveData
import cn.entertech.affectivecloudsdk.entity.RealtimeBioData
import cn.entertech.affectivecloudsdk.interfaces.BaseApi
import cn.entertech.affectivecloudsdk.interfaces.Callback
import cn.entertech.affectivecloudsdk.interfaces.Callback2
import cn.entertech.affectivecloudsdk.interfaces.WebSocketCallback
import org.java_websocket.handshake.ServerHandshake
import java.util.concurrent.CopyOnWriteArrayList

class EnterAffectiveCloudSubscriber(var url: String, var timeout: Int = 10000) {
    var mApi: BaseApi
    var mBiodataRealtimeListener = CopyOnWriteArrayList<(RealtimeBioData?) -> Unit>()
    var mAffectiveRealtimeListener = CopyOnWriteArrayList<(RealtimeAffectiveData?) -> Unit>()


    init {
        mApi = EnterAffectiveCloudApiImpl(url, timeout)
    }

    var isInit = false
    fun isInited(): Boolean {
        return isInit
    }


    fun init(callback: Callback) {
        mApi.openWebSocket(object : WebSocketCallback {
            override fun onOpen(serverHandshake: ServerHandshake?) {
                isInit = true
                addRealtimeDataCallback()
                callback.onSuccess()
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
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

    fun addRealtimeDataCallback() {
        mApi.addBioDataCallback(object:Callback2<RealtimeBioData>{
            override fun onSuccess(t: RealtimeBioData?) {
                mBiodataRealtimeListener.forEach {
                    it.invoke(t)
                }
            }

            override fun onError(error: Error?) {

            }

        })
        mApi.addAffectiveDataCallback(object:Callback2<RealtimeAffectiveData>{
            override fun onSuccess(t: RealtimeAffectiveData?) {
                mAffectiveRealtimeListener.forEach {
                    it.invoke(t)
                }
            }

            override fun onError(error: Error?) {

            }

        })
    }

    fun addBiodataRealtimeListener(listener: (RealtimeBioData?) -> Unit) {
        mBiodataRealtimeListener.add(listener)
    }

    fun addAffectiveDataRealtimeListener(listener: (RealtimeAffectiveData?) -> Unit) {
        mAffectiveRealtimeListener.add(listener)
    }

    fun removeBiodataRealtimeListener(listener: (RealtimeBioData?) -> Unit) {
        mBiodataRealtimeListener.remove(listener)
    }

    fun removeAffectiveRealtimeListener(listener: (RealtimeAffectiveData?) -> Unit) {
        mAffectiveRealtimeListener.remove(listener)
    }

    fun addWebSocketConnectListener(listener: () -> Unit) {
        mApi.addConnectListener(listener)
    }

    fun addWebSocketDisconnectListener(listener: (String) -> Unit) {
        mApi.addDisconnectListener(listener)
    }

    fun removeWebSocketConnectListener(listener: () -> Unit) {
        mApi.removeConnectListener(listener)
    }

    fun removeWebSocketDisconnectListener(listener: (String) -> Unit) {
        mApi.removeDisconnectListener(listener)
    }

    fun addRawJsonRequestListener(listener: (String) -> Unit) {
        mApi.addRawJsonRequestListener(listener)
    }

    fun addRawJsonResponseListener(listener: (String) -> Unit) {
        mApi.addRawJsonResponseListener(listener)
    }

    fun removeRawJsonRequestListener(listener: (String) -> Unit) {
        mApi.removeRawJsonRequestListener(listener)
    }

    fun removeRawJsonResponseListener(listener: (String) -> Unit) {
        mApi.removeRawJsonResponseListener(listener)
    }

    fun isWebSocketOpen(): Boolean {
        return mApi.isWebSocketOpen()
    }

    fun closeWebSocket() {
        mApi.closeWebSocket()
    }

    fun closeConnection(code: Int, message: String) {
        mApi.closeConnection(code, message)
    }
//
//    fun submit(remark: List<RecData>, callback: Callback) {
//        mApi.submit(remark, callback)
//    }
}