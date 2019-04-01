package cn.entertech.biomoduledemo

import android.os.Handler
import android.os.HandlerThread
import android.os.ProxyFileDescriptorCallback
import com.orhanobut.logger.Logger
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket

class SocketManager() {
    var mBrainDataSocket: Socket? = null
//    val SOCKET_ADDRESS: String = "api.affectivecloud.com"
    val SOCKET_ADDRESS: String = "test.affectivecloud.com"
    val BRAIN_DATA_SOCKET_PORT: Int = 8080
    var handlerThread: HandlerThread
    var handler: Handler
    var inputStream: InputStream? = null
    var bufferedReader: BufferedReader? = null

    var brainDataCallback = mutableListOf<(String) -> Unit>()

    init {
        mBrainDataSocket = Socket()
        handlerThread = HandlerThread("socket_thread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }

    companion object {
        private var socketManager: SocketManager? = null
        fun getInstance(): SocketManager {
            if (socketManager == null) {
                synchronized(SocketManager::class.java) {
                    if (socketManager == null) {
                        socketManager = SocketManager()
                    }
                }
            }
            return socketManager!!
        }
    }

    fun connectBrainDataSocket() {
        connectBrainDataSocket(null)
    }

    fun connectBrainDataSocket(connectSuccess:(()->Unit)?) {
        Thread(Runnable {
            mBrainDataSocket = Socket()
            mBrainDataSocket?.connect(InetSocketAddress(SOCKET_ADDRESS, BRAIN_DATA_SOCKET_PORT))
            Logger.d("socket connect state: " + mBrainDataSocket?.isConnected)
            if (mBrainDataSocket!!.isConnected){
                connectSuccess?.invoke()
            }
            inputStream = mBrainDataSocket?.getInputStream()
            bufferedReader = BufferedReader(InputStreamReader(inputStream))
            isRead = true
            readMessageFromServer()
        }).start()
    }

    private var isRead = false
    fun stopRead(){
        isRead = false
    }

    fun readMessageFromServer() {
        try {
            while (isRead) {
                var result = bufferedReader?.readLine()
                if (result != null) {
                    brainDataCallback.forEach {
                        it.invoke(result)
                    }
                    Logger.d("receive data from server: $result")
                }
            }
        }catch (e:Exception){
           e.printStackTrace()
        }
    }

    fun sendMessage(data: String) {
        handler.post {
            var pw: PrintWriter? = null
            try {
                pw = PrintWriter(mBrainDataSocket?.getOutputStream())
                pw.write(data + "\r\n")
                pw.flush()
                Logger.d("send data to server: $data\r\n")
            } catch (e: IOException) {
                Logger.d("send error:$e")
                e.printStackTrace()
            }
        }
    }


    fun addBrainDataListener(callback: ((String) -> Unit)) {
        brainDataCallback.add(callback)
    }

    fun removeBrainDataListener(callback: ((String) -> Unit)) {
        brainDataCallback.remove(callback)
    }


    fun disconnectBrainSocket() {
        mBrainDataSocket?.shutdownInput()
        mBrainDataSocket?.close()

    }

}