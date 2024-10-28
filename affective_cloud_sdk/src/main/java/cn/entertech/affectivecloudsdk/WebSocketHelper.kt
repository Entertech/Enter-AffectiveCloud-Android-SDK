package cn.entertech.affectivecloudsdk

import cn.entertech.affective.sdk.utils.AffectiveLogHelper
import cn.entertech.affectivecloudsdk.interfaces.IWebSocketHelper
import cn.entertech.affectivecloudsdk.interfaces.WebSocketCallback
import cn.entertech.affectivecloudsdk.utils.ConvertUtil
import org.java_websocket.WebSocket
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.enums.ReadyState
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class WebSocketHelper(var address: String, var timeout: Int = 10000) : IWebSocketHelper {
    private var mBrainDataWebSocket: WebSocketClient? = null

    var mOpenCallback: WebSocketCallback? = null
    private var rawJsonRequestListeners = CopyOnWriteArrayList<(String) -> Unit>()
    var rawJsonResponseListeners = CopyOnWriteArrayList<(String) -> Unit>()
    var connectListeners = CopyOnWriteArrayList<() -> Unit>()
    var disconnectListeners = CopyOnWriteArrayList<(String) -> Unit>()


    companion object{
        private const val TAG="WebSocketHelper"
    }
    override fun open(webSocketCallback: WebSocketCallback) {
        try {
            this.mOpenCallback = webSocketCallback
            mBrainDataWebSocket =
                object : WebSocketClient(URI(address), Draft_6455(), null, timeout) {
                    override fun onOpen(handshakedata: ServerHandshake?) {
                        AffectiveLogHelper.d(TAG, "onConnected " + handshakedata.toString())
                        mOpenCallback?.onOpen(handshakedata)
                        connectListeners.forEach {
                            it.invoke()
                        }
                    }

                    override fun onClose(code: Int, reason: String?, remote: Boolean) {
                        mOpenCallback?.onClose(code, reason, remote)
                        disconnectListeners.forEach {
                            it.invoke("$code:$reason")
                        }
                        AffectiveLogHelper.i(TAG, "onClose :$code::reason is $reason")
                    }

                    override fun isClosed(): Boolean {
                        val isClosed = super.isClosed()
                        mOpenCallback?.isClosed(isClosed)
                        return isClosed
                    }

                    override fun getReadyState(): ReadyState {
                        val readyState = super.getReadyState()
                        mOpenCallback?.getReadyState(readyState)
                        return readyState
                    }

                    override fun reconnectBlocking(): Boolean {
                        val reconnectBlocking = super.reconnectBlocking()
                        mOpenCallback?.reconnectBlocking(reconnectBlocking)
                        return reconnectBlocking
                    }

                    override fun reconnect() {
                        mOpenCallback?.reconnect()
                        super.reconnect()
                    }

                    override fun connect() {
                        mOpenCallback?.connect()
                        super.connect()
                    }

                    override fun connectBlocking(): Boolean {
                        val connectBlocking = super.connectBlocking()
                        mOpenCallback?.connectBlocking(connectBlocking)
                        return connectBlocking
                    }

                    override fun connectBlocking(timeout: Long, timeUnit: TimeUnit?): Boolean {
                        val connectBlocking = super.connectBlocking(timeout, timeUnit)
                        mOpenCallback?.connectBlocking(timeout, timeUnit, connectBlocking)
                        return connectBlocking
                    }

                    override fun closeBlocking() {
                        mOpenCallback?.closeBlocking()
                        super.closeBlocking()
                    }

                    override fun onCloseInitiated(code: Int, reason: String?) {
                        mOpenCallback?.onCloseInitiated(code, reason)
                        super.onCloseInitiated(code, reason)
                    }

                    override fun onClosing(code: Int, reason: String?, remote: Boolean) {
                        mOpenCallback?.onClosing(code, reason, remote)
                        super.onClosing(code, reason, remote)
                    }

                    override fun isClosing(): Boolean {
                        val isClose = super.isClosing()
                        mOpenCallback?.isClosing(isClose)
                        return isClose
                    }

                    override fun isOpen(): Boolean {
                        val isOpen = super.isOpen()
                        mOpenCallback?.isOpen(isOpen)
                        return isOpen
                    }

                    override fun onWebsocketClosing(
                        conn: WebSocket?,
                        code: Int,
                        reason: String?,
                        remote: Boolean
                    ) {
                        mOpenCallback?.onWebsocketClosing(conn, code, reason, remote)
                        super.onWebsocketClosing(conn, code, reason, remote)
                    }

                    override fun close() {
                        mOpenCallback?.close()
                        super.close()
                    }

                    override fun close(code: Int) {
                        mOpenCallback?.close(code)
                        super.close(code)
                    }

                    override fun close(code: Int, message: String?) {
                        mOpenCallback?.close(code, message)
                        super.close(code, message)
                    }

                    override fun closeConnection(code: Int, message: String?) {
                        mOpenCallback?.closeConnection(code, message)
                        super.closeConnection(code, message)
                    }

                    override fun onWebsocketCloseInitiated(
                        conn: WebSocket?,
                        code: Int,
                        reason: String?
                    ) {
                        mOpenCallback?.onWebsocketCloseInitiated(conn, code, reason)
                        super.onWebsocketCloseInitiated(conn, code, reason)
                    }

                    override fun isFlushAndClose(): Boolean {
                        val isFlushAndClose = super.isFlushAndClose()
                        mOpenCallback?.isFlushAndClose(isFlushAndClose)
                        return super.isFlushAndClose()
                    }

                    override fun onMessage(message: String?) {
                    }

                    override fun onMessage(message: ByteBuffer) {
                        val arr = ByteArray(message.remaining())
                        message.get(arr)
                        AffectiveLogHelper.d(TAG, "receive msg is " + ConvertUtil.uncompress(arr))
                        var msg = ConvertUtil.uncompress(arr)
                        rawJsonResponseListeners.forEach {
                            it.invoke(msg)
                        }
                    }

                    override fun onError(ex: java.lang.Exception?) {
                        mOpenCallback?.onError(ex)
                        AffectiveLogHelper.d("WebSocketHelper", "onError " + ex.toString())
                    }
                }
            var sslSocketFactory = getSSLSocketFactory()
            mBrainDataWebSocket!!.setSocketFactory(sslSocketFactory)
            mBrainDataWebSocket!!.connect()
        } catch (e: Exception) {
            mOpenCallback?.onError(e)
        }
    }

    private fun getSSLSocketFactory(): SSLSocketFactory? {
        var sslContext: SSLContext? = SSLContext.getInstance("TLS")
        sslContext?.init(null, arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out java.security.cert.X509Certificate>?,
                    authType: String?
                ) {
//                        AffectiveLogHelper.d("getSSLSocketFactory","checkClientTrusted")
                }

                override fun checkServerTrusted(
                    chain: Array<out java.security.cert.X509Certificate>?,
                    authType: String?
                ) {
//                        AffectiveLogHelper.d("getSSLSocketFactory","checkServerTrusted")
                }

                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate>? {
//                        AffectiveLogHelper.d("getSSLSocketFactory","getAcceptedIssuers")
                    return null
                }
            }
        ), SecureRandom())
        return sslContext?.socketFactory
    }


    override fun addConnectListener(listener: () -> Unit) {
        connectListeners.add(listener)
    }

    override fun addDisconnectListener(listener: (String) -> Unit) {
        disconnectListeners.add(listener)
    }

    override fun removeConnectListener(listener: () -> Unit) {
        connectListeners.remove(listener)
    }

    override fun removeDisconnectListener(listener: (String) -> Unit) {
        disconnectListeners.remove(listener)
    }

    fun sendMessage(jsonData: String) {
        AffectiveLogHelper.d(TAG,"sendMessage: $jsonData")
        rawJsonRequestListeners.forEach {
            it.invoke(jsonData)
        }
        sendMessage(ConvertUtil.compress(jsonData))
    }

    override fun sendMessage(data: ByteArray) {
        if (isOpen()) {
            mBrainDataWebSocket?.send(data)
        }else{
            AffectiveLogHelper.e(TAG,"sendMessage is not open")
        }
    }

    override fun isOpen(): Boolean {
        return mBrainDataWebSocket?.isOpen?:false
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