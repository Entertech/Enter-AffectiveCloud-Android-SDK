package cn.entertech.affectivecloudsdk.interfaces

import cn.entertech.affectivecloudsdk.EnterAffectiveCloudApiImpl.Companion.UPLOAD_BCG_PACKAGE_COUNT
import cn.entertech.affectivecloudsdk.EnterAffectiveCloudApiImpl.Companion.UPLOAD_GYRO_PACKAGE_COUNT
import cn.entertech.affectivecloudsdk.entity.RealtimeAffectiveData
import cn.entertech.affectivecloudsdk.entity.RealtimeBioData
import cn.entertech.affectivecloudsdk.entity.RecData

interface IEnterAffectiveCloudManager {
    fun openWebSocket(webSocketCallback: WebSocketCallback)

    fun closeWebSocket()
    fun isWebSocketOpen(): Boolean

    fun closeConnection(code: Int, message: String)
    fun restore(callback: Callback)

    fun init(callback: Callback)

    fun isInited(): Boolean

    fun appendEEGData(brainData: ByteArray)

    fun appendHeartRateData(heartRateData: Int)

    fun appendMCEEGData(brainData: ByteArray)

    fun appendBCGData(bcgData: ByteArray,packageCount:Int = UPLOAD_BCG_PACKAGE_COUNT)

    fun appendGyroData(gyroData:ByteArray,packageCount: Int = UPLOAD_GYRO_PACKAGE_COUNT)

    fun addBiodataRealtimeListener(listener: (RealtimeBioData?) -> (Unit))

    fun addAffectiveDataRealtimeListener(listener: (RealtimeAffectiveData?) -> (Unit))

    fun removeBiodataRealtimeListener(listener: (RealtimeBioData?) -> (Unit))

    fun removeAffectiveRealtimeListener(listener: (RealtimeAffectiveData?) -> (Unit))

    fun getBiodataReport(callback: Callback2<HashMap<Any, Any?>>)

    fun getAffectiveDataReport(callback: Callback2<HashMap<Any, Any?>>)

    fun submit(remark: List<RecData>, callback: Callback)

    fun release(callback: Callback)

    fun addWebSocketConnectListener(listener: () -> Unit)

    fun addWebSocketDisconnectListener(listener: (String) -> Unit)

    fun removeWebSocketConnectListener(listener: () -> Unit)

    fun removeWebSocketDisconnectListener(listener: (String) -> Unit)

    fun addRawJsonRequestListener(listener: ((String) -> (Unit)))

    fun addRawJsonResponseListener(listener: ((String) -> (Unit)))

    fun removeRawJsonRequestListener(listener: ((String) -> (Unit)))

    fun removeRawJsonResponseListener(listener: ((String) -> (Unit)))

}