package cn.entertech.affective.protocol

data class BioDataPayload(
    val heartRate: HeartRatePayload? = null,
    val eeg: EegPayload? = null,
)

data class HeartRatePayload(
    val hr: Double? = null,
    val hrv: Double? = null,
)

data class EegPayload(
    val gammaPower: Double? = null,
    val deltaPower: Double? = null,
    val thetaPower: Double? = null,
    val betaPower: Double? = null,
    val alphaPower: Double? = null,
    val quality: Double? = null,
    val leftWave: List<Double>? = null,
    val rightWave: List<Double>? = null,
)
