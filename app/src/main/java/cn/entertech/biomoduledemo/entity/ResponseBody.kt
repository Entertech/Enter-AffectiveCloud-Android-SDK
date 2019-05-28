package cn.entertech.biomoduledemo.entity

/**
 * WebSocket返回体。
 * 注意：
 * 实时的值需要事先订阅，否则获取为空；
 * 要想获取报表中的值，需要事先发送report指令，并指定需要返回的字段，否则为空
 */
data class ResponseBody(val code: Int, val request: Request, val data: Map<Any, Any>, val msg: String) {
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
                var hrData = data["eeg"] as Map<Any, Any>
                if (hrData.containsKey("eeg_alpha_curve")) {
                    return hrData["eeg_alpha_curve"] as ArrayList<Double>
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
                var hrData = data["eeg"] as Map<Any, Any>
                if (hrData.containsKey("eeg_beta_curve")) {
                    return hrData["eeg_beta_curve"] as ArrayList<Double>
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
                var hrData = data["eeg"] as Map<Any, Any>
                if (hrData.containsKey("eeg_theta_curve")) {
                    return hrData["eeg_theta_curve"] as ArrayList<Double>
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
                var hrData = data["eeg"] as Map<Any, Any>
                if (hrData.containsKey("eeg_delta_curve")) {
                    return hrData["eeg_delta_curve"] as ArrayList<Double>
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
                var hrData = data["eeg"] as Map<Any, Any>
                if (hrData.containsKey("eeg_gamma_curve")) {
                    return hrData["eeg_gamma_curve"] as ArrayList<Double>
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
                var attentionMap = data["relaxation"] as Map<Any, Any>
                if (attentionMap.containsKey("relaxation")) {
                    return attentionMap["relaxation"] as Double
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
                var attentionMap = data["pressure"] as Map<Any, Any>
                if (attentionMap.containsKey("pressure")) {
                    return attentionMap["pressure"] as Double
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
                var attentionMap = data["relaxation"] as Map<Any, Any>
                if (attentionMap.containsKey("relaxation_avg")) {
                    return attentionMap["relaxation_avg"] as Double
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
                var attentionMap = data["relaxation"] as Map<Any, Any>
                if (attentionMap.containsKey("relaxation_rec")) {
                    return attentionMap["relaxation_rec"] as ArrayList<Double>
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
                var attentionMap = data["pressure"] as Map<Any, Any>
                if (attentionMap.containsKey("pressure_avg")) {
                    return attentionMap["pressure_avg"] as Double
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
                var attentionMap = data["pressure"] as Map<Any, Any>
                if (attentionMap.containsKey("pressure_rec")) {
                    return attentionMap["pressure_rec"] as ArrayList<Double>
                }
            }
        }
        return null
    }

}
