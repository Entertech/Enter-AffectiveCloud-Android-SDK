package cn.entertech.affectivecloudsdk

import android.util.Log
import cn.entertech.affectivecloudsdk.interfaces.Callback2
import cn.entertech.affectivecloudsdk.interfaces.IWebSocketHelper
import cn.entertech.affectivecloudsdk.interfaces.WebSocketCallback
import cn.entertech.affectivecloudsdk.utils.ConvertUtil
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer
import java.util.concurrent.CopyOnWriteArrayList

class WebSocketHelper(var address: String, var timeout: Int = 10000) : IWebSocketHelper {
    private var mBrainDataWebSocket: WebSocketClient? = null

    var mOpenCallback: WebSocketCallback? = null
    var messageResponseListeners = CopyOnWriteArrayList<(String) -> Unit>()
    var rawJsonRequestListeners = CopyOnWriteArrayList<(String) -> Unit>()
    var rawJsonResponseListeners = CopyOnWriteArrayList<(String) -> Unit>()
    var connectListeners = CopyOnWriteArrayList<() -> Unit>()
    var disconnectListeners = CopyOnWriteArrayList<() -> Unit>()

    override fun open(webSocketCallback: WebSocketCallback) {
        try {
            this.mOpenCallback = webSocketCallback
            mBrainDataWebSocket = object : WebSocketClient(URI(address), Draft_6455(), null, timeout) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    Log.d("WebSocketHelper", "onConnected " + handshakedata.toString())
                    mOpenCallback?.onOpen(handshakedata)
                    connectListeners?.forEach {
                        it.invoke()
                    }
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    mOpenCallback?.onClose(code, reason, remote)
                    disconnectListeners.forEach {
                        it.invoke()
                    }
                    Log.d("WebSocketHelper", "onClose :$code::reason is $reason")
                }

                override fun onMessage(message: String?) {
                }

                override fun onMessage(message: ByteBuffer) {
                    val arr = ByteArray(message.remaining())
                    message.get(arr)
                    Log.d("WebSocketHelper", "receive msg is " + ConvertUtil.uncompress(arr))
                    var msg = ConvertUtil.uncompress(arr)
                    rawJsonResponseListeners?.forEach {
                        it.invoke(msg)
                    }
                }

                override fun onError(ex: java.lang.Exception?) {
                    mOpenCallback?.onError(ex)
                    Log.d("WebSocketHelper", "onError " + ex.toString())
                }
            }
            mBrainDataWebSocket!!.connect()
        } catch (e: Exception) {
            mOpenCallback?.onError(e)
        }

    }

    override fun addMessageResponseListener(listener: (String) -> Unit) {
        messageResponseListeners.add(listener)
    }

    override fun addConnectListener(listener: () -> Unit) {
        connectListeners.add(listener)
    }

    override fun addDisconnectListener(listener: () -> Unit) {
        disconnectListeners.add(listener)
    }

    override fun removeConnectListener(listener: () -> Unit) {
        connectListeners.remove(listener)
    }

    override fun removeDisconnectListener(listener: () -> Unit) {
        disconnectListeners.remove(listener)
    }

    fun sendMessage(jsonData: String) {
        rawJsonRequestListeners.forEach {
            it.invoke(jsonData)
        }
        sendMessage(ConvertUtil.compress(jsonData))
    }

    override fun sendMessage(data: ByteArray) {
        if (isOpen()){
            mBrainDataWebSocket?.send(data)
        }
    }

    override fun isOpen(): Boolean {
        if (mBrainDataWebSocket == null) {
            return false
        }
        return mBrainDataWebSocket!!.isOpen
    }

    override fun close() {
        mBrainDataWebSocket?.close()
    }

    override fun closeConnection(code: Int, message: String) {
        mBrainDataWebSocket?.closeConnection(code, message)
     }

    fun addRawJsonRequestListener(listener: (String) -> Unit) {
        this.rawJsonRequestListeners.add(listener)
    }

    fun addRawJsonResponseListener(listener: (String) -> Unit) {
        this.rawJsonResponseListeners.add(listener)
    }

    fun removeRawJsonRequestListener(listener: (String) -> Unit) {
        this.rawJsonRequestListeners.remove(listener)
    }

    fun removeRawJsonResponseListener(listener: (String) -> Unit) {
        this.rawJsonResponseListeners.remove(listener)
    }
}