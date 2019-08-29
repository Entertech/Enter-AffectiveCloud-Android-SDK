package cn.entertech.affectivecloudsdk.interfaces

import cn.entertech.affectivecloudsdk.entity.RealtimeAffectiveData
import cn.entertech.affectivecloudsdk.entity.RealtimeBioData

interface IEnterAffectiveCloudManager {
    fun openWebSocket(webSocketCallback: WebSocketCallback)

    fun isWebSocketOpen():Boolean

    fun restore(callback: Callback)

    fun init(callback: Callback)

    fun isInited():Boolean

    fun appendBrainData(brainData: ByteArray, triggerCount: Int = 600)

    fun appendHeartRateData(heartRateData: Int, triggerCount: Int = 2)

    fun addBiodataRealtimeListener(listener: (RealtimeBioData?) -> (Unit))

    fun addAffectiveRealtimeListener(listener: (RealtimeAffectiveData?) -> (Unit))

    fun removeBiodataRealtimeListener(listener: (RealtimeBioData?) -> (Unit))

    fun removeAffectiveRealtimeListener(listener: (RealtimeAffectiveData?) -> (Unit))

    fun reportBiodata(callback: Callback2<HashMap<Any, Any?>>)

    fun reportAffective(callback: Callback2<HashMap<Any, Any?>>)

    fun release(callback: Callback)

    fun addWebSocketConnectListener(listener: () -> Unit)

    fun addWebSocketDisconnectListener(listener: () -> Unit)

    fun removeWebSocketConnectListener(listener: () -> Unit)

    fun removeWebSocketDisconnectListener(listener: () -> Unit)

    fun addRawJsonRequestListener(listener: ((String) -> (Unit)))

    fun addRawJsonResponseListener(listener: ((String) -> (Unit)))

    fun removeRawJsonRequestListener(listener: ((String) -> (Unit)))

    fun removeRawJsonResponseListener(listener: ((String) -> (Unit)))

}