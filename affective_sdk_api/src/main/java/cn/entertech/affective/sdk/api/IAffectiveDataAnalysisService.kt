package cn.entertech.affective.sdk.api

import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData

interface IAffectiveDataAnalysisService {
    //==================append data==========================
    /**
     * 坐垫数据
     * */
    fun appendPEPR(pepr: ByteArray)

    /**
     * 双通道数据
     * */
    fun appendEEGData(brainData: ByteArray)
    fun appendBCGData(brainData: ByteArray)
    fun appendDCEEGData(brainData: ByteArray)
    fun appendGyroData(brainData: ByteArray)
    fun appendMCEEGData(brainData: ByteArray)

    /**
     * 单通道数据
     * */
    fun appendSCEEGData(brainData: ByteArray)

    /**
     * 心率相关数据
     * */
    fun appendHeartRateData(heartRateData: Int)

    //==================listener==========================
    fun addAffectiveDataRealtimeListener(listener: (RealtimeAffectiveData?) -> (Unit))
    fun addBiodataRealtimeListener(listener: (RealtimeBioData?) -> (Unit))



}