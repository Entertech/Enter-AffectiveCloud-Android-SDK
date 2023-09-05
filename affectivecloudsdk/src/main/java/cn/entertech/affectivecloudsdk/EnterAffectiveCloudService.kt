package cn.entertech.affectivecloudsdk

import cn.entertech.affective.sdk.bean.EnterAffectiveConfigProxy
import cn.entertech.affective.sdk.api.Callback
import cn.entertech.affective.sdk.api.Callback2
import cn.entertech.affective.sdk.api.IAffectiveDataAnalysisService
import cn.entertech.affective.sdk.bean.AffectiveServiceWay
import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData
import java.util.HashMap
import cn.entertech.affective.sdk.bean.Error
import cn.entertech.affective.sdk.bean.UploadReportEntity
import cn.entertech.affective.sdk.utils.LogUtil
import com.google.auto.service.AutoService
import java.io.File
import java.io.InputStream

@AutoService(IAffectiveDataAnalysisService::class)
class EnterAffectiveCloudService : IAffectiveDataAnalysisService {

    companion object{
        private const val TAG="EnterAffectiveCloudService"
    }

    private  var mEnterAffectiveCloudManager: EnterAffectiveCloudManager?=null

    /**
     * 启动webSocket
     * 创建会话
     * 启动bioDataService，启动成功后订阅数据
     * 启动AffectiveService ，启动成功后订阅数据
     *
     * */
    override fun startAffectiveService(
        callback: Callback2<String>,builder: EnterAffectiveConfigProxy
    ) {
        LogUtil.d(TAG,"startAffectiveService")
        mEnterAffectiveCloudManager = EnterAffectiveCloudManager(EnterAffectiveCloudConfig.proxyInstance(builder))
        mEnterAffectiveCloudManager?.init(callback)
    }

    override fun restoreAffectiveService(callback: Callback) {
        LogUtil.d(TAG,"restoreAffectiveService")
        mEnterAffectiveCloudManager?.restore(callback)
    }

    override fun subscribeData(
        bdListener: ((RealtimeBioData?) -> Unit)?,
        listener: ((RealtimeAffectiveData?) -> Unit)?
    ) {
        LogUtil.d(TAG,"subscribeData")
        bdListener?.apply {
            mEnterAffectiveCloudManager?.addBiodataRealtimeListener(this)
        }
        listener?.apply {
            mEnterAffectiveCloudManager?.addAffectiveDataRealtimeListener(this)
        }
    }

    override fun unSubscribeData(
        bdListener: ((RealtimeBioData?) -> Unit)?,
        listener: ((RealtimeAffectiveData?) -> Unit)?
    ) {
        LogUtil.d(TAG,"unSubscribeData")
        bdListener?.apply {
            mEnterAffectiveCloudManager?.removeBiodataRealtimeListener(this)
        }
        listener?.apply {
            mEnterAffectiveCloudManager?.removeAffectiveRealtimeListener(this)
        }
    }

    override fun <T> readFileAnalysisData(filePath: String, callback: Callback2<T>,case:(Int)->T?) {
        callback.onError(Error(-1,"not support this method"))
    }

    override fun <T> readFileAnalysisData(inputStream: InputStream, callback: Callback2<T>,case:(Int)->T?) {
        callback.onError(Error(-1,"not support this method"))
    }

    override fun <T> readFileAnalysisData(file: File, callback: Callback2<T>,case:(Int)->T?) {
        callback.onError(Error(-1,"not support this method"))
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

    override fun finishAffectiveService(callback: Callback) {
        LogUtil.d(TAG,"finishAffectiveService")
        mEnterAffectiveCloudManager?.release(callback)
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

    override fun hasStartBioDataService(): Boolean {
        LogUtil.d(TAG,"hasStartBioDataService")
        return mEnterAffectiveCloudManager?.isInited()?:false
    }

    override fun isAffectiveServiceConnect(): Boolean {
        LogUtil.d(TAG,"isAffectiveServiceConnect $mEnterAffectiveCloudManager")
        return mEnterAffectiveCloudManager?.isWebSocketOpen()?:false
    }

    override fun closeAffectiveServiceConnection() {
        mEnterAffectiveCloudManager?.closeWebSocket()
    }

    /**
     * 上传report指令，失败不做处理，成功开始释放资源
     * 释放资源之后不管成功还是失败，关闭websocket，然后http请求报表数据
     * */
    override fun getReport(callback: Callback2<UploadReportEntity>,needFinishService:Boolean) {
        mEnterAffectiveCloudManager?.getBiodataReport(object : Callback2<HashMap<Any, Any?>> {
            override fun onError(error: Error?) {
                callback.onError(error)
            }

            override fun onSuccess(t: HashMap<Any, Any?>?) {
                mEnterAffectiveCloudManager?.getAffectiveDataReport(object :
                    Callback2<HashMap<Any, Any?>> {
                    override fun onError(error: Error?) {
                        callback.onError(error)
                    }

                    override fun onSuccess(t: HashMap<Any, Any?>?) {
                        if (!needFinishService) {
                            callback.onSuccess(null)
                            return
                        }
                        finishAffectiveService(object :Callback{
                            override fun onSuccess() {
                                closeAffectiveServiceConnection()
                                callback.onSuccess(null)
                            }

                            override fun onError(error: Error?) {
                                closeAffectiveServiceConnection()
                                //释放失败也是同样操作，所以也是success
                                callback.onSuccess(null)
                            }
                        })
                    }

                })
            }

        })
    }

    override fun getAffectiveWay()= AffectiveServiceWay.AffectiveCloudService
}