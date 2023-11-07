package cn.entertech.affective.sdk.bean

import com.google.gson.annotations.SerializedName

data class UploadReportEntity(
    val code: Int,
    @SerializedName("data")
    val `data`: Data? = null,
    val msg: String,

    @SerializedName("report_version")
    val reportVersion: String = "3",
    @SerializedName("session_id")
    var sessionId: String,
    var start: String,
    @SerializedName("time_points")
    var timePoints: TimePoints? = null,
    val user_id: Int,
    /**
     * 算法版本
     * */
    val version: Version,
    var deviceString: String? = null,
    /**
     * 设备的mac地址
     * */
    var MAC: String? = null
)

data class Data(
    val affective: Affective,
    val biodata: Biodata
)

data class TimePoints(
    val affective: AffectiveTimePoints,
    val biodata: BiodataTimePoints
)

data class Version(
    val affective: AffectiveVersion,
    val biodata: BiodataVersion
)

data class Affective(
    val arousal: Arousal,
    val attention: Attention,
    val coherence: Coherence,
    val pleasure: Pleasure,
    val pressure: Pressure,
    val relaxation: Relaxation,
    val meditation: Meditation
)

data class Biodata(
    val eeg: Eeg,
    @SerializedName("hr-v2")
    val hr: HrV2,
    val pepr: PEPR?
)

data class PEPR(
    val hrAvg: Int,
    val hrMax: Int,
    val hrMin: Int,
    val hrRec: List<Int>,
    @SerializedName("hrv_avg")
    val hrvAvg: Double,
    @SerializedName("hrv_rec")
    val hrvRec: List<Double>,
    @SerializedName("rr_avg")
    val rrAvg: Double,
    @SerializedName("rr_rec")
    val rrRec: List<Double>,
    @SerializedName("bcg_quality_rec")
    val bcgQualityRec: List<Int>,
    @SerializedName("rw_quality_rec")
    val rwQualityRec: List<Int>
)


data class Arousal(
    /**
     * 全程激活度有效值（除去无效值0）的均值
     * */
    val arousal_avg: Int,
    /**
     * 全程激活度记录
     * */
    val arousal_rec: Any
)

data class Attention(
    /**
     * 全程注意力有效值（除去无效值0）的均值
     * */
    @SerializedName("attention_avg")
    val attentionAvg: Double,
    /**
     * 全程注意力记录
     * */
    @SerializedName("attention_rec")
    val attentionRec: List<Double>
)

data class Coherence(
    /**
     * 全程和谐度有效值（除去无效值0）的均值
     * */
    @SerializedName("coherence_avg")
    val coherenceAvg: Double,
    @SerializedName("coherence_duration")
    val coherenceDuration: Int?,
    @SerializedName("coherence_flag")
    val coherenceFlag: List<Int>?,
    /**
     * 全程和谐度记录
     * */
    @SerializedName("coherence_rec")
    val coherenceRec: List<Double>
)

data class Pleasure(
    /**
     * 全程愉悦度有效值（除去无效值0）的均值
     * */
    @SerializedName("pleasure_avg")
    val pleasureAvg: Double,
    /**
     * 全程压力水平记录
     * */
    @SerializedName("pleasure_rec")
    val pleasureRec: List<Double>
)

data class Pressure(
    @SerializedName("pressure_avg")
    val pressureAvg: Double,
    @SerializedName("pressure_rec")
    val pressureRec: List<Double>
)

data class Relaxation(
    /**
     * 全程放松度有效值（除去无效值0）的均值
     * */
    @SerializedName("relaxation_avg")
    val relaxationAvg: Double,
    /**
     * 全程放松度记录
     * */
    @SerializedName("relaxation_rec")
    val relaxationRec: List<Double>
)

data class Meditation(
    @SerializedName("meditation_avg")
    val meditationAvg: Double,
    @SerializedName("meditation_rec")
    val meditationRec: List<Double>,
    @SerializedName("meditation_tips_rec")
    val meditationTipsRec: List<Int>,
    @SerializedName("flow_percent")
    val flowPercent: Double,
    @SerializedName("flow_duration")
    val flowDuration: Int,
    @SerializedName("flow_latency")
    val flowLatency: Int,
    @SerializedName("flow_combo")
    val flowCombo: Int,
    @SerializedName("flow_depth")
    val flowDepth: Double,
    @SerializedName("flow_back_num")
    val flowBackNum: Int,
    @SerializedName("flow_loss_num")
    val flowLossNum: Int,
)

data class Eeg(
    @SerializedName("eeg_alpha_curve")
    val eegAlphaCurve: List<Double>,
    @SerializedName("eeg_beta_curve")
    val eegBetaCurve: List<Double>,
    @SerializedName("eeg_delta_curve")
    val eegDeltaCurve: List<Double>,
    @SerializedName("eeg_gamma_curve")
    val eegGammaCurve: List<Double>,
    @SerializedName("eeg_theta_curve")
    val eegThetaCurve: List<Double>,
    @SerializedName("eeg_quality_rec")
    val eegQualityRec: List<Int>
)

data class HrV2(
    @SerializedName("hr_avg")
    val hrAvg: Double?,
    @SerializedName("hr_max")
    val hrMax: Int?,
    @SerializedName("hr_min")
    val hrMin: Int?,
    @SerializedName("hr_rec")
    val hrRec: List<Int>,
    @SerializedName("hrv_avg")
    val hrvAvg: Double?,
    @SerializedName("hrv_rec")
    val hrvRec: List<Double>
)

data class AffectiveTimePoints(
    val arousal: List<TimePoint>,
    val attention: List<TimePoint>,
    val coherence: List<TimePoint>,
    val pleasure: List<TimePoint>,
    val pressure: List<TimePoint>,
    val relaxation: List<TimePoint>,
    val meditation: List<TimePoint>
)

data class BiodataTimePoints(
    val eeg: List<TimePoint>,
    @SerializedName("hr-v2")
    val hr: List<TimePoint>,
    val pepr: List<TimePoint>
)

data class AffectiveVersion(
    val arousal: String,
    val attention: String,
    val coherence: String,
    val pleasure: String,
    val pressure: String,
    val relaxation: String
)

data class BiodataVersion(
    val eeg: String,
    @SerializedName("hr-v2")
    val hr: String,
    val pepr: String
)

/**
 * 持续的时间段，可能中途会断开
 * */
data class TimePoint(
    var start: String,
    var stop: String
)