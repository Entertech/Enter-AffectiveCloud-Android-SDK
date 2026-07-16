package cn.entertech.affective.sdk.api

import android.content.Context
import cn.entertech.affective.sdk.bean.AffectiveServiceWay
import cn.entertech.affective.sdk.bean.EnterAffectiveConfigProxy
import cn.entertech.affective.sdk.bean.Error
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
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class AffectiveServiceAlgorithmAdapterTest {
    @Test
    fun lifecycleCallbacksAreExposedAsCoroutineResults() = runBlocking {
        val service = FakeAffectiveService()
        val algorithm = service.asAffectiveAlgorithm(sourceKey = "local")

        val connect = algorithm.connect(EnterAffectiveConfigProxy())
        val start = algorithm.start()
        val report = algorithm.report()
        val finish = algorithm.finish()

        assertTrue(connect is AffectiveAlgorithmResult.Success)
        assertTrue(start is AffectiveAlgorithmResult.Success)
        assertTrue(report is AffectiveAlgorithmResult.Success)
        assertTrue(finish is AffectiveAlgorithmResult.Success)
        assertEquals(AffectiveAlgorithmState.Finished, algorithm.state.value)
        assertEquals(listOf("connect", "start", "report", "finish"), service.calls)
    }

    @Test
    fun appendRoutesStructuredInputsToLegacyService() {
        val service = FakeAffectiveService()
        val algorithm = service.asAffectiveAlgorithm(sourceKey = "local")

        algorithm.append(AffectiveInput.EegBytes(byteArrayOf(1), isEar = true))
        algorithm.append(AffectiveInput.EegSample(2, isEar = true))
        algorithm.append(AffectiveInput.EegSamples(listOf(3), isEar = false))
        algorithm.append(AffectiveInput.SceegBytes(byteArrayOf(4), isEar = true))
        algorithm.append(AffectiveInput.SceegSample(5, isEar = false))
        algorithm.append(AffectiveInput.SceegSamples(listOf(6), isEar = true))
        algorithm.append(AffectiveInput.DcEeg(byteArrayOf(7)))
        algorithm.append(AffectiveInput.McEeg(byteArrayOf(8)))
        algorithm.append(AffectiveInput.HeartRate(72))
        algorithm.append(AffectiveInput.Pepr(byteArrayOf(9)))
        algorithm.append(AffectiveInput.Bcg(byteArrayOf(10), packageCount = 11))
        algorithm.append(AffectiveInput.Gyro(byteArrayOf(12), packageCount = 13))

        assertEquals(
            listOf(
                "eeg-bytes:true",
                "eeg-int:true",
                "eeg-list:false",
                "sceeg-bytes:true",
                "sceeg-int:false",
                "sceeg-list:true",
                "dceeg",
                "mceeg",
                "hr:72",
                "pepr",
                "bcg:11",
                "gyro:13",
            ),
            service.calls,
        )
    }

    @Test
    fun realtimeEventsSubscribeAndUnsubscribeThroughServiceCallbacks() = runBlocking {
        val service = FakeAffectiveService()
        val algorithm = service.asAffectiveAlgorithm(sourceKey = "local")
        val bioData = RealtimeBioData()

        val collectJob = launch {
            val event = algorithm.realtimeEvents.first()
            assertTrue(event is AffectiveRealtimeEvent.BioData)
            assertSame(bioData, (event as AffectiveRealtimeEvent.BioData).data)
        }

        while (service.bioListener == null) {
            kotlinx.coroutines.yield()
        }
        service.bioListener?.invoke(bioData)
        collectJob.join()

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
        override fun appendEEGData(brainData: ByteArray, isEar: Boolean) {
            calls += "eeg-bytes:$isEar"
        }

        override fun appendEEGData(brainData: Int, isEar: Boolean) {
            calls += "eeg-int:$isEar"
        }

        override fun appendEEGData(brainData: List<Int>, isEar: Boolean) {
            calls += "eeg-list:$isEar"
        }

        override fun appendDCEEGData(brainData: ByteArray) {
            calls += "dceeg"
        }

        override fun appendSCEEGData(brainData: ByteArray, isEar: Boolean) {
            calls += "sceeg-bytes:$isEar"
        }

        override fun appendSCEEGData(brainData: Int, isEar: Boolean) {
            calls += "sceeg-int:$isEar"
        }

        override fun appendSCEEGData(brainData: List<Int>, isEar: Boolean) {
            calls += "sceeg-list:$isEar"
        }

        override fun appendHeartRateData(heartRateData: Int) {
            calls += "hr:$heartRateData"
        }

        override fun appendMCEEGData(mceegData: ByteArray) {
            calls += "mceeg"
        }

        override fun appendPEPRData(peprData: ByteArray) {
            calls += "pepr"
        }

        override fun appendBCGData(bcgData: ByteArray, packageCount: Int) {
            calls += "bcg:$packageCount"
        }

        override fun appendGyroData(gyroData: ByteArray, packageCount: Int) {
            calls += "gyro:$packageCount"
        }

        override fun getAffectiveWay(): AffectiveServiceWay = AffectiveServiceWay.AffectiveLocalService
    }
}
