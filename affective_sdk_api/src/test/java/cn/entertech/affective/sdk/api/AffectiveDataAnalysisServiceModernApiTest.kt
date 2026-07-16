package cn.entertech.affective.sdk.api

import android.content.Context
import cn.entertech.affective.sdk.bean.AffectiveServiceWay
import cn.entertech.affective.sdk.bean.EnterAffectiveConfigProxy
import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData
import cn.entertech.affective.sdk.bean.UploadReportEntity
import cn.entertech.affective.sdk.bean.type.IDataType
import cn.entertech.device.api.IDeviceType
import java.io.InputStream
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AffectiveDataAnalysisServiceModernApiTest {
    @Test
    fun coroutineAndFlowApiUsesDataAnalysisServiceDirectly() = runBlocking {
        val service = FakeAffectiveService()

        val connect = service.awaitConnectAffectiveService(EnterAffectiveConfigProxy())
        val start = service.awaitStartAffectiveService()
        val report = service.awaitGetReport()
        val finish = service.awaitFinishAffectiveService()

        assertTrue(connect is AffectiveServiceResult.Success)
        assertTrue(start is AffectiveServiceResult.Success)
        assertTrue(report is AffectiveServiceResult.Success)
        assertTrue(finish is AffectiveServiceResult.Success)
        assertEquals(listOf("connect", "start", "report", "finish"), service.calls)
    }

    @Test
    fun appendInputRoutesThroughDataAnalysisServiceDirectly() {
        val service = FakeAffectiveService()

        val result = service.append(AffectiveInput.HeartRate(72))

        assertTrue(result is AffectiveServiceResult.Success)
        assertEquals(listOf("hr:72"), service.calls)
    }

    @Test
    fun realtimeEventsSubscribeAndUnsubscribeThroughDataAnalysisServiceDirectly() = runBlocking {
        val service = FakeAffectiveService()
        val bioData = RealtimeBioData()
        var event: AffectiveRealtimeEvent? = null

        val collectJob = launch {
            event = service.realtimeEvents(sourceKey = "local").first()
        }
        while (service.bioListener == null) {
            kotlinx.coroutines.yield()
        }
        service.bioListener?.invoke(bioData)
        collectJob.join()

        assertEquals(AffectiveRealtimeEvent.BioData("local", bioData), event)
        assertEquals(listOf("subscribe", "unsubscribe"), service.calls)
    }

    private class FakeAffectiveService : IAffectiveDataAnalysisService {
        val calls = mutableListOf<String>()
        var bioListener: ((RealtimeBioData?) -> Unit)? = null
        private var affectiveListener: ((RealtimeAffectiveData?) -> Unit)? = null

        override fun connectAffectiveServiceConnection(
            listener: IConnectionServiceListener?,
            configProxy: EnterAffectiveConfigProxy,
        ) {
            calls += "connect"
            listener?.connectionSuccess("session")
        }

        override fun closeAffectiveServiceConnection() {
            calls += "disconnect"
        }

        override fun hasConnectAffectiveService(): Boolean = true

        override fun addServiceConnectStatueListener(
            connectionListener: (() -> Unit)?,
            disconnectListener: ((String) -> Unit)?,
        ) = Unit

        override fun removeServiceConnectStatueListener(
            connectionListener: (() -> Unit)?,
            disconnectListener: ((String) -> Unit)?,
        ) = Unit

        override fun getSessionId(): String = "session"

        override fun startAffectiveService(initListener: IStartAffectiveServiceLister) {
            calls += "start"
            initListener.startSuccess()
        }

        override fun startAffectiveService(
            authenticationInputStream: InputStream?,
            context: Context?,
            initListener: IStartAffectiveServiceLister,
        ) = startAffectiveService(initListener)

        override fun restoreAffectiveService(listener: IStartAffectiveServiceLister) {
            listener.startSuccess()
        }

        override fun finishAffectiveService(listener: IFinishAffectiveServiceListener) {
            calls += "finish"
            listener.finishSuccess()
        }

        override suspend fun suspendFinishAffectiveService() = Unit

        override fun getReport(
            listener: IGetReportListener,
            isEar: Boolean,
            needFinishService: Boolean,
        ) {
            calls += "report"
            listener.onSuccess(UploadReportEntity())
        }

        override suspend fun suspendGetReport(needFinishService: Boolean): UploadReportEntity? =
            UploadReportEntity()

        override fun hasStartAffectiveService(): Boolean = true

        override fun subscribeData(
            bdListener: ((RealtimeBioData?) -> Unit)?,
            listener: ((RealtimeAffectiveData?) -> Unit)?,
        ) {
            calls += "subscribe"
            bioListener = bdListener
            affectiveListener = listener
        }

        override fun unSubscribeData(
            bdListener: ((RealtimeBioData?) -> Unit)?,
            listener: ((RealtimeAffectiveData?) -> Unit)?,
        ) {
            calls += "unsubscribe"
            if (bioListener === bdListener) {
                bioListener = null
            }
            if (affectiveListener === listener) {
                affectiveListener = null
            }
        }

        override fun <R> readFileAnalysisData(
            inputStream: InputStream,
            appSingleData: ((R) -> Boolean)?,
            appendAllData: (List<R>) -> Unit,
            case: (String) -> R,
            callback: Callback,
        ) = callback.onSuccess()

        override fun isGoodQuality(quality: Double): Boolean = true
        override fun appendDevicePosition(device: IDeviceType, position: String) = Unit
        override fun appendEEGData(brainData: ByteArray, isEar: Boolean) = Unit
        override fun appendEEGData(brainData: Int, isEar: Boolean) = Unit
        override fun appendEEGData(brainData: List<Int>, isEar: Boolean) = Unit
        override fun appendDCEEGData(brainData: ByteArray) = Unit
        override fun appendSCEEGData(brainData: ByteArray, isEar: Boolean) = Unit
        override fun appendSCEEGData(brainData: Int, isEar: Boolean) = Unit
        override fun appendSCEEGData(brainData: List<Int>, isEar: Boolean) = Unit

        override fun appendHeartRateData(heartRateData: Int) {
            calls += "hr:$heartRateData"
        }

        override fun appendMCEEGData(mceegData: ByteArray) = Unit
        override fun appendPEPRData(peprData: ByteArray) = Unit
        override fun appendBCGData(bcgData: ByteArray, packageCount: Int) = Unit
        override fun appendGyroData(gyroData: ByteArray, packageCount: Int) = Unit
        override fun getAffectiveWay(): AffectiveServiceWay = AffectiveServiceWay.AffectiveLocalService
    }
}
