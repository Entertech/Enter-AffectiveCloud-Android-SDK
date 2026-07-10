package cn.entertech.affective.sdk.manager

import android.content.Context
import cn.entertech.affective.sdk.api.Callback
import cn.entertech.affective.sdk.api.IAffectiveDataAnalysisService
import cn.entertech.affective.sdk.api.IConnectionServiceListener
import cn.entertech.affective.sdk.api.IFinishAffectiveServiceListener
import cn.entertech.affective.sdk.api.IGetReportListener
import cn.entertech.affective.sdk.api.IStartAffectiveServiceLister
import cn.entertech.affective.sdk.bean.AffectiveServiceWay
import cn.entertech.affective.sdk.bean.EnterAffectiveConfigProxy
import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData
import cn.entertech.affective.sdk.bean.UploadReportEntity
import java.io.InputStream

internal class FakeAffectiveService : IAffectiveDataAnalysisService {
    var connected = true
    var started = true
    val eegSamples = mutableListOf<Pair<List<Int>, Boolean>>()
    val heartRateSamples = mutableListOf<Int>()
    var failOnEeg = false
    var finishListener: IFinishAffectiveServiceListener? = null
    var bioListener: ((RealtimeBioData?) -> Unit)? = null
    var affectiveListener: ((RealtimeAffectiveData?) -> Unit)? = null

    override fun connectAffectiveServiceConnection(listener: IConnectionServiceListener?, configProxy: EnterAffectiveConfigProxy) {
        connected = true
        listener?.connectionSuccess("fake-session")
    }

    override fun closeAffectiveServiceConnection() { connected = false }
    override fun hasConnectAffectiveService() = connected
    override fun addServiceConnectStatueListener(connectionListener: (() -> Unit)?, disconnectListener: ((String) -> Unit)?) = Unit
    override fun removeServiceConnectStatueListener(connectionListener: (() -> Unit)?, disconnectListener: ((String) -> Unit)?) = Unit
    override fun getSessionId() = "fake-session"
    override fun startAffectiveService(initListener: IStartAffectiveServiceLister) { started = true; initListener.startSuccess() }
    override fun startAffectiveService(authenticationInputStream: InputStream?, context: Context?, initListener: IStartAffectiveServiceLister) = startAffectiveService(initListener)
    override fun restoreAffectiveService(listener: IStartAffectiveServiceLister) = startAffectiveService(listener)
    override fun finishAffectiveService(listener: IFinishAffectiveServiceListener) { finishListener = listener }
    override suspend fun suspendFinishAffectiveService() { started = false }
    override fun getReport(listener: IGetReportListener, isEar: Boolean, needFinishService: Boolean) { listener.onSuccess(null) }
    override suspend fun suspendGetReport(needFinishService: Boolean): UploadReportEntity? = null
    override fun hasStartAffectiveService() = started
    override fun subscribeData(bdListener: ((RealtimeBioData?) -> Unit)?, listener: ((RealtimeAffectiveData?) -> Unit)?) {
        bioListener = bdListener
        affectiveListener = listener
    }
    override fun unSubscribeData(bdListener: ((RealtimeBioData?) -> Unit)?, listener: ((RealtimeAffectiveData?) -> Unit)?) {
        if (bioListener === bdListener) bioListener = null
        if (affectiveListener === listener) affectiveListener = null
    }
    override fun <R> readFileAnalysisData(inputStream: InputStream, appSingleData: ((R) -> Boolean)?, appendAllData: (List<R>) -> Unit, case: (String) -> R, callback: Callback) = Unit
    override fun isGoodQuality(quality: Double) = true
    override fun appendEEGData(brainData: ByteArray, isEar: Boolean) = appendEEGData(brainData.map(Byte::toInt), isEar)
    override fun appendEEGData(brainData: Int, isEar: Boolean) = appendEEGData(listOf(brainData), isEar)
    override fun appendEEGData(brainData: List<Int>, isEar: Boolean) {
        if (failOnEeg) error("expected fake failure")
        eegSamples += brainData.toList() to isEar
    }
    override fun appendDCEEGData(brainData: ByteArray) = Unit
    override fun appendSCEEGData(brainData: ByteArray, isEar: Boolean) = Unit
    override fun appendSCEEGData(brainData: Int, isEar: Boolean) = Unit
    override fun appendSCEEGData(brainData: List<Int>, isEar: Boolean) = Unit
    override fun appendHeartRateData(heartRateData: Int) { heartRateSamples += heartRateData }
    override fun appendMCEEGData(mceegData: ByteArray) = Unit
    override fun appendPEPRData(peprData: ByteArray) = Unit
    override fun appendBCGData(bcgData: ByteArray, packageCount: Int) = Unit
    override fun appendGyroData(gyroData: ByteArray, packageCount: Int) = Unit
    override fun getAffectiveWay() = AffectiveServiceWay.AffectiveLocalService
}
