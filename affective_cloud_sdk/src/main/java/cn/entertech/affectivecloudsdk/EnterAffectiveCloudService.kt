package cn.entertech.affectivecloudsdk

import android.content.Context
import cn.entertech.affective.sdk.api.Callback
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
import cn.entertech.affective.sdk.bean.UploadReportEntity
import java.util.HashMap
import cn.entertech.affective.sdk.bean.Error
import cn.entertech.affective.sdk.utils.AffectiveLogHelper
import cn.entertech.base.net.NetworkUtils
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

internal class OneShotContinuation<T>(private val continuation: Continuation<T>) {
    private val hasCompleted = AtomicBoolean(false)

    fun resumeWith(result: Result<T>) {
        if (hasCompleted.compareAndSet(false, true)) {
            continuation.resumeWith(result)
        }
    }
}

/**
 * 同一个useid 不能频繁创建连接
 * 15之内未触发append方法，也就是15之内未收到源消息；或者15s之内未收到情感云数据，需要断开重连
 * */
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
        listener: IConnectionServiceListener?,
        configProxy: EnterAffectiveConfigProxy
    ) {
        mEnterAffectiveCloudManager =
            EnterAffectiveCloudManager(EnterAffectiveCloudConfig.proxyInstance(configProxy))
        mEnterAffectiveCloudManager?.openWebSocket(listener ?: object : IConnectionServiceListener {
            override fun connectionSuccess(sessionId: String?) = Unit

            override fun connectionError(error: Error?) = Unit
        })
    }

    override fun getSessionId(): String = mEnterAffectiveCloudManager?.getSessionId().orEmpty()

    override fun startAffectiveService(initListener: IStartAffectiveServiceLister) {
        AffectiveLogHelper.d(TAG, "startAffectiveService")
        mEnterAffectiveCloudManager?.init(initListener) ?: run {
            AffectiveLogHelper.e(TAG, "startAffectiveService mEnterAffectiveCloudManager is null")
        }
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
        startAffectiveService(initListener)
    }

    override fun restoreAffectiveService(listener: IStartAffectiveServiceLister) {
        AffectiveLogHelper.d(TAG, "restoreAffectiveService")
        mEnterAffectiveCloudManager?.restore(listener) ?: run {
            AffectiveLogHelper.e(TAG, "restoreAffectiveService mEnterAffectiveCloudManager is null")
        }
    }

    override fun checkEnvironment(context: Context): Boolean {
        return NetworkUtils.isNetworkConnected(context)
    }

    override fun subscribeData(
        bdListener: ((RealtimeBioData?) -> Unit)?,
        listener: ((RealtimeAffectiveData?) -> Unit)?
    ) {
        AffectiveLogHelper.d(TAG, "subscribeData")
        bdListener?.apply {
            mEnterAffectiveCloudManager?.addBiodataRealtimeListener(this) ?: run {
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

    override fun <R> readFileAnalysisData(
        inputStream: InputStream,
        appSingleData: ((R) -> Boolean)?,
        appendAllData: (List<R>) -> Unit,
        case: (String) -> R,
        callback: Callback
    ) {
        callback.onError(Error(-1, "not support this method"))
        throw IllegalAccessError("not support this method")
    }

    override fun appendEEGData(brainData: Int, isEar: Boolean) {
        throw IllegalAccessError("not support this method")
    }

    override fun appendEEGData(brainData: List<Int>, isEar: Boolean) {
        throw IllegalAccessError("not support this method")
    }

    override fun appendSCEEGData(brainData: Int, isEar: Boolean) {
        throw IllegalAccessError("not support this method")
    }

    override fun appendSCEEGData(brainData: List<Int>, isEar: Boolean) {
        throw IllegalAccessError("not support this method")
    }

    override fun appendEEGData(brainData: ByteArray, isEar: Boolean) {
        mEnterAffectiveCloudManager?.appendEEGData(brainData)
    }

    override fun appendDCEEGData(brainData: ByteArray) {
        mEnterAffectiveCloudManager?.appendDCEEGData(brainData)
    }

    override fun appendSCEEGData(brainData: ByteArray, isEar: Boolean) {
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
        if (hasConnectAffectiveService()) {
            mEnterAffectiveCloudManager?.release(listener)
        } else {
            listener.hasFinished()
        }
    }

    override suspend fun suspendFinishAffectiveService() = suspendCoroutine { continuation ->
        val completion = OneShotContinuation(continuation)
        val listener = object : IFinishAffectiveServiceListener {
            override fun finishBioFail(error: Error?) {
                completion.resumeWith(
                    Result.failure(
                        IllegalStateException(error?.toString() ?: "finish affective service failed")
                    )
                )
            }

            override fun finishAffectiveFail(error: Error?) {
                completion.resumeWith(
                    Result.failure(
                        IllegalStateException(error?.toString() ?: "finish affective service failed")
                    )
                )
            }

            override fun finishError(error: Error?) {
                completion.resumeWith(
                    Result.failure(
                        IllegalStateException(error?.toString() ?: "finish affective service failed")
                    )
                )
            }

            override fun finishSuccess() {
                completion.resumeWith(Result.success(Unit))
            }

            override fun hasFinished() {
                super.hasFinished()
                completion.resumeWith(Result.success(Unit))
            }
        }
        finishAffectiveService(listener)
    }

    override fun addServiceConnectStatueListener(
        connectionListener: (() -> Unit)?,
        disconnectListener: ((String) -> Unit)?
    ) {
        connectionListener?.apply {
            mEnterAffectiveCloudManager?.addWebSocketConnectListener(this)
        }
        disconnectListener?.apply {
            mEnterAffectiveCloudManager?.addWebSocketDisconnectListener(this)
        }
    }

    override fun removeServiceConnectStatueListener(
        connectionListener: (() -> Unit)?,
        disconnectListener: ((String) -> Unit)?
    ) {
        connectionListener?.apply {
            mEnterAffectiveCloudManager?.removeWebSocketConnectListener(this)
        }
        disconnectListener?.apply {
            mEnterAffectiveCloudManager?.removeWebSocketDisconnectListener(this)
        }
    }

    override fun hasStartAffectiveService(): Boolean {
//        AffectiveLogHelper.d(TAG, "hasStartBioDataService")
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
    override fun getReport(
        listener: IGetReportListener, isEar: Boolean, needFinishService: Boolean
    ) {
        val manager = mEnterAffectiveCloudManager
        if (manager == null) {
            listener.onError(Error(-1, "affective cloud service is not connected"))
            return
        }
        manager.getBiodataReport(object : Callback2<HashMap<Any, Any?>> {
            override fun onError(error: Error?) {
                AffectiveLogHelper.e(TAG, "getBiodataReport error $error")
                listener.getBioReportError(error)
                manager.getAffectiveDataReport(object :
                    Callback2<HashMap<Any, Any?>> {
                    override fun onError(error: Error?) {
                        AffectiveLogHelper.e(TAG, "getAffectiveDataReport error $error")
                        listener.getAffectiveReportError(error)
                        listener.onError(error)
                    }

                    override fun onSuccess(t: HashMap<Any, Any?>?) {
                        AffectiveLogHelper.e(TAG, "getAffectiveDataReport onSuccess ")
                        if (!needFinishService) {
                            listener.onSuccess(null)
                            return
                        }
                        finishReport(manager, listener)
                    }
                })
            }

            override fun onSuccess(t: HashMap<Any, Any?>?) {
                AffectiveLogHelper.e(TAG, "getBiodataReport onSuccess ")
                manager.getAffectiveDataReport(object :
                    Callback2<HashMap<Any, Any?>> {
                    override fun onError(error: Error?) {
                        AffectiveLogHelper.e(TAG, "getAffectiveDataReport error $error")
                        listener.getAffectiveReportError(error)
                        listener.onError(error)
                    }

                    override fun onSuccess(t: HashMap<Any, Any?>?) {
                        AffectiveLogHelper.e(TAG, "getAffectiveDataReport onSuccess ")
                        if (!needFinishService) {
                            listener.onSuccess(null)
                            return
                        }

                        finishReport(manager, listener)
                    }
                })
            }

        })
    }

    private fun finishReport(
        manager: EnterAffectiveCloudManager,
        listener: IGetReportListener
    ) {
        manager.release(object : IFinishAffectiveServiceListener {
            override fun finishBioFail(error: Error?) {
                listener.onSuccess(null)
            }

            override fun finishAffectiveFail(error: Error?) {
                listener.onSuccess(null)
            }

            override fun finishError(error: Error?) {
                listener.onSuccess(null)
            }

            override fun finishSuccess() {
                listener.onSuccess(null)
            }

            override fun hasFinished() {
                listener.onSuccess(null)
            }
        })
    }

    override suspend fun suspendGetReport(needFinishService: Boolean): UploadReportEntity? =
        suspendCoroutine { continuation ->
            val completion = OneShotContinuation(continuation)
            val listener = object : IGetReportListener {
                override fun onError(error: Error?) {
                    completion.resumeWith(
                        Result.failure(
                            IllegalStateException(error?.toString() ?: "get affective report failed")
                        )
                    )
                }

                override fun onSuccess(entity: UploadReportEntity?) {
                    completion.resumeWith(Result.success(entity))
                }

                override fun getBioReportError(error: Error?) = Unit

                override fun getAffectiveReportError(error: Error?) = Unit
            }
            getReport(listener, false, needFinishService)
        }

    override fun isGoodQuality(quality: Double): Boolean {
        return quality >= 2
    }


    override fun getAffectiveWay() = AffectiveServiceWay.AffectiveCloudService
}
