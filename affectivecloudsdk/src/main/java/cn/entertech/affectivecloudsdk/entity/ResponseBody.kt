package cn.entertech.biomoduledemo.entity

import cn.entertech.affectivecloudsdk.entity.*

/**
 * WebSocket返回体。
 * 注意：
 * 实时的值需要事先订阅，否则获取为空；
 * 要想获取报表中的值，需要事先发送report指令，并指定需要返回的字段，否则为空
 */
data class ResponseBody(
    val code: Int,
    val request: Request,
    val data: Map<Any, Any>,
    val msg: String
) {
    fun isCreateOp(): Boolean {
        return request.op == Request.REQUEST_OPTION_SESSION_CREATE
    }

    fun isRestoreOp(): Boolean {
        return request.op == Request.REQUEST_OPTION_SESSION_RESTORE
    }

    fun isInitBiodataOp(): Boolean {
        return request.op == Request.REQUEST_OPTION_BIODATA_INIT
    }

    fun isStartAffectiveOp(): Boolean {
        return request.op == Request.REQUEST_OPTION_AFFECTIVE_START
    }

    fun isSubmitOp(): Boolean {
        return request.op == Request.REQUEST_OPTION_SUBMIT
    }

    fun isBiodataSubOp(): Boolean {
        if (request.op == Request.REQUEST_OPTION_SUBSCRIBE && request.services == "biodata") {
            data?.keys?.forEach {
                if ((it as String).contains("fields")) {
                    return true
                }
            }
        }
        return false
    }

    fun isAffectiveSubOp(): Boolean {
        if (request.op == Request.REQUEST_OPTION_SUBSCRIBE && request.services == "affective") {
            data?.keys?.forEach {
                if ((it as String).contains("fields")) {
                    return true
                }
            }
        }
        return false
    }

    fun isBiodataUnsubOp(): Boolean {
        if (request.op == Request.REQUEST_OPTION_UNSUBSCRIBE && request.services == "biodata") {
            data?.keys?.forEach {
                if ((it as String).contains("fields")) {
                    return true
                }
            }
        }
        return false
    }

    fun isAffectiveUnsubOp(): Boolean {
        if (request.op == Request.REQUEST_OPTION_UNSUBSCRIBE && request.services == "affective") {
            data?.keys?.forEach {
                if ((it as String).contains("fields")) {
                    return true
                }
            }
        }
        return false
    }

    fun isBiodataResponse(): Boolean {
        var result = true
        if (request.op == Request.REQUEST_OPTION_SUBSCRIBE && request.services == "biodata") {
            data?.keys?.forEach {
                if ((it as String).contains("fields")) {
                    result = false
                }
            }
            return result
        } else {
            return false
        }
    }

    fun isAffectivedataResponse(): Boolean {
        var result = true
        if (request.op == Request.REQUEST_OPTION_SUBSCRIBE && request.services == "affective") {
            data?.keys?.forEach {
                if ((it as String).contains("fields")) {
                    result = false
                }
            }
            return result
        } else {
            return false
        }
    }

    fun isReportBiodata(): Boolean {
        return request.op == Request.REQUEST_OPTION_BIODATA_REPORT && request.services == "biodata"
    }

    fun isReportAffective(): Boolean {
        return request.op == Request.REQUEST_OPTION_AFFECTIVE_REPORT && request.services == "affective"
    }

    fun isAffectiveFinish(): Boolean {
        return request.op == Request.REQUEST_OPTION_AFFECTIVE_FINISH && request.services == "affective"
    }

    fun isSessionClose(): Boolean {
        return request.op == Request.REQUEST_OPTION_SESSION_CLOSE && request.services == "session"

    }

    fun getBiodataSubFields(): SubBiodataFields {
        var subBiodataFields = SubBiodataFields()
        if (data != null) {
            if (data.containsKey("sub_eeg_fields") && data["sub_eeg_fields"] != null) {
                var eegFields = data["sub_eeg_fields"] as ArrayList<String>
                subBiodataFields.subEEGFields = eegFields
            }
            if (data.containsKey("sub_hr_fields") && data["sub_hr_fields"] != null) {
                var hrFields = data["sub_hr_fields"] as ArrayList<String>
                subBiodataFields.subHrFields = hrFields
            }
        }
        return subBiodataFields
    }

    fun getAffectiveSubFields(): SubAffectiveDataFields {
        var subAffectiveDataFields = SubAffectiveDataFields()
        if (data != null) {
            if (data.containsKey("sub_attention_fields") && data["sub_attention_fields"] != null) {
                var attentionFields = data["sub_attention_fields"] as ArrayList<String>
                subAffectiveDataFields.subAttentionFields = attentionFields
            }
            if (data.containsKey("sub_relaxation_fields") && data["sub_relaxation_fields"] != null) {
                var relaxationFields = data["sub_relaxation_fields"] as ArrayList<String>
                subAffectiveDataFields.subRelaxationFields = relaxationFields
            }
            if (data.containsKey("sub_pressure_fields") && data["sub_pressure_fields"] != null) {
                var pressureFields = data["sub_pressure_fields"] as ArrayList<String>
                subAffectiveDataFields.subPressureFields = pressureFields
            }
            if (data.containsKey("sub_arousal_fields") && data["sub_arousal_fields"] != null) {
                var arousalFields = data["sub_arousal_fields"] as ArrayList<String>
                subAffectiveDataFields.subArousalFields = arousalFields
            }
            if (data.containsKey("sub_sleep_fields") && data["sub_sleep_fields"] != null) {
                var sleepFields = data["sub_sleep_fields"] as ArrayList<String>
                subAffectiveDataFields.subSleepFields = sleepFields
            }
        }
        return subAffectiveDataFields
    }


    /**
     * 封装的脑波实时数据
     */
    fun getRealtimeBioData(): RealtimeBioData? {
        var realtimeBioData = RealtimeBioData()
        if (data.containsKey("eeg")) {
            var realtimeEEGData = RealtimeEEGData()
            var eegData = data["eeg"] as Map<Any, Any>
            if (eegData.containsKey("eeg_quality")) {
                realtimeEEGData.quality = eegData["eeg_quality"] as Double
            }
            if (eegData.containsKey("eegr_wave")) {
                realtimeEEGData.rightwave = eegData["eegr_wave"] as ArrayList<Double>
            }
            if (eegData.containsKey("eegl_wave")) {
                realtimeEEGData.leftwave = eegData["eegl_wave"] as ArrayList<Double>
            }
            if (eegData.containsKey("eeg_gamma_power")) {
                realtimeEEGData.gammaPower = eegData["eeg_gamma_power"] as Double
            }
            if (eegData.containsKey("eeg_delta_power")) {
                realtimeEEGData.deltaPower = eegData["eeg_delta_power"] as Double
            }
            if (eegData.containsKey("eeg_theta_power")) {
                realtimeEEGData.thetaPower = eegData["eeg_theta_power"] as Double
            }
            if (eegData.containsKey("eeg_beta_power")) {
                realtimeEEGData.betaPower = eegData["eeg_beta_power"] as Double
            }
            if (eegData.containsKey("eeg_alpha_power")) {
                realtimeEEGData.alphaPower = eegData["eeg_alpha_power"] as Double
            }
            realtimeBioData.realtimeEEGData = realtimeEEGData
        }
        if (data.containsKey("hr")) {
            var realtimeHrData = RealtimeHrData()
            var hrMap = data["hr"] as Map<Any, Any>
            if (hrMap.containsKey("hr")) {
                realtimeHrData.hr = hrMap["hr"] as Double
            }
            if (hrMap.containsKey("hrv")) {
                realtimeHrData.hrv = hrMap["hrv"] as Double
            }
            realtimeBioData.realtimeHrData = realtimeHrData

        }
        return realtimeBioData
    }

    fun getRealtimeAffectiveData(): RealtimeAffectiveData? {
        var realtimeAffectiveData = RealtimeAffectiveData()
        if (data != null) {
            if (data.containsKey("attention")) {
                var realtimeAttentionData = RealtimeAttentionData()
                var map = data["attention"] as Map<Any, Any>
                if (map.containsKey("attention")) {
                    realtimeAttentionData.attention = map["attention"] as Double
                    realtimeAffectiveData.realtimeAttentionData = realtimeAttentionData
                }
            }
            if (data.containsKey("relaxation")) {
                var realtimeRelaxationData = RealtimeRelaxationData()
                var map = data["relaxation"] as Map<Any, Any>
                if (map.containsKey("relaxation")) {
                    realtimeRelaxationData.relaxation = map["relaxation"] as Double
                    realtimeAffectiveData.realtimeRelaxationData = realtimeRelaxationData
                }
            }
            if (data.containsKey("pressure")) {
                var realtimePressureData = RealtimePressureData()
                var map = data["pressure"] as Map<Any, Any>
                if (map.containsKey("pressure")) {
                    realtimePressureData.pressure = map["pressure"] as Double
                    realtimeAffectiveData.realtimePressureData = realtimePressureData
                }
            }
            if (data.containsKey("pleasure")) {
                var realtimePleasureData = RealtimePleasureData()
                var map = data["pleasure"] as Map<Any, Any>
                if (map.containsKey("pleasure")) {
                    realtimePleasureData.pressure = map["pleasure"] as Double
                    realtimeAffectiveData.realtimePleasureData = realtimePleasureData
                }
            }
            if (data.containsKey("arousal")) {
                var realtimeArousalData = RealtimeArousalData()
                var map = data["arousal"] as Map<Any, Any>
                if (map.containsKey("arousal")) {
                    realtimeArousalData.arousal = map["arousal"] as Double
                    realtimeAffectiveData.realtimeArousalData = realtimeArousalData
                }
            }
            if (data.containsKey("sleep")) {
                var realtimeSleepData = RealtimeSleepData()
                var attentionMap = data["sleep"] as Map<Any, Any>
                if (attentionMap.containsKey("sleep_degree")) {
                    realtimeSleepData.sleepDegree = attentionMap["sleep_degree"] as Double
                }
                if (attentionMap.containsKey("sleep_state")) {
                    realtimeSleepData.sleepState = attentionMap["sleep_state"] as Double
                }
                realtimeAffectiveData.realtimeSleepData = realtimeSleepData
            }
        }
        return realtimeAffectiveData
    }

    /**
     * 获取session id
     */
    fun getSessionId(): String? {
        if (request.op == Request.REQUEST_OPTION_SESSION_CREATE && data != null) {
            if (data.containsKey("session_id")) {
                return data["session_id"] as String
            }
        }
        return null
    }

    /**
     * 获取实时左脑波
     */
    fun getLeftBrainwave(): ArrayList<Float>? {
        if (request.services == Request.REQUEST_SERVICES_BIODATA && request.op == Request.REQUEST_OPTION_SUBSCRIBE) {
            if (data.containsKey("eeg")) {
                var eegData = data["eeg"] as Map<Any, Any>
                if (eegData.containsKey("eegl_wave")) {
                    return eegData["eegl_wave"] as ArrayList<Float>
                }
            }
        }
        return null
    }

    /**
     * 获取实时右脑波
     */
    fun getRightBrainwave(): ArrayList<Float>? {
        if (request.services == Request.REQUEST_SERVICES_BIODATA && request.op == Request.REQUEST_OPTION_SUBSCRIBE) {
            if (data.containsKey("eeg")) {
                var eegData = data["eeg"] as Map<Any, Any>
                if (eegData.containsKey("eegr_wave")) {
                    return eegData["eegr_wave"] as ArrayList<Float>
                }
            }
        }
        return null
    }

    /**
     * 获取实时脑电α频段能量占比
     */
    fun getEEGAlphaPower(): Double? {
        if (request.services == Request.REQUEST_SERVICES_BIODATA && request.op == Request.REQUEST_OPTION_SUBSCRIBE) {
            if (data.containsKey("eeg")) {
                var eegData = data["eeg"] as Map<Any, Any>
                if (eegData.containsKey("eeg_alpha_power")) {
                    return eegData["eeg_alpha_power"] as Double
                }
            }
        }
        return null
    }

    /**
     * 获取脑电β频段能量占比
     */
    fun getEEGBetaPower(): Double? {
        if (request.services == Request.REQUEST_SERVICES_BIODATA && request.op == Request.REQUEST_OPTION_SUBSCRIBE) {
            if (data.containsKey("eeg")) {
                var eegData = data["eeg"] as Map<Any, Any>
                if (eegData.containsKey("eeg_beta_power")) {
                    return eegData["eeg_beta_power"] as Double
                }
            }
        }
        return null
    }


    /**
     * 获取脑电θ频段能量占比
     */
    fun getEEGThetaPower(): Double? {
        if (request.services == Request.REQUEST_SERVICES_BIODATA && request.op == Request.REQUEST_OPTION_SUBSCRIBE) {
            if (data.containsKey("eeg")) {
                var eegData = data["eeg"] as Map<Any, Any>
                if (eegData.containsKey("eeg_theta_power")) {
                    return eegData["eeg_theta_power"] as Double
                }
            }
        }
        return null
    }


    /**
     * 获取脑电δ频段能量占比
     */
    fun getEEGDeltaPower(): Double? {
        if (request.services == Request.REQUEST_SERVICES_BIODATA && request.op == Request.REQUEST_OPTION_SUBSCRIBE) {
            if (data.containsKey("eeg")) {
                var eegData = data["eeg"] as Map<Any, Any>
                if (eegData.containsKey("eeg_delta_power")) {
                    return eegData["eeg_delta_power"] as Double
                }
            }
        }
        return null
    }

    /**
     * 获取脑电γ频段能量占比
     */
    fun getEEGGammaPower(): Double? {
        if (request.services == Request.REQUEST_SERVICES_BIODATA && request.op == Request.REQUEST_OPTION_SUBSCRIBE) {
            if (data.containsKey("eeg")) {
                var eegData = data["eeg"] as Map<Any, Any>
                if (eegData.containsKey("eeg_gamma_power")) {
                    return eegData["eeg_gamma_power"] as Double
                }
            }
        }
        return null
    }

    /**
     * 获取脑电信号质量进度，未达到100时表明脑电信号质量不佳，达到100时脑电信号质量良好
     */
    fun getEEGProgress(): Double? {
        if (request.services == Request.REQUEST_SERVICES_BIODATA && request.op == Request.REQUEST_OPTION_SUBSCRIBE) {
            if (data.containsKey("eeg")) {
                var eegData = data["eeg"] as Map<Any, Any>
                if (eegData.containsKey("eeg_progress")) {
                    return eegData["eeg_progress"] as Double
                }
            }
        }
        return null
    }

    /**
     * 获取心率值
     */
    fun getHeartRate(): Double? {
        if (request.services == Request.REQUEST_SERVICES_BIODATA && request.op == Request.REQUEST_OPTION_SUBSCRIBE) {
            if (data.containsKey("hr")) {
                var hrData = data["hr"] as Map<Any, Any>
                if (hrData.containsKey("hr")) {
                    return hrData["hr"] as Double
                }
            }
        }
        return null
    }

    /**
     * 获取心率变异性
     */
    fun getHeartRateVariability(): Double? {
        if (request.services == Request.REQUEST_SERVICES_BIODATA && request.op == Request.REQUEST_OPTION_SUBSCRIBE) {
            if (data.containsKey("hr")) {
                var hrData = data["hr"] as Map<Any, Any>
                if (hrData.containsKey("hrv")) {
                    return hrData["hrv"] as Double
                }
            }
        }
        return null
    }

    /**
     * 获取报表脑电α频段能量变化曲线
     */
    fun getEEGAlphaCurve(): ArrayList<Double>? {
        if (request.services == Request.REQUEST_SERVICES_BIODATA && request.op == Request.REQUEST_OPTION_BIODATA_REPORT) {
            if (data.containsKey("eeg")) {
                var eegData = data["eeg"] as Map<Any, Any>
                if (eegData.containsKey("eeg_alpha_curve")) {
                    return eegData["eeg_alpha_curve"] as ArrayList<Double>
                }
            }
        }
        return null
    }

    /**
     * 获取报表脑电β频段能量变化曲线
     */
    fun getEEGBetaCurve(): ArrayList<Double>? {
        if (request.services == Request.REQUEST_SERVICES_BIODATA && request.op == Request.REQUEST_OPTION_BIODATA_REPORT) {
            if (data.containsKey("eeg")) {
                var eegData = data["eeg"] as Map<Any, Any>
                if (eegData.containsKey("eeg_beta_curve")) {
                    return eegData["eeg_beta_curve"] as ArrayList<Double>
                }
            }
        }
        return null
    }

    /**
     * 获取报表脑电θ频段能量变化曲线
     */
    fun getEEGThetaCurve(): ArrayList<Double>? {
        if (request.services == Request.REQUEST_SERVICES_BIODATA && request.op == Request.REQUEST_OPTION_BIODATA_REPORT) {
            if (data.containsKey("eeg")) {
                var eegData = data["eeg"] as Map<Any, Any>
                if (eegData.containsKey("eeg_theta_curve")) {
                    return eegData["eeg_theta_curve"] as ArrayList<Double>
                }
            }
        }
        return null
    }

    /**
     * 获取报表脑电δ频段能量变化曲线
     */
    fun getEEGDeltaCurve(): ArrayList<Double>? {
        if (request.services == Request.REQUEST_SERVICES_BIODATA && request.op == Request.REQUEST_OPTION_BIODATA_REPORT) {
            if (data.containsKey("eeg")) {
                var eegData = data["eeg"] as Map<Any, Any>
                if (eegData.containsKey("eeg_delta_curve")) {
                    return eegData["eeg_delta_curve"] as ArrayList<Double>
                }
            }
        }
        return null
    }

    /**
     * 获取报表脑电γ频段能量变化曲线
     */
    fun getEEGGammaCurve(): ArrayList<Double>? {
        if (request.services == Request.REQUEST_SERVICES_BIODATA && request.op == Request.REQUEST_OPTION_BIODATA_REPORT) {
            if (data.containsKey("eeg")) {
                var eegData = data["eeg"] as Map<Any, Any>
                if (eegData.containsKey("eeg_gamma_curve")) {
                    return eegData["eeg_gamma_curve"] as ArrayList<Double>
                }
            }
        }
        return null
    }


    /**
     * 获取报表心率平均值
     */
    fun getHeartRateAvg(): Double? {
        if (request.services == Request.REQUEST_SERVICES_BIODATA && request.op == Request.REQUEST_OPTION_BIODATA_REPORT) {
            if (data.containsKey("hr")) {
                var hrData = data["hr"] as Map<Any, Any>
                if (hrData.containsKey("hr_avg")) {
                    return hrData["hr_avg"] as Double
                }
            }
        }
        return null
    }

    /**
     * 获取报表心率最大值
     */
    fun getHeartRateMax(): Double? {
        if (request.services == Request.REQUEST_SERVICES_BIODATA && request.op == Request.REQUEST_OPTION_BIODATA_REPORT) {
            if (data.containsKey("hr")) {
                var hrData = data["hr"] as Map<Any, Any>
                if (hrData.containsKey("hr_max")) {
                    return hrData["hr_max"] as Double
                }
            }
        }
        return null
    }

    /**
     * 获取报表心率最小值
     */
    fun getHeartRateMin(): Double? {
        if (request.services == Request.REQUEST_SERVICES_BIODATA && request.op == Request.REQUEST_OPTION_BIODATA_REPORT) {
            if (data.containsKey("hr")) {
                var hrData = data["hr"] as Map<Any, Any>
                if (hrData.containsKey("hr_min")) {
                    return hrData["hr_min"] as Double
                }
            }
        }
        return null
    }

    /**
     * 获取报表心率值全程记录
     */
    fun getHeartRateRec(): ArrayList<Double>? {
        if (request.services == Request.REQUEST_SERVICES_BIODATA && request.op == Request.REQUEST_OPTION_BIODATA_REPORT) {
            if (data.containsKey("hr")) {
                var hrData = data["hr"] as Map<Any, Any>
                if (hrData.containsKey("hr_rec")) {
                    return hrData["hr_rec"] as ArrayList<Double>?
                }
            }
        }
        return null
    }

    /**
     * 获取报表心率变异性全程记录
     */
    fun getHeartRateVariabilityRec(): ArrayList<Double>? {
        if (request.services == Request.REQUEST_SERVICES_BIODATA && request.op == Request.REQUEST_OPTION_BIODATA_REPORT) {
            if (data.containsKey("hr")) {
                var hrData = data["hr"] as Map<Any, Any>
                if (hrData.containsKey("hrv_rec")) {
                    return hrData["hrv_rec"] as ArrayList<Double>?
                }
            }
        }
        return null
    }

    /**
     * 获取实时注意力值
     */
    fun getAttention(): Double? {
        if (request.services == Request.REQUEST_SERVICES_AFFECTIVE && request.op == Request.REQUEST_OPTION_SUBSCRIBE) {
            if (data.containsKey("attention")) {
                var attentionMap = data["attention"] as Map<Any, Any>
                if (attentionMap.containsKey("attention")) {
                    return attentionMap["attention"] as Double
                }
            }
        }
        return null
    }

    /**
     * 获取实时放松度
     */
    fun getRelaxation(): Double? {
        if (request.services == Request.REQUEST_SERVICES_AFFECTIVE && request.op == Request.REQUEST_OPTION_SUBSCRIBE) {
            if (data.containsKey("relaxation")) {
                var relaxationMap = data["relaxation"] as Map<Any, Any>
                if (relaxationMap.containsKey("relaxation")) {
                    return relaxationMap["relaxation"] as Double
                }
            }
        }
        return null
    }

    /**
     * 获取实时压力值
     */
    fun getPressure(): Double? {
        if (request.services == Request.REQUEST_SERVICES_AFFECTIVE && request.op == Request.REQUEST_OPTION_SUBSCRIBE) {
            if (data.containsKey("pressure")) {
                var pressureMap = data["pressure"] as Map<Any, Any>
                if (pressureMap.containsKey("pressure")) {
                    return pressureMap["pressure"] as Double
                }
            }
        }
        return null
    }

    /**
     * 获取平均注意力值
     */
    fun getAttentionAvg(): Double? {
        if (request.services == Request.REQUEST_SERVICES_AFFECTIVE && request.op == Request.REQUEST_OPTION_AFFECTIVE_REPORT) {
            if (data.containsKey("attention")) {
                var attentionMap = data["attention"] as Map<Any, Any>
                if (attentionMap.containsKey("attention_avg")) {
                    return attentionMap["attention_avg"] as Double
                }
            }
        }
        return null
    }

    /**
     * 获取注意力全程记录
     */
    fun getAttentionRec(): ArrayList<Double>? {
        if (request.services == Request.REQUEST_SERVICES_AFFECTIVE && request.op == Request.REQUEST_OPTION_AFFECTIVE_REPORT) {
            if (data.containsKey("attention")) {
                var attentionMap = data["attention"] as Map<Any, Any>
                if (attentionMap.containsKey("attention_rec")) {
                    return attentionMap["attention_rec"] as ArrayList<Double>
                }
            }
        }
        return null
    }

    /**
     * 获取平均放松度
     */
    fun getRelaxationAvg(): Double? {
        if (request.services == Request.REQUEST_SERVICES_AFFECTIVE && request.op == Request.REQUEST_OPTION_AFFECTIVE_REPORT) {
            if (data.containsKey("relaxation")) {
                var relaxationMap = data["relaxation"] as Map<Any, Any>
                if (relaxationMap.containsKey("relaxation_avg")) {
                    return relaxationMap["relaxation_avg"] as Double
                }
            }
        }
        return null
    }

    /**
     * 获取放松度全程记录
     */
    fun getRelaxationRec(): ArrayList<Double>? {
        if (request.services == Request.REQUEST_SERVICES_AFFECTIVE && request.op == Request.REQUEST_OPTION_AFFECTIVE_REPORT) {
            if (data.containsKey("relaxation")) {
                var relaxationMap = data["relaxation"] as Map<Any, Any>
                if (relaxationMap.containsKey("relaxation_rec")) {
                    return relaxationMap["relaxation_rec"] as ArrayList<Double>
                }
            }
        }
        return null
    }

    /**
     * 获取平均压力值
     */
    fun getPressureAvg(): Double? {
        if (request.services == Request.REQUEST_SERVICES_AFFECTIVE && request.op == Request.REQUEST_OPTION_AFFECTIVE_REPORT) {
            if (data.containsKey("pressure")) {
                var pressureMap = data["pressure"] as Map<Any, Any>
                if (pressureMap.containsKey("pressure_avg")) {
                    return pressureMap["pressure_avg"] as Double
                }
            }
        }
        return null
    }

    /**
     * 获取压力水平全程记录
     */
    fun getPressureRec(): ArrayList<Double>? {
        if (request.services == Request.REQUEST_SERVICES_AFFECTIVE && request.op == Request.REQUEST_OPTION_AFFECTIVE_REPORT) {
            if (data.containsKey("pressure")) {
                var pressureMap = data["pressure"] as Map<Any, Any>
                if (pressureMap.containsKey("pressure_rec")) {
                    return pressureMap["pressure_rec"] as ArrayList<Double>
                }
            }
        }
        return null
    }

}
