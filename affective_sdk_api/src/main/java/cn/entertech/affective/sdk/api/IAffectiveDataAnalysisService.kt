package cn.entertech.affective.sdk.api

import cn.entertech.affective.sdk.bean.AffectiveServiceWay
import cn.entertech.affective.sdk.bean.EnterAffectiveConfigProxy
import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData
import cn.entertech.affective.sdk.bean.UploadReportEntity
import java.io.File
import java.io.InputStream
import java.util.ServiceLoader

interface IAffectiveDataAnalysisService {

    companion object {
        const val UPLOAD_BCG_PACKAGE_COUNT = 10
        const val UPLOAD_GYRO_PACKAGE_COUNT = 5


        fun getService(way: AffectiveServiceWay): IAffectiveDataAnalysisService? {
            ServiceLoader.load(IAffectiveDataAnalysisService::class.java)
                .forEach {
                    if (it.getAffectiveWay() == way) {
                        return it
                    }
                }
            return null
        }
    }


    fun hasStartBioDataService(): Boolean

    fun checkInitStatue(){

    }

    /**
     * 启动情感服务
     * */
    fun startAffectiveService(
        authenticationInputStream: InputStream?,
        callback: Callback2<String>,
        builder: EnterAffectiveConfigProxy
    )

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

    fun <T> readFileAnalysisData(filePath: String, callback: Callback2<T>,case:(Int)->T?)

    fun <T> readFileAnalysisData(inputStream: InputStream, callback: Callback2<T>,case:(Int)->T?)

    /**
     * @param file 待分析的文件
     * */
    fun <T> readFileAnalysisData(file: File, callback: Callback2<T>,case:(Int)->T?)

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

    fun getReport(callback: Callback2<UploadReportEntity>,needFinishService:Boolean)


    fun getAffectiveWay(): AffectiveServiceWay
}