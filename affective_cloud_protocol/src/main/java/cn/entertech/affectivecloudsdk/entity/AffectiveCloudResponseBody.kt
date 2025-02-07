package cn.entertech.affectivecloudsdk.entity

import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData
import cn.entertech.affectivecloudsdk.entity.Request
import cn.entertech.affectivecloudsdk.entity.Request.Companion.REQUEST_OPTION_SUBSCRIBE
import cn.entertech.affectivecloudsdk.entity.Request.Companion.REQUEST_SERVICES_AFFECTIVE
import cn.entertech.affectivecloudsdk.entity.Request.Companion.REQUEST_SERVICES_BIODATA
import com.google.gson.annotations.SerializedName

data class AffectiveCloudResponseBody(
    @SerializedName("code") val code: Int = 0,
    @SerializedName("request") var request: Request,
    @SerializedName("data") var data: Map<Any, Any>,
    @SerializedName("msg") val msg: String = ""
) {

    companion object {
        fun obtain(data: RealtimeAffectiveData): AffectiveCloudResponseBody {
            val subscribeData = HashMap<Any, Any>()
            val attentionData = HashMap<String, Double>()
            attentionData["attention"] = data.realtimeAttentionData?.attention ?: 0.0
            subscribeData["attention"] = attentionData
            val relaxationData = HashMap<String, Double>()
            relaxationData["relaxation"] = data.realtimeRelaxationData?.relaxation ?: 0.0
            subscribeData["relaxation"] = relaxationData
            val pressureData = HashMap<String, Double>()
            pressureData["pressure"] = data.realtimePressureData?.pressure ?: 0.0
            subscribeData["pressure"] = pressureData
            val pleasureData = HashMap<String, Double>()
            pleasureData["pleasure"] = data.realtimePleasureData?.pleasure ?: 0.0
            subscribeData["pleasure"] = pleasureData
            val arousalData = HashMap<String, Double>()
            arousalData["arousal"] = data.realtimeArousalData?.arousal ?: 0.0
            subscribeData["arousal"] = arousalData

            val coherenceData = HashMap<String, Double>()
            coherenceData["coherence"] = data.realtimeCoherenceData?.coherence ?: 0.0
            subscribeData["coherence"] = coherenceData

            val meditationData = HashMap<String, Double>()
            meditationData["meditation"] = data.realtimeFlowData?.meditation ?: 0.0
            subscribeData["meditation"] = meditationData

            return AffectiveCloudResponseBody(
                request = Request(
                    services = REQUEST_SERVICES_AFFECTIVE, op = REQUEST_OPTION_SUBSCRIBE
                ), data = subscribeData
            )
        }


        fun obtain(bioData: RealtimeBioData): AffectiveCloudResponseBody {
            val realtimeEEgData = bioData.realtimeEEGData
            val subscribeData = HashMap<Any, Any>()
            val hr_v2 = HashMap<String, Double>()
            hr_v2["hr"] = bioData.realtimeHrData?.hr ?: 0.0
            hr_v2["hrv"] = bioData.realtimeHrData?.hrv ?: 0.0

            subscribeData["hr-v2"] = hr_v2
            realtimeEEgData?.apply {
                val subscribeEegData = HashMap<String, Any>()
                subscribeEegData["eeg_gamma_power"] = realtimeEEgData.gammaPower ?: 0.0
                subscribeEegData["eeg_delta_power"] = realtimeEEgData.deltaPower ?: 0.0
                subscribeEegData["eeg_theta_power"] = realtimeEEgData.thetaPower ?: 0.0
                subscribeEegData["eeg_beta_power"] = realtimeEEgData.betaPower ?: 0.0
                subscribeEegData["eeg_alpha_power"] = realtimeEEgData.alphaPower ?: 0.0
                subscribeEegData["eeg_quality"] = realtimeEEgData.quality ?: 0.0
                subscribeEegData["eegl_wave"] = realtimeEEgData.leftwave ?: emptyList<Double>()
                subscribeEegData["eegr_wave"] = realtimeEEgData.rightwave ?: emptyList<Double>()
                subscribeData["eeg"] = subscribeEegData
            }

            return AffectiveCloudResponseBody(
                request = Request(
                    services = REQUEST_SERVICES_BIODATA, op = REQUEST_OPTION_SUBSCRIBE
                ), data = subscribeData
            )
        }
    }
}
