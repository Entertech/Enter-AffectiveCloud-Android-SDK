package cn.entertech.affectivecloudsdk.interfaces

import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception

interface WebSocketCallback{
    fun onOpen(serverHandshake: ServerHandshake?)
    fun onClose(code: Int, reason: String?, remote: Boolean)
    fun onError(e:Exception?)
}