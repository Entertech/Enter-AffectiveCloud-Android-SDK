package cn.entertech.affectivecloudsdk.interfaces

interface IWebSocketHelper {
    fun open(webSocketCallback: WebSocketCallback)
    fun addConnectListener(listener: (() -> Unit))
    fun addDisconnectListener(listener: ((String) -> Unit))
    fun removeConnectListener(listener: (() -> Unit))
    fun removeDisconnectListener(listener: ((String) -> Unit))
    fun sendMessage(data: ByteArray)
    fun isOpen(): Boolean
    fun close()
    fun closeConnection(code:Int, message:String)
}