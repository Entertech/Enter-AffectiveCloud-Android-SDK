package cn.entertech.affective.sdk.api

import android.content.Context
import cn.entertech.affective.sdk.bean.AffectiveServiceWay
import cn.entertech.affective.sdk.bean.EnterAffectiveConfigProxy
import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData
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
        connectionListener: () -> Unit,
        disconnectListener: (String) -> Unit
    )

    fun removeServiceConnectStatueListener(
        connectionListener: () -> Unit,
        disconnectListener: (String) -> Unit
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

    /**
     * 获取报表
     * @param needFinishService 是否需要自动结束情感服务 true 自动结束
     * */
    fun getReport(listener: IGetReportListener, needFinishService: Boolean)

    /**
     * 是否启动了情感云服务
     * */
    fun hasStartAffectiveService(): Boolean

    /**
     * 检查一次初始化状态
     * @see [hasStartAffectiveService]
     * */
    fun checkInitStatue(){

    }

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
     * @param file 待分析的文件
     * */
    fun <T> readFileAnalysisData(inputStream: InputStream, callback: Callback2<T>,case:(Int)->T?)

    /**
     * 发送数据
     * */

    fun appendEEGData(brainData: ByteArray)

    fun appendDCEEGData(brainData: ByteArray)

    /**
     * 单通道数据
     * */
    fun appendSCEEGData(brainData: ByteArray)

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