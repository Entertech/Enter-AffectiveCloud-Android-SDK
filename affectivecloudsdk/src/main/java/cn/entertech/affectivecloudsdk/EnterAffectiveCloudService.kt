package cn.entertech.affectivecloudsdk

import android.content.Context
import cn.entertech.affective.sdk.bean.EnterAffectiveConfigProxy
import cn.entertech.affective.sdk.api.Callback2
import cn.entertech.affective.sdk.api.IAffectiveDataAnalysisService
import cn.entertech.affective.sdk.api.IConnectionServiceListener
import cn.entertech.affective.sdk.api.IFinishAffectiveServiceListener
import cn.entertech.affective.sdk.api.IGetReportListener
import cn.entertech.affective.sdk.api.IStartAffectiveServiceLister
import cn.entertech.affective.sdk.bean.AffectiveServiceWay
import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData
import java.util.HashMap
import cn.entertech.affective.sdk.bean.Error
import cn.entertech.affective.sdk.utils.AffectiveLogHelper
import com.google.auto.service.AutoService
import java.io.InputStream

@AutoService(IAffectiveDataAnalysisService::class)
class EnterAffectiveCloudService : IAffectiveDataAnalysisService {

    companion object {
        private const val TAG = "EnterAffectiveCloudService"
    }

    private var mEnterAffectiveCloudManager: EnterAffectiveCloudManager? = null


    /**
     * 连接websocket
     * 创建session
     * */
    override fun connectAffectiveServiceConnection(
        listener: IConnectionServiceListener,
        configProxy: EnterAffectiveConfigProxy
    ) {
        mEnterAffectiveCloudManager =
            EnterAffectiveCloudManager(EnterAffectiveCloudConfig.proxyInstance(configProxy))
        mEnterAffectiveCloudManager?.openWebSocket(listener)
    }

    /**
     * 启动bioDataService，启动成功后订阅数据
     * 启动AffectiveService ，启动成功后订阅数据
     *
     * */
    override fun startAffectiveService(
        authenticationInputStream: InputStream?,
        context: Context?, initListener: IStartAffectiveServiceLister
    ) {
        AffectiveLogHelper.d(TAG, "startAffectiveService")
        mEnterAffectiveCloudManager?.init(initListener)?: run {
            AffectiveLogHelper.e(TAG, "startAffectiveService mEnterAffectiveCloudManager is null")
        }
    }

    override fun restoreAffectiveService(listener: IStartAffectiveServiceLister) {
        AffectiveLogHelper.d(TAG, "restoreAffectiveService")
        mEnterAffectiveCloudManager?.restore(listener)?: run {
            AffectiveLogHelper.e(TAG, "restoreAffectiveService mEnterAffectiveCloudManager is null")
        }
    }

    override fun subscribeData(
        bdListener: ((RealtimeBioData?) -> Unit)?,
        listener: ((RealtimeAffectiveData?) -> Unit)?
    ) {
        AffectiveLogHelper.d(TAG, "subscribeData")
        bdListener?.apply {
            mEnterAffectiveCloudManager?.addBiodataRealtimeListener(this)?: run {
                AffectiveLogHelper.e(TAG, "mEnterAffectiveCloudManager is null")
            }
        }
        listener?.apply {
            mEnterAffectiveCloudManager?.addAffectiveDataRealtimeListener(this)
        }
    }

    override fun unSubscribeData(
        bdListener: ((RealtimeBioData?) -> Unit)?,
        listener: ((RealtimeAffectiveData?) -> Unit)?
    ) {
        AffectiveLogHelper.d(TAG, "unSubscribeData")
        bdListener?.apply {
            mEnterAffectiveCloudManager?.removeBiodataRealtimeListener(this)
        }
        listener?.apply {
            mEnterAffectiveCloudManager?.removeAffectiveRealtimeListener(this)
        }
    }


    override fun <T> readFileAnalysisData(
        inputStream: InputStream,
        callback: Callback2<T>,
        case: (Int) -> T?
    ) {
        callback.onError(Error(-1, "not support this method"))
    }


    override fun appendEEGData(brainData: ByteArray) {
        mEnterAffectiveCloudManager?.appendEEGData(brainData)
    }

    override fun appendDCEEGData(brainData: ByteArray) {
        mEnterAffectiveCloudManager?.appendDCEEGData(brainData)
    }

    override fun appendSCEEGData(brainData: ByteArray) {
        mEnterAffectiveCloudManager?.appendSCEEGData(brainData)
    }

    override fun appendHeartRateData(heartRateData: Int) {
        mEnterAffectiveCloudManager?.appendHeartRateData(heartRateData)
    }

    override fun appendMCEEGData(mceegData: ByteArray) {
        mEnterAffectiveCloudManager?.appendMCEEGData(mceegData)
    }

    override fun appendPEPRData(peprData: ByteArray) {
        mEnterAffectiveCloudManager?.appendPEPRData(peprData)
    }

    override fun appendBCGData(bcgData: ByteArray, packageCount: Int) {
        mEnterAffectiveCloudManager?.appendBCGData(bcgData, packageCount)
    }

    override fun appendGyroData(gyroData: ByteArray, packageCount: Int) {
        mEnterAffectiveCloudManager?.appendGyroData(gyroData, packageCount)
    }

    override fun finishAffectiveService(listener: IFinishAffectiveServiceListener) {
        AffectiveLogHelper.d(TAG, "finishAffectiveService")
        mEnterAffectiveCloudManager?.release(listener)
    }

    override fun addServiceConnectStatueListener(
        connectionListener: () -> Unit,
        disconnectListener: (String) -> Unit
    ) {
        mEnterAffectiveCloudManager?.addWebSocketConnectListener(connectionListener)
        mEnterAffectiveCloudManager?.addWebSocketDisconnectListener(disconnectListener)
    }

    override fun removeServiceConnectStatueListener(
        connectionListener: () -> Unit,
        disconnectListener: (String) -> Unit
    ) {
        mEnterAffectiveCloudManager?.removeWebSocketConnectListener(connectionListener)
        mEnterAffectiveCloudManager?.removeWebSocketDisconnectListener(disconnectListener)
    }

    override fun hasStartAffectiveService(): Boolean {
        AffectiveLogHelper.d(TAG, "hasStartBioDataService")
        return mEnterAffectiveCloudManager?.isInited() ?: false
    }

    override fun hasConnectAffectiveService(): Boolean {
        AffectiveLogHelper.d(TAG, "isAffectiveServiceConnect $mEnterAffectiveCloudManager")
        return mEnterAffectiveCloudManager?.isWebSocketOpen() ?: false
    }

    override fun closeAffectiveServiceConnection() {
        mEnterAffectiveCloudManager?.closeWebSocket()
    }

    /**
     * 上传report指令，失败不做处理，成功开始释放资源
     * 释放资源之后不管成功还是失败，关闭websocket，然后http请求报表数据
     * */
    override fun getReport(listener: IGetReportListener, needFinishService: Boolean) {
        mEnterAffectiveCloudManager?.getBiodataReport(object : Callback2<HashMap<Any, Any?>> {
            override fun onError(error: Error?) {
                listener.getBioReportError(error)
            }

            override fun onSuccess(t: HashMap<Any, Any?>?) {
                mEnterAffectiveCloudManager?.getAffectiveDataReport(object :
                    Callback2<HashMap<Any, Any?>> {
                    override fun onError(error: Error?) {
                        listener.getAffectiveReportError(error)
                        listener.onError(error)
                    }

                    override fun onSuccess(t: HashMap<Any, Any?>?) {
                        if (!needFinishService) {
                            listener.onSuccess(null)
                            return
                        }
                        finishAffectiveService(object : IFinishAffectiveServiceListener {
                            override fun finishBioFail(error: Error?) {

                            }

                            override fun finishAffectiveFail(error: Error?) {
                            }

                            override fun finishError(error: Error?) {
                                listener.onSuccess(null)
                            }

                            override fun finishSuccess() {
                                listener.onSuccess(null)
                            }
                        })
                    }

                })
            }

        })
    }

    override fun getAffectiveWay() = AffectiveServiceWay.AffectiveCloudService
}