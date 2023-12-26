package cn.entertech.affective.sdk.bean

import java.io.Serializable

data class RealtimeSCEEGData(
    /**
     * 经过滤波后的通道实时脑电波，一个数组，长度为150，对应0.6秒内的脑电波形；
     * 数值范围[-500, 500]，信号质量不佳时全为0
     * */
    val sceegWave: List<Double> = emptyList(),
    //5种脑电波节律的能量分贝值：α波、β波、θ波、δ波、γ波
    //5种脑电波节律各一个数值，数值范围[0, +∞)，初始阶段与信号质量不佳时返回为0
    val sceegAlphaPower: Double = 0.0,
    val sceegBetaPower: Double = 0.0,
    val sceegThetaPower: Double = 0.0,
    val sceegDeltaPower: Double = 0.0,
    val sceegGammaPower: Double = 0.0,
    /**
     * 脑电信号质量等级
     * 大于等于1表示脑电信号质量良好
     * */
    val sceegQuality: Double = 0.0,
): Serializable
