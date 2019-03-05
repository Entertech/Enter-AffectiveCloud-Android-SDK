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
    val SOCKET_ADDRESS: String = "api.affectivecloud.com"
    val BRAIN_DATA_SOCKET_PORT: Int = 8080
    var handlerThread: HandlerThread
    var handler: Handler

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

    fun connetBrainDataSocket() {
        Thread(Runnable {
            mBrainDataSocket?.connect(InetSocketAddress(SOCKET_ADDRESS, BRAIN_DATA_SOCKET_PORT))
            Logger.d("socket connect state: " + mBrainDataSocket?.isConnected)
            readMessageFromServer()
        }).start()

    }

    fun readMessageFromServer() {
        var inputStream = mBrainDataSocket?.getInputStream()
        var bufferedReader = BufferedReader(InputStreamReader(inputStream))
        while (true) {
            var result = bufferedReader.readLine()
            if (result != null) {
                brainDataCallback.forEach {
                    it.invoke(result)
                }
                Logger.d("receive data from server: $result")
            }
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
//                var inputStream = mBrainDataSocket?.getInputStream()
//                var bufferedReader = BufferedReader(InputStreamReader(inputStream))
//                var result = bufferedReader.readLine()
//                if (result != null) {
//                    brainDataCallback.forEach {
//                        it.invoke(result)
//                    }
//                    Logger.d("receive data from server: $result")
//                }
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
        mBrainDataSocket?.close()
    }

}