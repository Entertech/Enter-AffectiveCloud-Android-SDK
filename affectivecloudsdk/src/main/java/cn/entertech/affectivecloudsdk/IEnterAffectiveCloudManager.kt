package cn.entertech.affectivecloudsdk

import cn.entertech.affective.sdk.api.Callback
import cn.entertech.affective.sdk.api.Callback2
import cn.entertech.affective.sdk.api.IAffectiveDataAnalysisService.Companion.UPLOAD_BCG_PACKAGE_COUNT
import cn.entertech.affective.sdk.api.IAffectiveDataAnalysisService.Companion.UPLOAD_GYRO_PACKAGE_COUNT
import cn.entertech.affective.sdk.api.IFinishAffectiveServiceListener
import cn.entertech.affective.sdk.api.IStartAffectiveServiceLister
import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData


/**
 * https://docs.affectivecloud.cn/%F0%9F%8E%99%E6%8E%A5%E5%8F%A3%E5%8D%8F%E8%AE%AE/%E7%94%9F%E7%89%A9%E6%95%B0%E6%8D%AE%E5%9F%BA%E7%A1%80%E5%88%86%E6%9E%90%E6%9C%8D%E5%8A%A1%E5%8D%8F%E8%AE%AE
 * https://docs.affectivecloud.cn/%F0%9F%8E%99%E6%8E%A5%E5%8F%A3%E5%8D%8F%E8%AE%AE/%E6%83%85%E6%84%9F%E8%AE%A1%E7%AE%97%E6%9C%8D%E5%8A%A1%E5%8D%8F%E8%AE%AE
 *
 * */
interface IEnterAffectiveCloudManager {


    /**
     * 初始化分析服务
     * */
    fun init(initListener: IStartAffectiveServiceLister)

    /**
     * 是否启动了生物数据基础分析服务
     * */
    fun isInited(): Boolean

    /**
     * 获取会话id
     * */
    fun getSessionId(): String?

    /**
     * 情感分析服务是否可用
     * */
    fun isWebSocketOpen(): Boolean

    /**
     * 恢复
     * */
    fun restore(listener: IStartAffectiveServiceLister)

    fun closeWebSocket()

    fun release(listener: IFinishAffectiveServiceListener)

    fun closeSession(callback: Callback)

    fun getBiodataReport(callback: Callback2<HashMap<Any, Any?>>)
    fun getAffectiveDataReport(callback: Callback2<HashMap<Any, Any?>>)

    //==================append data==========================

    fun appendEEGData(brainData: ByteArray)

    fun appendDCEEGData(brainData: ByteArray)

    /**
     * 单通道数据
     * */
    fun appendSCEEGData(brainData: ByteArray)

    fun appendHeartRateData(heartRateData: Int)

    fun appendMCEEGData(mceegData: ByteArray)

    /**
     * 坐垫数据
     * */
    fun appendPEPRData(peprData: ByteArray)

    fun appendBCGData(bcgData: ByteArray, packageCount: Int = UPLOAD_BCG_PACKAGE_COUNT)

    fun appendGyroData(gyroData: ByteArray, packageCount: Int = UPLOAD_GYRO_PACKAGE_COUNT)

    //==================listener==========================
    fun addBiodataRealtimeListener(listener: (RealtimeBioData?) -> (Unit))
    fun removeBiodataRealtimeListener(listener: (RealtimeBioData?) -> (Unit))

    fun addAffectiveDataRealtimeListener(listener: (RealtimeAffectiveData?) -> (Unit))
    fun removeAffectiveRealtimeListener(listener: (RealtimeAffectiveData?) -> (Unit))

    fun addWebSocketConnectListener(listener: () -> Unit)
    fun removeWebSocketConnectListener(listener: () -> Unit)

    fun addWebSocketDisconnectListener(listener: (String) -> Unit)
    fun removeWebSocketDisconnectListener(listener: (String) -> Unit)

    fun addRawJsonRequestListener(listener: ((String) -> (Unit)))
    fun removeRawJsonRequestListener(listener: ((String) -> (Unit)))

    fun addRawJsonResponseListener(listener: ((String) -> (Unit)))
    fun removeRawJsonResponseListener(listener: ((String) -> (Unit)))

}