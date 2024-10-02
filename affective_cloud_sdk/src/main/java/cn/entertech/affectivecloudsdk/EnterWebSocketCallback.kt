package cn.entertech.affectivecloudsdk

import cn.entertech.affective.sdk.utils.AffectiveLogHelper
import cn.entertech.affectivecloudsdk.interfaces.WebSocketCallback
import org.java_websocket.WebSocket
import org.java_websocket.enums.ReadyState
import org.java_websocket.handshake.ServerHandshake
import java.util.concurrent.TimeUnit

class EnterWebSocketCallback : WebSocketCallback {

    companion object {
        private const val TIME = 1000
        private const val TAG = "EnterWebSocketCallback"
    }


    var onOpen: (() -> Unit)? = null
    var onClose: (() -> Unit)? = null
    var onError: ((Exception?) -> Unit)? = null

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
        if (isClosed) {
            AffectiveLogHelper.i(TAG, "EnterWebSocketCallback: isClosed $isClosed")
        }
        return super.isClosed(isClosed)
    }

    override fun getReadyState(readyState: ReadyState): ReadyState {
        AffectiveLogHelper.i(TAG, "EnterWebSocketCallback: getReadyState $readyState")
        return super.getReadyState(readyState)
    }

    override fun reconnectBlocking(reconnectBlocking: Boolean): Boolean {
        AffectiveLogHelper.i(TAG, "EnterWebSocketCallback: reconnectBlocking $reconnectBlocking")
        return super.reconnectBlocking(reconnectBlocking)
    }

    override fun reconnect() {
        AffectiveLogHelper.i(TAG, "EnterWebSocketCallback: reconnect ")
        super.reconnect()
    }

    override fun connect() {
        AffectiveLogHelper.i(TAG, "EnterWebSocketCallback: connect ")
        super.connect()
    }

    override fun connectBlocking(connectBlocking: Boolean): Boolean {
        AffectiveLogHelper.i(TAG, "EnterWebSocketCallback: connectBlocking $connectBlocking")
        return super.connectBlocking(connectBlocking)
    }

    override fun connectBlocking(
        timeout: Long,
        timeUnit: TimeUnit?,
        connectBlocking: Boolean
    ): Boolean {
        AffectiveLogHelper.i(
            TAG, "EnterWebSocketCallback: connectBlocking timeout $timeout," +
                    " timeUnit, connectBlocking $connectBlocking"
        )
        return super.connectBlocking(timeout, timeUnit, connectBlocking)
    }

    override fun closeBlocking() {
        AffectiveLogHelper.i(TAG, "EnterWebSocketCallback: closeBlocking ")
        super.closeBlocking()
    }

    override fun onCloseInitiated(code: Int, reason: String?) {
        AffectiveLogHelper.i(
            TAG,
            "EnterWebSocketCallback: onCloseInitiated code $code, reason $reason"
        )
        super.onCloseInitiated(code, reason)
    }

    override fun onClosing(code: Int, reason: String?, remote: Boolean) {
        AffectiveLogHelper.i(TAG, "EnterWebSocketCallback: onClosing ")
        super.onClosing(code, reason, remote)
    }

    override fun isClosing(isClosing: Boolean): Boolean {
        if (isClosing) {
            AffectiveLogHelper.i(TAG, "EnterWebSocketCallback: isClosing $isClosing ")
        }
        return super.isClosing(isClosing)
    }

    override fun isOpen(isOpen: Boolean): Boolean {
        if (!isOpen) {
            AffectiveLogHelper.i(TAG, "EnterWebSocketCallback: isOpen $isOpen ")
        }
        return super.isOpen(isOpen)
    }

    override fun onWebsocketClosing(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        AffectiveLogHelper.i(
            TAG,
            "EnterWebSocketCallback: onWebsocketClosing code $code reason $reason"
        )
        super.onWebsocketClosing(conn, code, reason, remote)
    }

    override fun close() {
        AffectiveLogHelper.i(TAG, "EnterWebSocketCallback: close ")
        super.close()
    }

    override fun close(code: Int) {
        AffectiveLogHelper.i(TAG, "EnterWebSocketCallback: close $code ")
        super.close(code)
    }

    override fun close(code: Int, message: String?) {
        AffectiveLogHelper.i(TAG, "EnterWebSocketCallback: close code $code message $message")
        super.close(code, message)
    }

    override fun closeConnection(code: Int, message: String?) {
        AffectiveLogHelper.i(
            TAG,
            "EnterWebSocketCallback: closeConnection code $code message $message"
        )
        super.closeConnection(code, message)
    }

    override fun onWebsocketCloseInitiated(conn: WebSocket?, code: Int, reason: String?) {
        AffectiveLogHelper.i(TAG, "EnterWebSocketCallback: reconnect ")
        super.onWebsocketCloseInitiated(conn, code, reason)
    }

    override fun isFlushAndClose(isFlushAndClose: Boolean): Boolean {
        AffectiveLogHelper.i(TAG, "EnterWebSocketCallback: isFlushAndClose ")
        return super.isFlushAndClose(isFlushAndClose)
    }
}