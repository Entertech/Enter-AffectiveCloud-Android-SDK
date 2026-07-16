package cn.entertech.affective.sdk.api

import android.content.Context
import cn.entertech.affective.sdk.bean.AffectiveServiceWay
import cn.entertech.affective.sdk.bean.EnterAffectiveConfigProxy
import cn.entertech.affective.sdk.bean.Error
import cn.entertech.affective.sdk.bean.UploadReportEntity
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Coroutine/Flow adapter for the stable [IAffectiveDataAnalysisService] entry point.
 *
 * Business modules still depend on the concrete implementation component they need
 * (`affective_local_sdk` or `affective_cloud_sdk`) and obtain an
 * [IAffectiveDataAnalysisService] from the API module. This adapter only adds a
 * modern Android calling style on top of that existing service instance.
 */
open class AffectiveServiceAlgorithmAdapter(
    protected val service: IAffectiveDataAnalysisService,
    private val sourceKey: String = service.getAffectiveWay().name,
) : AffectiveAlgorithm {
    private val mutableState = MutableStateFlow(AffectiveAlgorithmState.Idle)

    override val way: AffectiveServiceWay get() = service.getAffectiveWay()
    override val state: StateFlow<AffectiveAlgorithmState> = mutableState.asStateFlow()

    override val realtimeEvents: Flow<AffectiveRealtimeEvent> = callbackFlow {
        val bioListener: (cn.entertech.affective.sdk.bean.RealtimeBioData?) -> Unit = {
            trySend(AffectiveRealtimeEvent.BioData(sourceKey, it))
        }
        val affectiveListener: (cn.entertech.affective.sdk.bean.RealtimeAffectiveData?) -> Unit = {
            trySend(AffectiveRealtimeEvent.AffectiveData(sourceKey, it))
        }
        service.subscribeData(bioListener, affectiveListener)
        awaitClose { service.unSubscribeData(bioListener, affectiveListener) }
    }

    override suspend fun connect(config: EnterAffectiveConfigProxy): AffectiveAlgorithmResult<Unit> =
        suspendCancellableCoroutine { continuation ->
            mutableState.value = AffectiveAlgorithmState.Connecting
            val completed = AtomicBoolean(false)
            fun complete(result: AffectiveAlgorithmResult<Unit>, state: AffectiveAlgorithmState) {
                if (completed.compareAndSet(false, true) && continuation.isActive) {
                    mutableState.value = state
                    continuation.resume(result)
                }
            }
            service.connectAffectiveServiceConnection(
                object : IConnectionServiceListener {
                    override fun connectionSuccess(sessionId: String?) = complete(
                        AffectiveAlgorithmResult.Success(Unit),
                        AffectiveAlgorithmState.Connected,
                    )

                    override fun hasConnected(sessionId: String) = connectionSuccess(sessionId)

                    override fun connectionError(error: Error?) = complete(
                        AffectiveAlgorithmResult.Failure(error.orUnknown("connect failed")),
                        AffectiveAlgorithmState.Failed,
                    )
                },
                config,
            )
        }

    override suspend fun start(
        authenticationInputStream: InputStream?,
        context: Context?,
    ): AffectiveAlgorithmResult<Unit> = suspendCancellableCoroutine { continuation ->
        mutableState.value = AffectiveAlgorithmState.Starting
        val completed = AtomicBoolean(false)
        fun complete(result: AffectiveAlgorithmResult<Unit>, state: AffectiveAlgorithmState) {
            if (completed.compareAndSet(false, true) && continuation.isActive) {
                mutableState.value = state
                continuation.resume(result)
            }
        }
        val listener = object : IStartAffectiveServiceLister {
            override fun startSuccess() = complete(
                AffectiveAlgorithmResult.Success(Unit),
                AffectiveAlgorithmState.Running,
            )

            override fun hasStarted() = startSuccess()
            override fun startFail(error: Error?) = fail(error)
            override fun startBioFail(error: Error?) = fail(error)
            override fun startAffectionFail(error: Error?) = fail(error)

            private fun fail(error: Error?) = complete(
                AffectiveAlgorithmResult.Failure(error.orUnknown("start failed")),
                AffectiveAlgorithmState.Failed,
            )
        }
        if (authenticationInputStream == null && context == null) {
            service.startAffectiveService(listener)
        } else {
            service.startAffectiveService(authenticationInputStream, context, listener)
        }
    }

    override suspend fun report(
        isEar: Boolean,
        needFinishService: Boolean,
    ): AffectiveAlgorithmResult<UploadReportEntity?> = suspendCancellableCoroutine { continuation ->
        val completed = AtomicBoolean(false)
        fun complete(result: AffectiveAlgorithmResult<UploadReportEntity?>) {
            if (completed.compareAndSet(false, true) && continuation.isActive) {
                continuation.resume(result)
            }
        }
        service.getReport(
            object : IGetReportListener {
                override fun onSuccess(entity: UploadReportEntity?) =
                    complete(AffectiveAlgorithmResult.Success(entity))

                override fun onError(error: Error?) = fail(error)
                override fun getBioReportError(error: Error?) = fail(error)
                override fun getAffectiveReportError(error: Error?) = fail(error)

                private fun fail(error: Error?) = complete(
                    AffectiveAlgorithmResult.Failure(error.orUnknown("report failed")),
                )
            },
            isEar,
            needFinishService,
        )
    }

    override suspend fun finish(): AffectiveAlgorithmResult<Unit> =
        suspendCancellableCoroutine { continuation ->
            mutableState.value = AffectiveAlgorithmState.Finishing
            val completed = AtomicBoolean(false)
            fun complete(result: AffectiveAlgorithmResult<Unit>, state: AffectiveAlgorithmState) {
                if (completed.compareAndSet(false, true) && continuation.isActive) {
                    mutableState.value = state
                    continuation.resume(result)
                }
            }
            service.finishAffectiveService(object : IFinishAffectiveServiceListener {
                override fun finishSuccess() = complete(
                    AffectiveAlgorithmResult.Success(Unit),
                    AffectiveAlgorithmState.Finished,
                )

                override fun hasFinished() = finishSuccess()
                override fun finishError(error: Error?) = complete(
                    AffectiveAlgorithmResult.Failure(error.orUnknown("finish failed")),
                    AffectiveAlgorithmState.Failed,
                )
            })
        }

    override fun append(input: AffectiveInput): AffectiveAlgorithmResult<Unit> = try {
        when (input) {
            is AffectiveInput.EegBytes -> service.appendEEGData(input.data, input.isEar)
            is AffectiveInput.EegSample -> service.appendEEGData(input.value, input.isEar)
            is AffectiveInput.EegSamples -> service.appendEEGData(input.data, input.isEar)
            is AffectiveInput.SceegBytes -> service.appendSCEEGData(input.data, input.isEar)
            is AffectiveInput.SceegSample -> service.appendSCEEGData(input.value, input.isEar)
            is AffectiveInput.SceegSamples -> service.appendSCEEGData(input.data, input.isEar)
            is AffectiveInput.DcEeg -> service.appendDCEEGData(input.data)
            is AffectiveInput.McEeg -> service.appendMCEEGData(input.data)
            is AffectiveInput.HeartRate -> service.appendHeartRateData(input.value)
            is AffectiveInput.Pepr -> service.appendPEPRData(input.data)
            is AffectiveInput.Bcg -> service.appendBCGData(input.data, input.packageCount)
            is AffectiveInput.Gyro -> service.appendGyroData(input.data, input.packageCount)
        }
        AffectiveAlgorithmResult.Success(Unit)
    } catch (throwable: Throwable) {
        AffectiveAlgorithmResult.Failure(Error(-1, throwable.message ?: "append failed"))
    }

    override fun disconnect() {
        service.closeAffectiveServiceConnection()
        mutableState.value = AffectiveAlgorithmState.Disconnected
    }

    private fun Error?.orUnknown(message: String): Error = this ?: Error(-1, message)
}

fun IAffectiveDataAnalysisService.asAffectiveAlgorithm(
    sourceKey: String = getAffectiveWay().name,
): AffectiveAlgorithm = AffectiveServiceAlgorithmAdapter(this, sourceKey)
