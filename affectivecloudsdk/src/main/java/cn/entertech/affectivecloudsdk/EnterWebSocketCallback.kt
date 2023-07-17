package cn.entertech.affectivecloudsdk

import cn.entertech.affectivecloudsdk.interfaces.Callback
import cn.entertech.affectivecloudsdk.interfaces.WebSocketCallback
import org.java_websocket.WebSocket
import org.java_websocket.enums.ReadyState
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.util.concurrent.TimeUnit

class EnterWebSocketCallback : WebSocketCallback {

    companion object{
        private const val TIME=1000
    }

    private val map by lazy {

    }

    var onOpen: (() -> Unit)? = null
    var onClose: (() -> Unit)? = null
    var onError: ((Exception?) -> Unit)? = null
    var callback: Callback? = null

    override fun onOpen(serverHandshake: ServerHandshake?) {
        onOpen?.invoke()
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        onClose?.invoke()
    }

    override fun onError(e: Exception?) {
        onError?.invoke(e)
    }

    override fun isClosed(isClosed: Boolean): Boolean {
        if(isClosed) {
            callback?.log("EnterWebSocketCallback: isClosed $isClosed")
        }
        return super.isClosed(isClosed)
    }

    override fun getReadyState(readyState: ReadyState): ReadyState {
        callback?.log("EnterWebSocketCallback: getReadyState $readyState")
        return super.getReadyState(readyState)
    }

    override fun reconnectBlocking(reconnectBlocking: Boolean): Boolean {
        callback?.log("EnterWebSocketCallback: reconnectBlocking $reconnectBlocking")
        return super.reconnectBlocking(reconnectBlocking)
    }

    override fun reconnect() {
        callback?.log("EnterWebSocketCallback: reconnect ")
        super.reconnect()
    }

    override fun connect() {
        callback?.log("EnterWebSocketCallback: connect ")
        super.connect()
    }

    override fun connectBlocking(connectBlocking: Boolean): Boolean {
        callback?.log("EnterWebSocketCallback: connectBlocking $connectBlocking")
        return super.connectBlocking(connectBlocking)
    }

    override fun connectBlocking(
        timeout: Long,
        timeUnit: TimeUnit?,
        connectBlocking: Boolean
    ): Boolean {
        callback?.log("EnterWebSocketCallback: connectBlocking timeout $timeout," +
                " timeUnit, connectBlocking $connectBlocking")
        return super.connectBlocking(timeout, timeUnit, connectBlocking)
    }

    override fun closeBlocking() {
        callback?.log("EnterWebSocketCallback: closeBlocking ")
        super.closeBlocking()
    }

    override fun onCloseInitiated(code: Int, reason: String?) {
        callback?.log("EnterWebSocketCallback: onCloseInitiated code $code, reason $reason")
        super.onCloseInitiated(code, reason)
    }

    override fun onClosing(code: Int, reason: String?, remote: Boolean) {
        callback?.log("EnterWebSocketCallback: onClosing ")
        super.onClosing(code, reason, remote)
    }

    override fun isClosing(isClosing: Boolean): Boolean {
        if(isClosing) {
            callback?.log("EnterWebSocketCallback: isClosing $isClosing ")
        }
        return super.isClosing(isClosing)
    }

    override fun isOpen(isOpen: Boolean): Boolean {
        if(!isOpen) {
            callback?.log("EnterWebSocketCallback: isOpen $isOpen ")
        }
        return super.isOpen(isOpen)
    }

    override fun onWebsocketClosing(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        callback?.log("EnterWebSocketCallback: onWebsocketClosing code $code reason $reason")
        super.onWebsocketClosing(conn, code, reason, remote)
    }

    override fun close() {
        callback?.log("EnterWebSocketCallback: close ")
        super.close()
    }

    override fun close(code: Int) {
        callback?.log("EnterWebSocketCallback: close $code ")
        super.close(code)
    }

    override fun close(code: Int, message: String?) {
        callback?.log("EnterWebSocketCallback: close code $code message $message")
        super.close(code, message)
    }

    override fun closeConnection(code: Int, message: String?) {
        callback?.log("EnterWebSocketCallback: closeConnection code $code message $message")
        super.closeConnection(code, message)
    }

    override fun onWebsocketCloseInitiated(conn: WebSocket?, code: Int, reason: String?) {
        callback?.log("EnterWebSocketCallback: reconnect ")
        super.onWebsocketCloseInitiated(conn, code, reason)
    }

    override fun isFlushAndClose(isFlushAndClose: Boolean): Boolean {
        callback?.log("EnterWebSocketCallback: isFlushAndClose ")
        return super.isFlushAndClose(isFlushAndClose)
    }
}