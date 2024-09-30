package cn.entertech.affective.sdk.bean.report

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Sleep() : Serializable {

    companion object {
        /**
         * 清醒
         * */
        const val SLEEP_STAGE_AWAKE = 0

        /**
         * 非快速眼动期1（思睡期）
         * */
        const val SLEEP_STAGE_NREM1 = 1

        /**
         * NREM2 = 2  # 非快速眼动期2（浅睡期）
         * */
        const val SLEEP_STAGE_NREM2 = 2

        /**
         * NREM3 = 3  # 非快速眼动期3（深睡期）
         * */
        const val SLEEP_STAGE_NREM3 = 3

        /**
         * REM = 4  # 快速眼动期
         * */
        const val SLEEP_STAGE_REM = 4
    }

    /**
     * 清醒时长，单位：秒
     * */
    @SerializedName("awake_duration")
    var awakeDuration: Int = 0

    /**
     * 深睡时长，单位：秒
     * */
    @SerializedName("deep_duration")
    var deepDuration: Int = 0

    @SerializedName("hrv_score")
    var hrvScore: Double = 0.0

    /**
     * 浅睡时长，单位：秒
     * */
    @SerializedName("light_duration")
    var lightDuration: Int = 0

    /**
     * 快速眼动时长
     */
    @SerializedName("rem_duration")
    var remDuration: Int = 0

    /**
     * 睡眠曲线，反映整个体验过程的睡眠情况。睡眠曲线的值越高表明越接近清醒，曲线值越低表明越接近深睡。
     * */
    @SerializedName("sleep_curve")
    var sleepCurve: ArrayList<Double> = ArrayList()

    /**
     * 入睡用时，单位：秒
     * */
    @SerializedName("sleep_latency")
    var sleepLatency: Int = 0

    var score: Double = 0.0

    @SerializedName("sleep_movement")
    var sleepMovement: Double = 0.0

    @SerializedName("sleep_movement_rec")
    var sleepMovementRec: List<Double> = ArrayList()

    /**
     * 入睡点时间索引即入睡时刻在睡眠曲线上的时间轴坐标。数值范围[0 +∞)0表示无效值
     * */
    @SerializedName("sleep_point")
    var sleepPoint: Int = 0

    @SerializedName("sleep_position")
    var sleepPosition: List<Int> = emptyList()


    /**
     * sleepSpindle:睡眠抗干扰[0100]，值越大表示越容易被干扰
     * */
    @SerializedName("sleep_spindle")
    var sleepSpindle: Double = 0.0

    /**
     * 目前没有用途，传空数据
     * */
    @SerializedName("sleep_spindle_rec")
    var sleepSpindleRec: List<Double> = ArrayList()

    /**
     * WAKE = [SLEEP_STAGE_AWAKE]  # 清醒
     * NREM1 = [SLEEP_STAGE_NREM1]  # 非快速眼动期1（思睡期）
     * NREM2 = [SLEEP_STAGE_NREM2]  # 非快速眼动期2（浅睡期）
     * NREM3 = [SLEEP_STAGE_NREM3]  # 非快速眼动期3（深睡期）
     * REM = [SLEEP_STAGE_REM]  # 快速眼动期
     */
    @SerializedName("sleep_stage")
    var sleepStage: List<Int> = emptyList()

    @SerializedName("temperature")
    var temperature: Double = 0.0

    @SerializedName("temperature_rec")
    var temperatureRec: List<Double> = ArrayList()

    /**
     * 运动次数
     */
    var movementCount: Int = 0

    /**
     * 惊醒次数
     */
    var arousalCount: Int = 0

    /**
     * 容差 睡眠抗干扰[0100] see[sleepSpindle]
     */
    var disturbTolerance: Double = 0.0

    var sleepEegAlphaCurve: List<Double> = ArrayList()

    var sleepEegBetaCurve: List<Double> = ArrayList()

    var sleepEegThetaCurve: List<Double> = ArrayList()

    var sleepEegDeltaCurve: List<Double> = ArrayList()

    var sleepEegGammaCurve: List<Double> = ArrayList()

    var sleepEegQualityRec: List<Int> = ArrayList()

    var sleepArousalRec: List<Int> = ArrayList()

    constructor(
        sleepCurve: ArrayList<Double>,
        awakeDuration: Int,
        deepDuration: Int,
        hrvScore: Double,
        lightDuration: Int,
        remDuration: Int,
        sleepLatency: Int,
        score: Double,
        sleepMovement: Double,
        sleepMovementRec: List<Double>,
        sleepPoint: Int,
        sleepPosition: List<Int>,
        sleepSpindle: Double,
        sleepSpindleRec: List<Double>,
        sleepStage: List<Int>,
        temperature: Double,
        temperatureRec: List<Double>,
        movementCount: Int,
        arousalCount: Int,
        disturbTolerance: Double,
        sleepEegAlphaCurve: List<Double>,
        sleepEegBetaCurve: List<Double>,
        sleepEegThetaCurve: List<Double>,
        sleepEegDeltaCurve: List<Double>,
        sleepEegGammaCurve: List<Double>,
        sleepEegQualityRec: List<Int>,
        sleepArousalRec: List<Int>
    ) : this() {
        this.awakeDuration = awakeDuration
        this.deepDuration = deepDuration
        this.hrvScore = hrvScore
        this.lightDuration = lightDuration
        this.remDuration = remDuration
        this.sleepCurve = sleepCurve
        this.sleepLatency = sleepLatency
        this.score = score
        this.sleepMovement = sleepMovement
        this.sleepMovementRec = sleepMovementRec
        this.sleepPoint = sleepPoint
        this.sleepPosition = sleepPosition
        this.sleepSpindle = sleepSpindle
        this.sleepSpindleRec = sleepSpindleRec
        this.sleepStage = sleepStage
        this.temperature = temperature
        this.temperatureRec = temperatureRec
        this.movementCount = movementCount
        this.arousalCount = arousalCount
        this.disturbTolerance = disturbTolerance
        this.sleepEegAlphaCurve = sleepEegAlphaCurve
        this.sleepEegBetaCurve = sleepEegBetaCurve
        this.sleepEegThetaCurve = sleepEegThetaCurve
        this.sleepEegDeltaCurve = sleepEegDeltaCurve
        this.sleepEegGammaCurve = sleepEegGammaCurve
        this.sleepEegQualityRec = sleepEegQualityRec
        this.sleepArousalRec = sleepArousalRec
    }

}