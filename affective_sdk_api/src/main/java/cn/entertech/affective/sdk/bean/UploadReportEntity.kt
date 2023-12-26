package cn.entertech.affective.sdk.bean

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UploadReportEntity(
    val code: Int,
    @SerializedName("data")
    val `data`: Data? = null,
    val msg: String = "",

    @SerializedName("report_version")
    val reportVersion: String = "3",
    @SerializedName("session_id")
    var sessionId: String = "",
    var start: String = "",
    @SerializedName("time_points")
    var timePoints: TimePoints? = null,
    val user_id: Int = 0,
    /**
     * 算法版本
     * */
    val version: Version? = null,
    var deviceString: String? = null,
    /**
     * 设备的mac地址
     * */
    var MAC: String? = null
) : Serializable {
}

data class Data(
    val affective: Affective,
    val biodata: Biodata
) : Serializable

data class TimePoints(
    val affective: AffectiveTimePoints,
    val biodata: BiodataTimePoints
) : Serializable

data class Version(
    val affective: AffectiveVersion,
    val biodata: BiodataVersion
) : Serializable

data class Affective(
    val arousal: Arousal = Arousal(),
    val attention: Attention = Attention(),
    val coherence: Coherence = Coherence(),
    val pleasure: Pleasure = Pleasure(),
    val pressure: Pressure = Pressure(),
    val relaxation: Relaxation = Relaxation(),
    val meditation: Meditation = Meditation(),
    val sleep: Sleep = Sleep()
) : Serializable

data class Biodata(
    val sceegData: Sceeg? = null,
    val eeg: Eeg? = null,
    @SerializedName("hr-v2")
    val hr: HrV2? = null,
    val pepr: PEPR?
) : Serializable

data class PEPR(
    @SerializedName("hr_avg")
    val hrAvg: Int = 0,
    @SerializedName("hr_max")
    val hrMax: Int = 0,
    @SerializedName("hr_min")
    val hrMin: Int = 0,
    @SerializedName("hr_rec")
    val hrRec: List<Int> = ArrayList(),
    @SerializedName("hrv_avg")
    val hrvAvg: Double = 0.0,
    @SerializedName("hrv_rec")
    val hrvRec: List<Double> = ArrayList(),
    @SerializedName("rr_avg")
    val rrAvg: Double = 0.0,
    @SerializedName("rr_rec")
    val rrRec: List<Double> = ArrayList(),
    @SerializedName("bcg_quality_rec")
    val bcgQualityRec: List<Int> = ArrayList(),
    @SerializedName("rw_quality_rec")
    val rwQualityRec: List<Int> = ArrayList()
) : Serializable

data class Sleep(
    /**
     * 睡眠曲线，反映整个体验过程的睡眠情况。睡眠曲线的值越高表明越接近清醒，曲线值越低表明越接近深睡。
     * */
    val sleepCurve: ArrayList<Double> = ArrayList(),
    /**
     * 入睡点时间索引,即入睡时刻在睡眠曲线上的时间轴坐标。数值范围[0, +∞),0表示无效值
     * */
    val sleepPoint: Int = 0,
    /**
     * 入睡用时，单位：秒
     * */
    val sleepLatency: Int = 0,
    /**
     * 清醒时长，单位：秒
     * */
    val awakeDuration: Int = 0,
    /**
     * 浅睡时长，单位：秒
     * */
    val lightDuration: Int = 0,
    /**
     * 深睡时长，单位：秒
     * */
    val deepDuration: Int = 0,
    /**
     * 快速眼动时长
     */
    var remDuration: Int = 0,
    /**
     * 运动次数
     */
    var movementCount: Int = 0,
    /**
     * 惊醒次数
     */
    var arousalCount: Int = 0,
    /**
     * 容差
     */
    var disturbTolerance: Double = 0.0,

    val sleepEegAlphaCurve: List<Double> = ArrayList(),

    val sleepEegBetaCurve: List<Double> =
        ArrayList(),

    val sleepEegThetaCurve: List<Double> =
        ArrayList(),

    val sleepEegDeltaCurve: List<Double> =
        ArrayList(),

    val sleepEegGammaCurve: List<Double> =
        ArrayList(),

    val sleepEegQualityRec: List<Int> =
        ArrayList(),

    val sleepMovementRec: List<Int> =
        ArrayList(),

    val sleepArousalRec: List<Int> = ArrayList()
) : Serializable

data class Arousal(
    /**
     * 全程激活度有效值（除去无效值0）的均值
     * */
    val arousal_avg: Int = 0,
    /**
     * 全程激活度记录
     * */
    val arousal_rec: Any = java.util.ArrayList<Double>()
) : Serializable

data class Attention(
    /**
     * 全程注意力有效值（除去无效值0）的均值
     * */
    @SerializedName("attention_avg")
    val attentionAvg: Double = 0.0,
    /**
     * 全程注意力记录
     * */
    @SerializedName("attention_rec")
    val attentionRec: List<Double> = java.util.ArrayList<Double>()
) : Serializable

data class Coherence(
    /**
     * 全程和谐度有效值（除去无效值0）的均值
     * */
    @SerializedName("coherence_avg")
    val coherenceAvg: Double = 0.0,
    @SerializedName("coherence_duration")
    val coherenceDuration: Int? = 0,
    @SerializedName("coherence_flag")
    val coherenceFlag: List<Int>? = java.util.ArrayList<Int>(),
    /**
     * 全程和谐度记录
     * */
    @SerializedName("coherence_rec")
    val coherenceRec: List<Double> = java.util.ArrayList<Double>()
) : Serializable

data class Pleasure(
    /**
     * 全程愉悦度有效值（除去无效值0）的均值
     * */
    @SerializedName("pleasure_avg")
    val pleasureAvg: Double = 0.0,
    /**
     * 全程压力水平记录
     * */
    @SerializedName("pleasure_rec")
    val pleasureRec: List<Double> = java.util.ArrayList<Double>()
) : Serializable

data class Pressure(
    @SerializedName("pressure_avg")
    val pressureAvg: Double = 0.0,
    @SerializedName("pressure_rec")
    val pressureRec: List<Double> = java.util.ArrayList<Double>()
) : Serializable

data class Relaxation(
    /**
     * 全程放松度有效值（除去无效值0）的均值
     * */
    @SerializedName("relaxation_avg")
    val relaxationAvg: Double = 0.0,
    /**
     * 全程放松度记录
     * */
    @SerializedName("relaxation_rec")
    val relaxationRec: List<Double> = java.util.ArrayList<Double>()
) : Serializable

data class Meditation(
    @SerializedName("meditation_avg")
    val meditationAvg: Double = 0.0,
    @SerializedName("meditation_rec")
    val meditationRec: List<Double> = java.util.ArrayList<Double>(),
    @SerializedName("meditation_tips_rec")
    val meditationTipsRec: List<Int> = java.util.ArrayList<Int>(),
    @SerializedName("flow_percent")
    val flowPercent: Double = 0.0,
    @SerializedName("flow_duration")
    val flowDuration: Int = 0,
    @SerializedName("flow_latency")
    val flowLatency: Int = 0,
    @SerializedName("flow_combo")
    val flowCombo: Int = 0,
    @SerializedName("flow_depth")
    val flowDepth: Double = 0.0,
    @SerializedName("flow_back_num")
    val flowBackNum: Int = 0,
    @SerializedName("flow_loss_num")
    val flowLossNum: Int = 0,
) : Serializable

data class Sceeg(
    val sceegAlphaCurve: List<Double> = java.util.ArrayList<Double>(),
    val scegBetaCurve: List<Double> = java.util.ArrayList<Double>(),
    val sceegDeltaCurve: List<Double> = java.util.ArrayList<Double>(),
    val sceegGammaCurve: List<Double> = java.util.ArrayList<Double>(),
    val sceegThetaCurve: List<Double> = java.util.ArrayList<Double>(),
    val sceegQualityRec: List<Int> = java.util.ArrayList<Int>()
) : Serializable


data class Eeg(
    @SerializedName("eeg_alpha_curve")
    val eegAlphaCurve: List<Double> = java.util.ArrayList<Double>(),
    @SerializedName("eeg_beta_curve")
    val eegBetaCurve: List<Double> = ArrayList(),
    @SerializedName("eeg_delta_curve")
    val eegDeltaCurve: List<Double> = ArrayList(),
    @SerializedName("eeg_gamma_curve")
    val eegGammaCurve: List<Double> = ArrayList(),
    @SerializedName("eeg_theta_curve")
    val eegThetaCurve: List<Double> = ArrayList(),
    @SerializedName("eeg_quality_rec")
    val eegQualityRec: List<Int> = ArrayList()
) : Serializable

data class HrV2(
    @SerializedName("hr_avg")
    val hrAvg: Double? = null,
    @SerializedName("hr_max")
    val hrMax: Int? = null,
    @SerializedName("hr_min")
    val hrMin: Int? = null,
    @SerializedName("hr_rec")
    val hrRec: List<Int> = ArrayList(),
    @SerializedName("hrv_avg")
    val hrvAvg: Double? = null,
    @SerializedName("hrv_rec")
    val hrvRec: List<Double> = ArrayList()
) : Serializable

data class AffectiveTimePoints(
    val arousal: List<TimePoint> = ArrayList(),
    val attention: List<TimePoint> = ArrayList(),
    val coherence: List<TimePoint> = ArrayList(),
    val pleasure: List<TimePoint> = ArrayList(),
    val pressure: List<TimePoint> = ArrayList(),
    val relaxation: List<TimePoint> = ArrayList(),
    val meditation: List<TimePoint> = ArrayList()
) : Serializable

data class BiodataTimePoints(
    val eeg: List<TimePoint> = ArrayList(),
    @SerializedName("hr-v2")
    val hr: List<TimePoint> = ArrayList(),
    val pepr: List<TimePoint> = ArrayList()
) : Serializable

data class AffectiveVersion(
    val arousal: String,
    val attention: String,
    val coherence: String,
    val pleasure: String,
    val pressure: String,
    val relaxation: String
) : Serializable

data class BiodataVersion(
    val eeg: String,
    @SerializedName("hr-v2")
    val hr: String,
    val pepr: String
) : Serializable

/**
 * 持续的时间段，可能中途会断开
 * */
data class TimePoint(
    var start: String,
    var stop: String
) : Serializable