package cn.entertech.affectivecloudsdk.entity

import com.google.gson.annotations.SerializedName

/**
 * WebSocket返回体。
 * 注意：
 * 实时的值需要事先订阅，否则获取为空；
 * 要想获取报表中的值，需要事先发送report指令，并指定需要返回的字段，否则为空
 */
class ResponseBody(
    @SerializedName("code") var code: Int,
    @SerializedName("request") var request: Request,
    @SerializedName("data") var data: Map<Any, Any>,
    @SerializedName("msg") var msg: String
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
            data.keys.forEach {
                if ((it as String).contains("fields")) {
                    return true
                }
            }
        }
        return false
    }

    fun isAffectiveSubOp(): Boolean {
        if (request.op == Request.REQUEST_OPTION_SUBSCRIBE && request.services == "affective") {
            data.keys.forEach {
                if ((it as String).contains("fields")) {
                    return true
                }
            }
        }
        return false
    }

    fun isBiodataUnsubOp(): Boolean {
        if (request.op == Request.REQUEST_OPTION_UNSUBSCRIBE && request.services == "biodata") {
            data.keys.forEach {
                if ((it as String).contains("fields")) {
                    return true
                }
            }
        }
        return false
    }

    fun isAffectiveUnsubOp(): Boolean {
        if (request.op == Request.REQUEST_OPTION_UNSUBSCRIBE && request.services == "affective") {
            data.keys.forEach {
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
            data.keys.forEach {
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
            data.keys.forEach {
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
        if (data.containsKey("sub_eeg_fields") && data["sub_eeg_fields"] != null) {
            @Suppress("UNCHECKED_CAST")
            var eegFields = data["sub_eeg_fields"] as ArrayList<String>
            subBiodataFields.subEEGFields = eegFields
        }
        if (data.containsKey("sub_hr_fields") && data["sub_hr_fields"] != null) {
            @Suppress("UNCHECKED_CAST")
            var hrFields = data["sub_hr_fields"] as ArrayList<String>
            subBiodataFields.subHrFields = hrFields
        }
        return subBiodataFields
    }

    fun getAffectiveSubFields(): SubAffectiveDataFields {
        var subAffectiveDataFields = SubAffectiveDataFields()
        if (data.containsKey("sub_attention_fields") && data["sub_attention_fields"] != null) {
            @Suppress("UNCHECKED_CAST")
            var attentionFields = data["sub_attention_fields"] as ArrayList<String>
            subAffectiveDataFields.subAttentionFields = attentionFields
        }
        if (data.containsKey("sub_relaxation_fields") && data["sub_relaxation_fields"] != null) {
            @Suppress("UNCHECKED_CAST")
            var relaxationFields = data["sub_relaxation_fields"] as ArrayList<String>
            subAffectiveDataFields.subRelaxationFields = relaxationFields
        }
        if (data.containsKey("sub_pressure_fields") && data["sub_pressure_fields"] != null) {
            @Suppress("UNCHECKED_CAST")
            var pressureFields = data["sub_pressure_fields"] as ArrayList<String>
            subAffectiveDataFields.subPressureFields = pressureFields
        }
        if (data.containsKey("sub_pleasure_fields") && data["sub_pleasure_fields"] != null) {
            @Suppress("UNCHECKED_CAST")
            var pleasureFields = data["sub_pleasure_fields"] as ArrayList<String>
            subAffectiveDataFields.subPleasureFields = pleasureFields
        }
        if (data.containsKey("sub_arousal_fields") && data["sub_arousal_fields"] != null) {
            @Suppress("UNCHECKED_CAST")
            var arousalFields = data["sub_arousal_fields"] as ArrayList<String>
            subAffectiveDataFields.subArousalFields = arousalFields
        }
        if (data.containsKey("sub_sleep_fields") && data["sub_sleep_fields"] != null) {
            @Suppress("UNCHECKED_CAST")
            var sleepFields = data["sub_sleep_fields"] as ArrayList<String>
            subAffectiveDataFields.subSleepFields = sleepFields
        }
        if (data.containsKey("sub_coherence_fields") && data["sub_coherence_fields"] != null) {
            @Suppress("UNCHECKED_CAST")
            var coherenceFields = data["sub_coherence_fields"] as ArrayList<String>
            subAffectiveDataFields.subCoherenceFields = coherenceFields
        }
        if (data.containsKey("sub_meditation_fields") && data["sub_meditation_fields"] != null) {
            @Suppress("UNCHECKED_CAST")
            var meditationFields = data["sub_meditation_fields"] as ArrayList<String>
            subAffectiveDataFields.subFlowFields = meditationFields
        }
        if (data.containsKey("sub_ssvep-multi-classify_fields") && data["sub_ssvep-multi-classify_fields"] != null) {
            @Suppress("UNCHECKED_CAST")
            var subSsvepMultiClassifyFields = data["sub_ssvep-multi-classify_fields"] as ArrayList<String>
            subAffectiveDataFields.subSsvepMultiClassifyFields = subSsvepMultiClassifyFields
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

            @Suppress("UNCHECKED_CAST")
            var eegData = data["eeg"] as Map<Any, Any>
            if (eegData.containsKey("eeg_quality")) {
                realtimeEEGData.quality = eegData["eeg_quality"] as Double
            }
            if (eegData.containsKey("eegr_wave")) {
                @Suppress("UNCHECKED_CAST")
                realtimeEEGData.rightwave = eegData["eegr_wave"] as ArrayList<Double>
            }
            if (eegData.containsKey("eegl_wave")) {
                @Suppress("UNCHECKED_CAST")
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
        if (data.containsKey("hr-v2")) {
            var realtimeHrData = RealtimeHrData()

            @Suppress("UNCHECKED_CAST")
            var hrMap = data["hr-v2"] as Map<Any, Any>
            if (hrMap.containsKey("hr")) {
                realtimeHrData.hr = hrMap["hr"] as Double
            }
            if (hrMap.containsKey("hrv")) {
                realtimeHrData.hrv = hrMap["hrv"] as Double
            }
            realtimeBioData.realtimeHrData = realtimeHrData

        }
        if (data.containsKey("mceeg")) {
            var realtimeMCEEGData = RealtimeMCEEGData()

            @Suppress("UNCHECKED_CAST")
            var mceegData = data["mceeg"] as Map<Any, Any>
            if (mceegData.containsKey("mceeg_wave")) {
                @Suppress("UNCHECKED_CAST")
                realtimeMCEEGData.mceegWave =
                    MCEEGWaveData(mceegData["mceeg_wave"] as Map<Any, Any>)
            }
            if (mceegData.containsKey("mceog_wave")) {
                @Suppress("UNCHECKED_CAST")
                realtimeMCEEGData.mceogWave =
                    MCEOGWaveData(mceegData["mceog_wave"] as Map<Any, Any>)
            }
            if (mceegData.containsKey("eeg_alpha_power")) {
                realtimeMCEEGData.eegAlphaPower = mceegData["eeg_alpha_power"] as Double
            }
            if (mceegData.containsKey("eeg_beta_power")) {
                realtimeMCEEGData.eegBetaPower = mceegData["eeg_beta_power"] as Double
            }
            if (mceegData.containsKey("eeg_theta_power")) {
                realtimeMCEEGData.eegThetaPower = mceegData["eeg_theta_power"] as Double
            }
            if (mceegData.containsKey("eeg_delta_power")) {
                realtimeMCEEGData.eegDeltaPower = mceegData["eeg_delta_power"] as Double
            }
            if (mceegData.containsKey("eeg_gamma_power")) {
                realtimeMCEEGData.eegGammaPower = mceegData["eeg_gamma_power"] as Double
            }
            if (mceegData.containsKey("mceeg_quality")) {
                realtimeMCEEGData.mceegQuality =
                    MCEEGQualityData(mceegData["mceeg_quality"] as Map<Any, Any>)
            }
            if (mceegData.containsKey("mceog_quality")) {
                realtimeMCEEGData.mceogQuality =
                    MCEOGQualityData(mceegData["mceog_quality"] as Map<Any, Any>)
            }
            if (mceegData.containsKey("mceeg_alpha_power")) {
                realtimeMCEEGData.mceegAlphaPower = mceegData["mceeg_alpha_power"] as Double
            }
            if (mceegData.containsKey("mceeg_beta_power")) {
                realtimeMCEEGData.mceegBetaPower = mceegData["mceeg_beta_power"] as Double
            }
            if (mceegData.containsKey("mceeg_theta_power")) {
                realtimeMCEEGData.mceegThetaPower = mceegData["mceeg_theta_power"] as Double
            }
            if (mceegData.containsKey("mceeg_delta_power")) {
                realtimeMCEEGData.mceegDeltaPower = mceegData["mceeg_delta_power"] as Double
            }
            if (mceegData.containsKey("mceeg_gamma_power")) {
                realtimeMCEEGData.mceegGammaPower = mceegData["mceeg_gamma_power"] as Double
            }
            realtimeBioData.realtimeMCEEGData = realtimeMCEEGData
        }
        if (data.containsKey("bcg")) {
            var realtimeBcgData = RealtimeBCGData()

            @Suppress("UNCHECKED_CAST")
            var bcgMap = data["bcg"] as Map<Any, Any>
            if (bcgMap.containsKey("hr")) {
                realtimeBcgData.hr = bcgMap["hr"] as Double
            }
            if (bcgMap.containsKey("hrv")) {
                realtimeBcgData.hrv = bcgMap["hrv"] as Double
            }
            if (bcgMap.containsKey("bcg_wave")) {
                @Suppress("UNCHECKED_CAST")
                realtimeBcgData.bcgWave = bcgMap["bcg_wave"] as ArrayList<Double>
            }
            if (bcgMap.containsKey("bcg_quality")) {
                realtimeBcgData.bcgQuality = bcgMap["bcg_quality"] as Double
            }
            realtimeBioData.realtimeBCGData = realtimeBcgData

        }
        if (data.containsKey("pepr")) {
            var realtimePEPRData = RealtimePEPRData()

            @Suppress("UNCHECKED_CAST")
            var peprMap = data["pepr"] as Map<Any, Any>
            if (peprMap.containsKey("hr")) {
                realtimePEPRData.hr = peprMap["hr"] as Double
            }
            if (peprMap.containsKey("hrv")) {
                realtimePEPRData.hrv = peprMap["hrv"] as Double
            }
            if (peprMap.containsKey("bcg_wave")) {
                @Suppress("UNCHECKED_CAST")
                realtimePEPRData.bcgWave = peprMap["bcg_wave"] as ArrayList<Double>
            }
            if (peprMap.containsKey("rw_wave")) {
                @Suppress("UNCHECKED_CAST")
                realtimePEPRData.rwWave = peprMap["rw_wave"] as ArrayList<Double>
            }
            if (peprMap.containsKey("bcg_quality")) {
                realtimePEPRData.bcgQuality = (peprMap["bcg_quality"] as Double).toInt()
            }
            if (peprMap.containsKey("rw_quality")) {
                realtimePEPRData.rwQuality = (peprMap["rw_quality"] as Double).toInt()
            }
            if (peprMap.containsKey("rr")) {
                realtimePEPRData.rr = peprMap["rr"] as Double
            }
            realtimeBioData.realtimePEPRData = realtimePEPRData

        }
        if (data.containsKey("dceeg-ssvep")) {
            var realtimeDceegSsvepData = RealtimeDceegSsvepData()

            @Suppress("UNCHECKED_CAST")
            var dceegSsvepMap = data["dceeg-ssvep"] as Map<Any, Any>
            if (dceegSsvepMap.containsKey("eegl_wave")) {
                realtimeDceegSsvepData.eeglWave = dceegSsvepMap["eegl_wave"] as ArrayList<Double>
            }
            if (dceegSsvepMap.containsKey("eegr_wave")) {
                realtimeDceegSsvepData.eegrWave = dceegSsvepMap["eegr_wave"] as ArrayList<Double>
            }
            if (dceegSsvepMap.containsKey("eeg_quality")) {
                @Suppress("UNCHECKED_CAST")
                realtimeDceegSsvepData.eegQuality = (dceegSsvepMap["eeg_quality"] as Double).toInt()
            }
            if (dceegSsvepMap.containsKey("eeg_alpha_power")) {
                @Suppress("UNCHECKED_CAST")
                realtimeDceegSsvepData.eegAlphaPower = dceegSsvepMap["eeg_alpha_power"] as Double
            }
            if (dceegSsvepMap.containsKey("eeg_beta_power")) {
                @Suppress("UNCHECKED_CAST")
                realtimeDceegSsvepData.eegBetaPower = dceegSsvepMap["eeg_beta_power"] as Double
            }
            if (dceegSsvepMap.containsKey("eeg_theta_power")) {
                @Suppress("UNCHECKED_CAST")
                realtimeDceegSsvepData.eegThetaPower = dceegSsvepMap["eeg_theta_power"] as Double
            }
            if (dceegSsvepMap.containsKey("eeg_delta_power")) {
                @Suppress("UNCHECKED_CAST")
                realtimeDceegSsvepData.eegDeltaPower = dceegSsvepMap["eeg_delta_power"] as Double
            }
            if (dceegSsvepMap.containsKey("eeg_gamma_power")) {
                @Suppress("UNCHECKED_CAST")
                realtimeDceegSsvepData.eegGammaPower = dceegSsvepMap["eeg_gamma_power"] as Double
            }
            if (dceegSsvepMap.containsKey("ssvep_freq_corr")) {
                @Suppress("UNCHECKED_CAST")
                realtimeDceegSsvepData.ssvepFreqCorr = dceegSsvepMap["ssvep_freq_corr"] as Map<String,Double>
            }
            if (dceegSsvepMap.containsKey("ssvep_freq_power")) {
                @Suppress("UNCHECKED_CAST")
                realtimeDceegSsvepData.ssvepFreqPower =  dceegSsvepMap["ssvep_freq_power"] as Map<String,Double>
            }
            realtimeBioData.realtimeDceegSsvepData = realtimeDceegSsvepData

        }
        return realtimeBioData
    }

    fun getRealtimeAffectiveData(): RealtimeAffectiveData? {
        var realtimeAffectiveData = RealtimeAffectiveData()
        if (data.containsKey("attention")) {
            var realtimeAttentionData = RealtimeAttentionData()

            @Suppress("UNCHECKED_CAST")
            var map = data["attention"] as Map<Any, Any>
            if (map.containsKey("attention")) {
                realtimeAttentionData.attention = map["attention"] as Double
                realtimeAffectiveData.realtimeAttentionData = realtimeAttentionData
            }
        }
        if (data.containsKey("relaxation")) {
            var realtimeRelaxationData = RealtimeRelaxationData()

            @Suppress("UNCHECKED_CAST")
            var map = data["relaxation"] as Map<Any, Any>
            if (map.containsKey("relaxation")) {
                realtimeRelaxationData.relaxation = map["relaxation"] as Double
                realtimeAffectiveData.realtimeRelaxationData = realtimeRelaxationData
            }
        }
        if (data.containsKey("pressure")) {
            var realtimePressureData = RealtimePressureData()

            @Suppress("UNCHECKED_CAST")
            var map = data["pressure"] as Map<Any, Any>
            if (map.containsKey("pressure")) {
                realtimePressureData.pressure = map["pressure"] as Double
                realtimeAffectiveData.realtimePressureData = realtimePressureData
            }
        }
        if (data.containsKey("pleasure")) {
            var realtimePleasureData = RealtimePleasureData()

            @Suppress("UNCHECKED_CAST")
            var map = data["pleasure"] as Map<Any, Any>
            if (map.containsKey("pleasure")) {
                realtimePleasureData.pleasure = map["pleasure"] as Double
                realtimeAffectiveData.realtimePleasureData = realtimePleasureData
            }
        }
        if (data.containsKey("arousal")) {
            var realtimeArousalData = RealtimeArousalData()

            @Suppress("UNCHECKED_CAST")
            var map = data["arousal"] as Map<Any, Any>
            if (map.containsKey("arousal")) {
                realtimeArousalData.arousal = map["arousal"] as Double
                realtimeAffectiveData.realtimeArousalData = realtimeArousalData
            }
        }
        if (data.containsKey("coherence")) {
            var realtimeCoherenceData = RealtimeCoherenceData()

            @Suppress("UNCHECKED_CAST")
            var map = data["coherence"] as Map<Any, Any>
            if (map.containsKey("coherence")) {
                realtimeCoherenceData.coherence = map["coherence"] as Double
                realtimeAffectiveData.realtimeCoherenceData = realtimeCoherenceData
            }
        }
        if (data.containsKey("meditation")) {
            var realtimeFlowData = RealtimeFlowData()

            @Suppress("UNCHECKED_CAST")
            var map = data["meditation"] as Map<Any, Any>
            if (map.containsKey("meditation")) {
                realtimeFlowData.meditation = map["meditation"] as Double
            }
            if (map.containsKey("meditation_tips")) {
                realtimeFlowData.meditationTips = map["meditation_tips"] as Double
            }

            realtimeAffectiveData.realtimeFlowData = realtimeFlowData
        }
        if (data.containsKey("sleep")) {
            var realtimeSleepData = RealtimeSleepData()

            @Suppress("UNCHECKED_CAST")
            var attentionMap = data["sleep"] as Map<Any, Any>
            if (attentionMap.containsKey("sleep_degree")) {
                realtimeSleepData.sleepDegree = attentionMap["sleep_degree"] as Double
            }
            if (attentionMap.containsKey("sleep_state")) {
                realtimeSleepData.sleepState = attentionMap["sleep_state"] as Double
            }
            realtimeAffectiveData.realtimeSleepData = realtimeSleepData
        }
        if (data.containsKey("ssvep-multi-classify")) {
            var realtimeSsvepMultiClassifyData = RealtimeSsvepMultiClassifyData()

            @Suppress("UNCHECKED_CAST")
            var ssvepMultiClassifyMap = data["ssvep-multi-classify"] as Map<Any,Any>
            if (ssvepMultiClassifyMap.containsKey("ssvep_class")) {
                realtimeSsvepMultiClassifyData.ssvepClass = (ssvepMultiClassifyMap["ssvep_class"] as Double).toInt()
            }
            if (ssvepMultiClassifyMap.containsKey("ssvep_prob")) {
                realtimeSsvepMultiClassifyData.ssvepProb = ssvepMultiClassifyMap["ssvep_prob"] as Map<String,Double>
            }
            realtimeAffectiveData.realtimeSsvepMultiClassifyData = realtimeSsvepMultiClassifyData
        }
        return realtimeAffectiveData
    }

    /**
     * 获取session id
     */
    fun getSessionId(): String? {
        if (request.op == Request.REQUEST_OPTION_SESSION_CREATE) {
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
                @Suppress("UNCHECKED_CAST")
                var eegData = data["eeg"] as Map<Any, Any>
                if (eegData.containsKey("eegl_wave")) {
                    @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
                var eegData = data["eeg"] as Map<Any, Any>
                if (eegData.containsKey("eegr_wave")) {
                    @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
                var eegData = data["eeg"] as Map<Any, Any>
                if (eegData.containsKey("eeg_alpha_curve")) {
                    @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
                var eegData = data["eeg"] as Map<Any, Any>
                if (eegData.containsKey("eeg_beta_curve")) {
                    @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
                var eegData = data["eeg"] as Map<Any, Any>
                if (eegData.containsKey("eeg_theta_curve")) {
                    @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
                var eegData = data["eeg"] as Map<Any, Any>
                if (eegData.containsKey("eeg_delta_curve")) {
                    @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
                var eegData = data["eeg"] as Map<Any, Any>
                if (eegData.containsKey("eeg_gamma_curve")) {
                    @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
                var hrData = data["hr"] as Map<Any, Any>
                if (hrData.containsKey("hr_rec")) {
                    @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
                var hrData = data["hr"] as Map<Any, Any>
                if (hrData.containsKey("hrv_rec")) {
                    @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
                var attentionMap = data["attention"] as Map<Any, Any>
                if (attentionMap.containsKey("attention_rec")) {
                    @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
                var relaxationMap = data["relaxation"] as Map<Any, Any>
                if (relaxationMap.containsKey("relaxation_rec")) {
                    @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
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
                @Suppress("UNCHECKED_CAST")
                var pressureMap = data["pressure"] as Map<Any, Any>
                if (pressureMap.containsKey("pressure_rec")) {
                    @Suppress("UNCHECKED_CAST")
                    return pressureMap["pressure_rec"] as ArrayList<Double>
                }
            }
        }
        return null
    }

}
