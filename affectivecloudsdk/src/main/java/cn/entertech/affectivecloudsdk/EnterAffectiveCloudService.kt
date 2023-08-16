package cn.entertech.affectivecloudsdk

import cn.entertech.affective.sdk.api.Callback
import cn.entertech.affective.sdk.api.Callback2
import cn.entertech.affective.sdk.api.IAffectiveDataAnalysisService
import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData

class EnterAffectiveCloudService : IAffectiveDataAnalysisService {
    private lateinit var mEnterAffectiveCloudManager: EnterAffectiveCloudManager

    /**
     * 启动webSocket
     * 创建会话
     * 启动bioDataService，启动成功后订阅数据
     * 启动AffectiveService ，启动成功后订阅数据
     *
     * */
    override fun startAffectiveService(callback: Callback2<String>) {
        mEnterAffectiveCloudManager.init(callback)
    }

    override fun restoreAffectiveService(callback: Callback) {
        mEnterAffectiveCloudManager.restore(callback)
    }

    override fun subscribeData(
        bdListener: ((RealtimeBioData?) -> Unit)?,
        listener: ((RealtimeAffectiveData?) -> Unit)?
    ) {
        bdListener?.apply {
            mEnterAffectiveCloudManager.addBiodataRealtimeListener(this)
        }
        listener?.apply {
            mEnterAffectiveCloudManager.addAffectiveDataRealtimeListener(this)
        }
    }

    override fun unSubscribeData(
        bdListener: ((RealtimeBioData?) -> Unit)?,
        listener: ((RealtimeAffectiveData?) -> Unit)?
    ) {
        bdListener?.apply {
            mEnterAffectiveCloudManager.removeBiodataRealtimeListener(this)
        }
        listener?.apply {
            mEnterAffectiveCloudManager.removeAffectiveRealtimeListener(this)
        }
    }

    override fun appendEEGData(brainData: ByteArray) {
        mEnterAffectiveCloudManager.appendEEGData(brainData)
    }

    override fun appendDCEEGData(brainData: ByteArray) {
        mEnterAffectiveCloudManager.appendDCEEGData(brainData)
    }

    override fun appendSCEEGData(brainData: ByteArray) {
        mEnterAffectiveCloudManager.appendSCEEGData(brainData)
    }

    override fun appendHeartRateData(heartRateData: Int) {
        mEnterAffectiveCloudManager.appendHeartRateData(heartRateData)
    }

    override fun appendMCEEGData(mceegData: ByteArray) {
        mEnterAffectiveCloudManager.appendMCEEGData(mceegData)
    }

    override fun appendPEPRData(peprData: ByteArray) {
        mEnterAffectiveCloudManager.appendPEPRData(peprData)
    }

    override fun appendBCGData(bcgData: ByteArray, packageCount: Int) {
        mEnterAffectiveCloudManager.appendBCGData(bcgData,packageCount)
    }

    override fun appendGyroData(gyroData: ByteArray, packageCount: Int) {
        mEnterAffectiveCloudManager.appendGyroData(gyroData,packageCount)
    }

    override fun closeAffectiveService(callback: Callback) {
        mEnterAffectiveCloudManager.release(callback)
    }

    override fun addServiceConnectStatueListener(
        connectionListener: () -> Unit,
        disconnectListener: (String) -> Unit
    ) {
        mEnterAffectiveCloudManager.addWebSocketConnectListener(connectionListener)
        mEnterAffectiveCloudManager.addWebSocketDisconnectListener(disconnectListener)
    }

    override fun removeServiceConnectStatueListener(
        connectionListener: () -> Unit,
        disconnectListener: (String) -> Unit
    ) {
        mEnterAffectiveCloudManager.removeWebSocketConnectListener(connectionListener)
        mEnterAffectiveCloudManager.removeWebSocketDisconnectListener(disconnectListener)
    }

    override fun hasStartBioDataService(): Boolean {
        return mEnterAffectiveCloudManager.isInited()
    }
}