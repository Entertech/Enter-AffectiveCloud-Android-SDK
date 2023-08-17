package cn.entertech.affective.sdk.api

import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData

interface IAffectiveDataAnalysisService {
    companion object {
        const val UPLOAD_BCG_PACKAGE_COUNT = 10
        const val UPLOAD_GYRO_PACKAGE_COUNT = 5

    }


    fun hasStartBioDataService(): Boolean

    /**
     * 启动情感服务
     * */
    fun startAffectiveService(callback: Callback2<String>)

    /**
     * 重启情感服务
     * */
    fun restoreAffectiveService(callback: Callback)

    /**
     * 订阅数据
     * */
    fun subscribeData(
        bdListener: ((RealtimeBioData?) -> Unit)? = null,
        listener: ((RealtimeAffectiveData?) -> Unit)? = null
    )

    /**
     * 取消订阅
     * */
    fun unSubscribeData(
        bdListener: ((RealtimeBioData?) -> Unit)? = null,
        listener: ((RealtimeAffectiveData?) -> Unit)? = null
    )

    /**
     * 发送数据
     * */

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

    /**
     * 结束情感服务
     * */
    fun finishAffectiveService(callback: Callback)

    fun addServiceConnectStatueListener(
        connectionListener: () -> Unit,
        disconnectListener: (String) -> Unit
    )

    fun removeServiceConnectStatueListener(
        connectionListener: () -> Unit,
        disconnectListener: (String) -> Unit
    )

    fun isAffectiveServiceConnect(): Boolean

    fun closeAffectiveServiceConnection()

    fun getReport(callback: Callback)
}