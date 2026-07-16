package cn.entertech.affective.sdk.api

sealed interface AffectiveInput {
    data class EegBytes(
        val data: ByteArray,
        val isEar: Boolean = false,
    ) : AffectiveInput

    data class EegSample(
        val value: Int,
        val isEar: Boolean = false,
    ) : AffectiveInput

    data class EegSamples(
        val data: List<Int>,
        val isEar: Boolean = false,
    ) : AffectiveInput

    data class SceegBytes(
        val data: ByteArray,
        val isEar: Boolean = false,
    ) : AffectiveInput

    data class SceegSample(
        val value: Int,
        val isEar: Boolean = false,
    ) : AffectiveInput

    data class SceegSamples(
        val data: List<Int>,
        val isEar: Boolean = false,
    ) : AffectiveInput

    data class DcEeg(val data: ByteArray) : AffectiveInput
    data class McEeg(val data: ByteArray) : AffectiveInput
    data class HeartRate(val value: Int) : AffectiveInput
    data class Pepr(val data: ByteArray) : AffectiveInput
    data class Bcg(
        val data: ByteArray,
        val packageCount: Int = IAffectiveDataAnalysisService.UPLOAD_BCG_PACKAGE_COUNT,
    ) : AffectiveInput

    data class Gyro(
        val data: ByteArray,
        val packageCount: Int = IAffectiveDataAnalysisService.UPLOAD_GYRO_PACKAGE_COUNT,
    ) : AffectiveInput
}
