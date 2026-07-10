package cn.entertech.affective.protocol

data class AffectiveDataPayload(
    val attention: Double? = null,
    val relaxation: Double? = null,
    val pressure: Double? = null,
    val pleasure: Double? = null,
    val arousal: Double? = null,
    val coherence: Double? = null,
    val meditation: Double? = null,
    val meditationTips: Double? = null,
)
