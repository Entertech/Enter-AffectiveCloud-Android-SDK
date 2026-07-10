package cn.entertech.affective.sdk.api

sealed interface AffectiveInput {
    data class EegBytes(
        val data: ByteArray,
        val isEar: Boolean = false,
    ) : AffectiveInput

    data class EegSamples(
        val data: List<Int>,
        val isEar: Boolean = false,
    ) : AffectiveInput

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
