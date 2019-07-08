package cn.entertech.biomoduledemo.websocket

import android.util.Log
import cn.entertech.biomoduledemo.utils.ConvertUtil
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer
import java.util.concurrent.CopyOnWriteArrayList

class WebSocketManager{
    var mBrainDataWebSocket: WebSocketClient? = null
    //    测试服
    var url: URI = URI("wss://server.affectivecloud.com/ws/algorithm/v0.1/")
//    var url: URI = URI("wss://server.affectivecloud.cn/ws/algorithm/v0.1/")
    var receiveDataCallback = CopyOnWriteArrayList<(String?) -> Unit>()

    companion object {
        private var socketManager: WebSocketManager? = null
        fun getInstance(): WebSocketManager {
            if (socketManager == null) {
                synchronized(WebSocketManager::class.java) {
                    if (socketManager == null) {
                        socketManager =
                            WebSocketManager()
                    }
                }
            }
            return socketManager!!
        }
    }

    fun connect(connectedCallback: ((ServerHandshake?) -> Unit)?) {
        try {
            mBrainDataWebSocket = object : WebSocketClient(url, Draft_6455(), null, 10000) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    Log.d("WebSocketManager","onConnected " + handshakedata.toString())
                    connectedCallback?.invoke(handshakedata)
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {

                    Log.d("WebSocketManager","onClose :" + code + "::reason is " + reason)
                }

                override fun onMessage(message: String?) {
                }

                override fun onMessage(message: ByteBuffer) {
                    val arr = ByteArray(message.remaining())
                    message.get(arr)
                    Log.d("WebSocketManager","receive msg is " + ConvertUtil.uncompress(arr))
                    var recMsg = ConvertUtil.uncompress(arr)
                    receiveDataCallback.forEach {
                        it.invoke(recMsg)
                    }
                }

                override fun onError(ex: java.lang.Exception?) {
                    Log.d("WebSocketManager","onError " + ex.toString())
                }
            }
            mBrainDataWebSocket!!.connect()
        } catch (e: Exception) {
            Log.d("####", "on connect error$e")
        }
    }

    fun isOpen():Boolean{
        return if (mBrainDataWebSocket!= null){
            mBrainDataWebSocket!!.isOpen
        }else{
            false
        }
    }

    fun connect() {
        connect(null)
    }


    fun sendMessage(data: String) {
        mBrainDataWebSocket?.send(data + "\r\n")
    }

    fun sendMessage(data: ByteArray) {

        com.orhanobut.logger.Logger.d("web socket send is "+data.toString())
        mBrainDataWebSocket?.send(data)
    }


    fun addReceiveDataListener(callback: ((String?) -> Unit)) {
        receiveDataCallback.add(callback)
    }

    fun removeReceiveDataListener(callback: ((String) -> Unit)) {
        receiveDataCallback.remove(callback)
    }


    fun close() {
        mBrainDataWebSocket?.close()

    }

}