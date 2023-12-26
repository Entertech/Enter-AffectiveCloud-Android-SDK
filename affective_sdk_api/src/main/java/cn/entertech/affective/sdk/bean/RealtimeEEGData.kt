package cn.entertech.affective.sdk.bean

import java.io.Serializable

class RealtimeEEGData: Serializable {
    /**
     * 经过滤波后的左通道实时脑电波，左右通道各一个数组，长度为150，对应0.6秒内的脑电波形；数值范围[-500, 500]，信号质量不佳时全为0
     * */
    var leftwave: ArrayList<Double>? = null

    /**
     * 经过滤波后的右通道实时脑电波，左右通道各一个数组，长度为150，对应0.6秒内的脑电波形；数值范围[-500, 500]，信号质量不佳时全为0
     * */
    var rightwave: ArrayList<Double>? = null

    //5种脑电波节律的能量分贝值：α波、β波、θ波、δ波、γ波
    // 5种脑电波节律各一个数值，数值范围[0, +∞)，初始阶段与信号质量不佳时返回为0

    var alphaPower: Double? = null
    var betaPower: Double? = null
    var thetaPower: Double? = null
    var deltaPower: Double? = null
    var gammaPower: Double? = null

    /**
     * 脑电信号质量等级 >=1表示脑电信号质量良好
     * */
    var quality: Double? = null

    override fun toString(): String {
        return "RealtimeEEGDataEntity(leftwave=$leftwave, rightwave=$rightwave, alphaPower=$alphaPower, betaPower=$betaPower, thetaPower=$thetaPower, deltaPower=$deltaPower, gammaPower=$gammaPower, quality=$quality)"
    }

}