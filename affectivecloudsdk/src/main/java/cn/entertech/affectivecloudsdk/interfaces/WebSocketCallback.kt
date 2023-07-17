package cn.entertech.affectivecloudsdk.interfaces

import org.java_websocket.WebSocket
import org.java_websocket.enums.ReadyState
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.util.concurrent.TimeUnit

interface WebSocketCallback {
    fun onOpen(serverHandshake: ServerHandshake?)
    fun onClose(code: Int, reason: String?, remote: Boolean)
    fun onError(e: Exception?)

    fun isClosed(isClosed: Boolean): Boolean {
        return isClosed
    }

    fun getReadyState(readyState: ReadyState): ReadyState {
        return readyState
    }

    fun reconnectBlocking(reconnectBlocking: Boolean): Boolean {
        return reconnectBlocking
    }

    fun reconnect() {

    }

    fun connect() {
    }

    fun connectBlocking(connectBlocking: Boolean): Boolean {
        return connectBlocking
    }

    fun connectBlocking(timeout: Long, timeUnit: TimeUnit?, connectBlocking: Boolean): Boolean {
        return connectBlocking
    }

    fun closeBlocking() {

    }

    fun onCloseInitiated(code: Int, reason: String?) {
    }

    fun onClosing(code: Int, reason: String?, remote: Boolean) {
    }

    fun isClosing(isClosing: Boolean): Boolean {
        return isClosing
    }

    fun isOpen(isOpen: Boolean): Boolean {
        return isOpen
    }

    fun onWebsocketClosing(
        conn: WebSocket?,
        code: Int,
        reason: String?,
        remote: Boolean
    ) {
    }

    fun close() {
    }

    fun close(code: Int) {
    }

    fun close(code: Int, message: String?) {
    }

    fun closeConnection(code: Int, message: String?) {
    }

    fun onWebsocketCloseInitiated(
        conn: WebSocket?,
        code: Int,
        reason: String?
    ) {
    }

    fun isFlushAndClose(isFlushAndClose: Boolean): Boolean {
        return isFlushAndClose
    }
}