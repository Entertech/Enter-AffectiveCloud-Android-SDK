package cn.entertech.affective.sdk.api

import android.content.Context
import cn.entertech.affective.sdk.bean.EnterAffectiveConfigProxy
import cn.entertech.affective.sdk.bean.Error
import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData
import cn.entertech.affective.sdk.bean.UploadReportEntity
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

fun IAffectiveDataAnalysisService.realtimeEvents(
    sourceKey: String = getAffectiveWay().name,
): Flow<AffectiveRealtimeEvent> = callbackFlow {
    val bioListener: (RealtimeBioData?) -> Unit = {
        trySend(AffectiveRealtimeEvent.BioData(sourceKey, it))
    }
    val affectiveListener: (RealtimeAffectiveData?) -> Unit = {
        trySend(AffectiveRealtimeEvent.AffectiveData(sourceKey, it))
    }
    subscribeData(bioListener, affectiveListener)
    awaitClose { unSubscribeData(bioListener, affectiveListener) }
}

suspend fun IAffectiveDataAnalysisService.awaitConnectAffectiveService(
    config: EnterAffectiveConfigProxy = EnterAffectiveConfigProxy(),
): AffectiveServiceResult<Unit> =
    suspendCancellableCoroutine { continuation ->
        val completed = AtomicBoolean(false)
        fun complete(result: AffectiveServiceResult<Unit>) {
            if (completed.compareAndSet(false, true) && continuation.isActive) {
                continuation.resume(result)
            }
        }
        connectAffectiveServiceConnection(
            object : IConnectionServiceListener {
                override fun connectionSuccess(sessionId: String?) =
                    complete(AffectiveServiceResult.Success(Unit))

                override fun hasConnected(sessionId: String) = connectionSuccess(sessionId)

                override fun connectionError(error: Error?) =
                    complete(AffectiveServiceResult.Failure(error.orUnknown("connect failed")))
            },
            config,
        )
    }

suspend fun IAffectiveDataAnalysisService.awaitStartAffectiveService(
    authenticationInputStream: InputStream? = null,
    context: Context? = null,
): AffectiveServiceResult<Unit> =
    suspendCancellableCoroutine { continuation ->
        val completed = AtomicBoolean(false)
        fun complete(result: AffectiveServiceResult<Unit>) {
            if (completed.compareAndSet(false, true) && continuation.isActive) {
                continuation.resume(result)
            }
        }
        val listener = object : IStartAffectiveServiceLister {
            override fun startSuccess() = complete(AffectiveServiceResult.Success(Unit))
            override fun hasStarted() = startSuccess()
            override fun startFail(error: Error?) = fail(error)
            override fun startBioFail(error: Error?) = fail(error)
            override fun startAffectionFail(error: Error?) = fail(error)

            private fun fail(error: Error?) =
                complete(AffectiveServiceResult.Failure(error.orUnknown("start failed")))
        }
        if (authenticationInputStream == null && context == null) {
            startAffectiveService(listener)
        } else {
            startAffectiveService(authenticationInputStream, context, listener)
        }
    }

suspend fun IAffectiveDataAnalysisService.awaitGetReport(
    isEar: Boolean = false,
    needFinishService: Boolean = false,
): AffectiveServiceResult<UploadReportEntity?> =
    suspendCancellableCoroutine { continuation ->
        val completed = AtomicBoolean(false)
        fun complete(result: AffectiveServiceResult<UploadReportEntity?>) {
            if (completed.compareAndSet(false, true) && continuation.isActive) {
                continuation.resume(result)
            }
        }
        getReport(
            object : IGetReportListener {
                override fun onSuccess(entity: UploadReportEntity?) =
                    complete(AffectiveServiceResult.Success(entity))

                override fun onError(error: Error?) = fail(error)
                override fun getBioReportError(error: Error?) = fail(error)
                override fun getAffectiveReportError(error: Error?) = fail(error)

                private fun fail(error: Error?) =
                    complete(AffectiveServiceResult.Failure(error.orUnknown("report failed")))
            },
            isEar,
            needFinishService,
        )
    }

suspend fun IAffectiveDataAnalysisService.awaitFinishAffectiveService(): AffectiveServiceResult<Unit> =
    suspendCancellableCoroutine { continuation ->
        val completed = AtomicBoolean(false)
        fun complete(result: AffectiveServiceResult<Unit>) {
            if (completed.compareAndSet(false, true) && continuation.isActive) {
                continuation.resume(result)
            }
        }
        finishAffectiveService(object : IFinishAffectiveServiceListener {
            override fun finishSuccess() = complete(AffectiveServiceResult.Success(Unit))
            override fun hasFinished() = finishSuccess()
            override fun finishError(error: Error?) =
                complete(AffectiveServiceResult.Failure(error.orUnknown("finish failed")))
        })
    }

@Deprecated(
    message = "append(AffectiveInput) is now part of IAffectiveDataAnalysisService.",
    replaceWith = ReplaceWith("append(input)"),
)
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun IAffectiveDataAnalysisService.append(input: AffectiveInput): AffectiveServiceResult<Unit> =
    this.append(input)

private fun Error?.orUnknown(message: String): Error = this ?: Error(-1, message)
