package cn.entertech.affective.sdk.api

import android.content.Context
import cn.entertech.affective.sdk.bean.AffectiveServiceWay
import cn.entertech.affective.sdk.bean.EnterAffectiveConfigProxy
import cn.entertech.affective.sdk.bean.UploadReportEntity
import java.io.InputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AffectiveAlgorithm {
    val way: AffectiveServiceWay
    val state: StateFlow<AffectiveAlgorithmState>
    val realtimeEvents: Flow<AffectiveRealtimeEvent>

    suspend fun connect(
        config: EnterAffectiveConfigProxy = EnterAffectiveConfigProxy(),
    ): AffectiveAlgorithmResult<Unit>

    suspend fun start(
        authenticationInputStream: InputStream? = null,
        context: Context? = null,
    ): AffectiveAlgorithmResult<Unit>

    suspend fun report(
        isEar: Boolean = false,
        needFinishService: Boolean = false,
    ): AffectiveAlgorithmResult<UploadReportEntity?>

    suspend fun finish(): AffectiveAlgorithmResult<Unit>

    fun append(input: AffectiveInput): AffectiveAlgorithmResult<Unit>

    fun disconnect()
}
