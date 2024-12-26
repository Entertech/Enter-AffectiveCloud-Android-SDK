package cn.entertech.affective.sdk.api

import android.content.Context
import cn.entertech.affective.sdk.bean.AffectiveServiceWay
import cn.entertech.affective.sdk.bean.EnterAffectiveConfigProxy
import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData
import cn.entertech.affective.sdk.bean.UploadReportEntity
import cn.entertech.ble.api.bean.MeditateDataType
import cn.entertech.device.DeviceType
import java.io.InputStream
import java.util.ServiceLoader


/**
 * 步骤：
 * 1.建立情感云连接
 * 2.启动、初始化生物数据基础分析服务&& 若配置感云计算服务，则启动情感云计算服务
 * 3.启动服务成功后，各自订阅自己的数据
 * 4.取消订阅
 * 5.结束服务
 * 6.关闭情感云连接
 * */
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

    /**
     * 连接情感云
     * */
    fun connectAffectiveServiceConnection(
        listener: IConnectionServiceListener,
        configProxy: EnterAffectiveConfigProxy
    )

    /**
     * 断开
     * */
    fun closeAffectiveServiceConnection()

    fun hasConnectAffectiveService(): Boolean

    fun addServiceConnectStatueListener(
        connectionListener: (() -> Unit)? = null,
        disconnectListener: ((String) -> Unit)? = null
    )

    fun removeServiceConnectStatueListener(
        connectionListener: (() -> Unit)? = null,
        disconnectListener: ((String) -> Unit)? = null
    )

    fun getSessionId(): String
    /**
     * 启动情感服务
     * */
    fun startAffectiveService(
        initListener: IStartAffectiveServiceLister
    )


    /**
     * 启动情感服务
     * */
    fun startAffectiveService(
        authenticationInputStream: InputStream?,
        context: Context?,
        initListener: IStartAffectiveServiceLister
    )

    /**
     * 重启情感服务
     * */
    fun restoreAffectiveService(listener: IStartAffectiveServiceLister)

    /**
     * 结束情感服务
     * */
    fun finishAffectiveService(listener: IFinishAffectiveServiceListener)

    suspend fun suspendFinishAffectiveService()
    /**
     * 获取报表
     * @param needFinishService 是否需要自动结束情感服务 true 自动结束
     * */
    fun getReport(listener: IGetReportListener, needFinishService: Boolean)

    suspend fun suspendGetReport(needFinishService: Boolean):UploadReportEntity?

    /**
     * 是否启动了情感云服务
     * */
    fun hasStartAffectiveService(): Boolean

    /**
     * 检查一次初始化状态
     * @see [hasStartAffectiveService]
     * */
    fun checkInitStatue() {

    }

    /**
     * 检查环境
     * @return true 表示当前环境可用
     * */
    fun checkEnvironment(context: Context): Boolean = true

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
     * @param inputStream 待分析的数据流
     * @param callback 结果回调
     * @param appSingleData 处理单个数据，若返回true，则表示消耗该数据，不添加到all数据里面
     * @param case 数据流读取出来的字符串转成需要的类型R
     * @param appendAllData 处理所有未被消耗的数据
     * */
    fun <R> readFileAnalysisData(
        inputStream: InputStream,
        appSingleData: ((R) -> Boolean)? = null,
        appendAllData: (List<R>) -> Unit,
        case: (String) -> R,
        callback: Callback,
    )

    fun isGoodQuality(quality: Double): Boolean

    fun appendDevicePosition(device:DeviceType,position:String){

    }

    /**
     * 发送数据
     * */
    @Deprecated("", ReplaceWith("appendByteArrayData(dataType, data)"))
    fun appendData(dataType: MeditateDataType, data: ByteArray): Boolean {
        return appendByteArrayData(dataType, data)
    }

    fun appendByteArrayData(dataType: MeditateDataType, data: ByteArray): Boolean {
        return when (dataType) {
            MeditateDataType.EEG -> {
                appendEEGData(data, false)
                true
            }

            MeditateDataType.SCEEG -> {
                appendSCEEGData(data)
                true
            }


            MeditateDataType.PEPR -> {
                appendPEPRData(data)
                true
            }

            else -> {
                false
            }
        }
    }

    fun appendDoubleData(dataType: MeditateDataType, data: Double): Boolean {
        return false
    }

    fun appendIntData(dataType: MeditateDataType, data: Int): Boolean {
        if (!hasStartAffectiveService()) {
            return false
        }
        when (dataType) {
            MeditateDataType.EEG -> {
                appendEEGData(data,)
            }

            MeditateDataType.SCEEG -> {
                appendSCEEGData(data)
            }

            MeditateDataType.HEART_RATE -> {
                appendHeartRateData(data)
            }

            else -> {
                return false
            }
        }
        return true
    }

    fun appendIntListData(dataType: MeditateDataType, data: List<Int>): Boolean {
        when (dataType) {
            MeditateDataType.EEG -> {
                appendEEGData(data,)
            }

            MeditateDataType.SCEEG -> {
                appendSCEEGData(data)
            }

            else -> {
                return false
            }
        }
        return true
    }

    fun appendFloatData(dataType: MeditateDataType, data: Float): Boolean {
        return false
    }

    fun appendEEGData(brainData: ByteArray, isEar: Boolean = false)
    fun appendEEGData(brainData: Int, isEar: Boolean = false)
    fun appendEEGData(brainData: List<Int>, isEar: Boolean = false)

    fun appendDCEEGData(brainData: ByteArray)

    /**
     * 单通道数据
     * */
    fun appendSCEEGData(brainData: ByteArray)
    fun appendSCEEGData(brainData: Int)
    fun appendSCEEGData(brainData: List<Int>)

    /**
     * 添加心率数据
     * */
    fun appendHeartRateData(heartRateData: Int)

    fun appendMCEEGData(mceegData: ByteArray)

    /**
     * 坐垫数据
     * */
    fun appendPEPRData(peprData: ByteArray)

    fun appendBCGData(bcgData: ByteArray, packageCount: Int = UPLOAD_BCG_PACKAGE_COUNT)

    fun appendGyroData(gyroData: ByteArray, packageCount: Int = UPLOAD_GYRO_PACKAGE_COUNT)


    fun getAffectiveWay(): AffectiveServiceWay
}