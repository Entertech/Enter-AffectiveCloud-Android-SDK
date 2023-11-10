package cn.entertech.affective.sdk.bean

data class RealtimeSCEEGData(
    val sceegWave: List<Double> = emptyList(),
    val sceegAlphaPower: Double = 0.0,
    val sceegBetaPower: Double = 0.0,
    val sceegThetaPower: Double = 0.0,
    val sceegDeltaPower: Double = 0.0,
    val sceegGammaPower: Double = 0.0,
    val sceegQuality: Double = 0.0,
)
