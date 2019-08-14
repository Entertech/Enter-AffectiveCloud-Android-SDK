package cn.entertech.affectivecloudsdk.interfaces

import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception

interface IWebSocketHelper {
    fun open(webSocketCallback: WebSocketCallback)
    fun addMessageResponseListener(listener: ((String) -> Unit))
    fun addConnectListener(listener: (() -> Unit))
    fun addDisconnectListener(listener: (() -> Unit))
    fun removeConnectListener(listener: (() -> Unit))
    fun removeDisconnectListener(listener: (() -> Unit))
    fun sendMessage(data: ByteArray)
    fun isOpen(): Boolean
    fun close()
}