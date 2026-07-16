package cn.entertech.affective.sdk.api

import android.content.Context
import cn.entertech.affective.sdk.bean.AffectiveServiceWay
import cn.entertech.affective.sdk.bean.EnterAffectiveConfigProxy
import cn.entertech.affective.sdk.bean.Error
import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData
import cn.entertech.affective.sdk.bean.UploadReportEntity
import cn.entertech.affective.sdk.bean.type.BioDataCategory
import cn.entertech.affective.sdk.bean.type.IDataType
import cn.entertech.device.api.IDeviceType
import java.io.InputStream


/**
 * Stable business entry point for affective data analysis.
 *
 * Apps should depend on the implementation component they need
 * (`affective_local_sdk` or `affective_cloud_sdk`) and operate through this API
 * contract. Service instances are created by the selected implementation module
 * or by the app's own dependency graph; this API module only defines the public
 * method contract.
 *
 * Recommended lifecycle:
 * 1. Connect the selected service.
 * 2. Start biodata and affective analysis services.
 * 3. Subscribe realtime data.
 * 4. Append hardware data.
 * 5. Fetch reports if needed.
 * 6. Finish and close the connection.
 *
 * Coroutine and Flow helpers are available as Kotlin extension APIs in
 * `AffectiveDataAnalysisServiceExtensions.kt`.
 */
interface IAffectiveDataAnalysisService {

    companion object {
        const val UPLOAD_BCG_PACKAGE_COUNT = 10
        const val UPLOAD_GYRO_PACKAGE_COUNT = 5
    }

    /**
     * 连接情感云
     * */
    fun connectAffectiveServiceConnection(
        listener: IConnectionServiceListener?, configProxy: EnterAffectiveConfigProxy
    )

    /**
     * 断开
     * */
    fun closeAffectiveServiceConnection()

    fun hasConnectAffectiveService(): Boolean

    @Deprecated(
        message = "The method name contains a typo and exposes callback-style state. Use connectAffectiveServiceConnection() and hasConnectAffectiveService(). Kotlin callers can also use awaitConnectAffectiveService().",
    )
    fun addServiceConnectStatueListener(
        connectionListener: (() -> Unit)? = null, disconnectListener: ((String) -> Unit)? = null
    )

    @Deprecated(
        message = "The method name contains a typo and exposes callback-style state. Use closeAffectiveServiceConnection() and hasConnectAffectiveService().",
    )
    fun removeServiceConnectStatueListener(
        connectionListener: (() -> Unit)? = null, disconnectListener: ((String) -> Unit)? = null
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
     * @param isEar 是否为耳道脑电报表
     * */
    fun getReport(
        listener: IGetReportListener, isEar: Boolean = false, needFinishService: Boolean = false
    )

    suspend fun suspendGetReport(needFinishService: Boolean): UploadReportEntity?

    /**
     * 是否启动了情感云服务
     * */
    fun hasStartAffectiveService(): Boolean

    /**
     * 检查一次初始化状态
     * @see [hasStartAffectiveService]
     * */
    @Deprecated(
        message = "This method is a no-op and its name contains a typo. Use hasStartAffectiveService().",
        replaceWith = ReplaceWith("hasStartAffectiveService()"),
    )
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
    @Deprecated(
        message = "File parsing is not part of the algorithm runtime contract. Parse files in the app/data layer and append typed AffectiveInput values with append(AffectiveInput).",
    )
    fun <R> readFileAnalysisData(
        inputStream: InputStream,
        appSingleData: ((R) -> Boolean)? = null,
        appendAllData: (List<R>) -> Unit,
        case: (String) -> R,
        callback: Callback,
    )

    fun isGoodQuality(quality: Double): Boolean

    fun appendDevicePosition(device: IDeviceType, position: String) {

    }

    @Deprecated(
        message = "Use append(AffectiveInput.*) or the typed append methods.",
        replaceWith = ReplaceWith("append(AffectiveInput.EegBytes(data))", "cn.entertech.affective.sdk.api.AffectiveInput"),
    )
    fun appendByteArrayData(dataType: IDataType, data: ByteArray): Boolean {
        return when (dataType) {
            BioDataCategory.EEG -> {
                appendEEGData(data, false)
                true
            }

            BioDataCategory.SCEEG -> {
                appendSCEEGData(data)
                true
            }


            BioDataCategory.PEPR -> {
                appendPEPRData(data)
                true
            }

            else -> {
                false
            }
        }
    }

    @Deprecated(
        message = "No built-in IDataType mapping currently consumes Double. Keep data conversion in the caller.",
    )
    fun appendDoubleData(dataType: IDataType, data: Double): Boolean {
        return false
    }

    @Deprecated(
        message = "Use append(AffectiveInput.*) or the typed append methods.",
        replaceWith = ReplaceWith("append(AffectiveInput.HeartRate(data))", "cn.entertech.affective.sdk.api.AffectiveInput"),
    )
    fun appendIntData(dataType: IDataType, data: Int): Boolean {
        if (!hasStartAffectiveService()) {
            return false
        }
        when (dataType) {
            BioDataCategory.EEG -> {
                appendEEGData(data)
            }

            BioDataCategory.SCEEG -> {
                appendSCEEGData(data)
            }

            BioDataCategory.HR -> {
                appendHeartRateData(data)
            }

            else -> {
                return false
            }
        }
        return true
    }

    @Deprecated(
        message = "Use append(AffectiveInput.*) or the typed append methods.",
        replaceWith = ReplaceWith("append(AffectiveInput.EegSamples(data))", "cn.entertech.affective.sdk.api.AffectiveInput"),
    )
    fun appendIntListData(dataType: IDataType, data: List<Int>): Boolean {
        when (dataType) {
            BioDataCategory.EEG -> {
                appendEEGData(data)
            }

            BioDataCategory.SCEEG -> {
                appendSCEEGData(data)
            }

            else -> {
                return false
            }
        }
        return true
    }

    @Deprecated(
        message = "No built-in IDataType mapping currently consumes Float. Keep data conversion in the caller.",
    )
    fun appendFloatData(dataType: IDataType, data: Float): Boolean {
        return false
    }

    fun appendEEGData(brainData: ByteArray, isEar: Boolean = false)
    fun appendEEGData(brainData: Int, isEar: Boolean = false)
    fun appendEEGData(brainData: List<Int>, isEar: Boolean = false)

    fun appendDCEEGData(brainData: ByteArray)

    /**
     * 单通道数据
     * */
    fun appendSCEEGData(brainData: ByteArray, isEar: Boolean = false)
    fun appendSCEEGData(brainData: Int, isEar: Boolean = false)
    fun appendSCEEGData(brainData: List<Int>, isEar: Boolean = false)

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

    fun append(input: AffectiveInput): AffectiveServiceResult<Unit> = try {
        when (input) {
            is AffectiveInput.EegBytes -> appendEEGData(input.data, input.isEar)
            is AffectiveInput.EegSample -> appendEEGData(input.value, input.isEar)
            is AffectiveInput.EegSamples -> appendEEGData(input.data, input.isEar)
            is AffectiveInput.SceegBytes -> appendSCEEGData(input.data, input.isEar)
            is AffectiveInput.SceegSample -> appendSCEEGData(input.value, input.isEar)
            is AffectiveInput.SceegSamples -> appendSCEEGData(input.data, input.isEar)
            is AffectiveInput.DcEeg -> appendDCEEGData(input.data)
            is AffectiveInput.McEeg -> appendMCEEGData(input.data)
            is AffectiveInput.HeartRate -> appendHeartRateData(input.value)
            is AffectiveInput.Pepr -> appendPEPRData(input.data)
            is AffectiveInput.Bcg -> appendBCGData(input.data, input.packageCount)
            is AffectiveInput.Gyro -> appendGyroData(input.data, input.packageCount)
        }
        AffectiveServiceResult.Success(Unit)
    } catch (throwable: Throwable) {
        AffectiveServiceResult.Failure(Error(-1, throwable.message ?: "append failed"))
    }

    fun getAffectiveWay(): AffectiveServiceWay
}
