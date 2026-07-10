package cn.entertech.affective.protocol

import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData
import com.google.gson.Gson

class GsonRealtimeProtocolEncoder internal constructor(
    private val gson: Gson,
) : RealtimeProtocolEncoder {
    constructor() : this(Gson())

    override fun encodeBioData(data: RealtimeBioData, timestampMillis: Long): String {
        val hr = data.realtimeHrData
        val eeg = data.realtimeEEGData
        val payload = BioDataPayload(
            heartRate = hr?.let { HeartRatePayload(hr = it.hr, hrv = it.hrv) },
            eeg = eeg?.let {
                EegPayload(
                    gammaPower = it.gammaPower,
                    deltaPower = it.deltaPower,
                    thetaPower = it.thetaPower,
                    betaPower = it.betaPower,
                    alphaPower = it.alphaPower,
                    quality = it.quality,
                    leftWave = it.leftwave?.toList(),
                    rightWave = it.rightwave?.toList(),
                )
            },
        )
        return gson.toJson(
            RealtimeTransportEnvelope(
                version = ProtocolVersion.CURRENT,
                type = BIO_DATA_TYPE,
                timestampMillis = timestampMillis,
                payload = payload,
            ),
        )
    }

    override fun encodeAffectiveData(data: RealtimeAffectiveData, timestampMillis: Long): String {
        val payload = AffectiveDataPayload(
            attention = data.realtimeAttentionData?.attention,
            relaxation = data.realtimeRelaxationData?.relaxation,
            pressure = data.realtimePressureData?.pressure,
            pleasure = data.realtimePleasureData?.pleasure,
            arousal = data.realtimeArousalData?.arousal,
            coherence = data.realtimeCoherenceData?.coherence,
            meditation = data.realtimeFlowData?.meditation,
            meditationTips = data.realtimeFlowData?.meditationTips,
        )
        return gson.toJson(
            RealtimeTransportEnvelope(
                version = ProtocolVersion.CURRENT,
                type = AFFECTIVE_DATA_TYPE,
                timestampMillis = timestampMillis,
                payload = payload,
            ),
        )
    }

    companion object {
        const val BIO_DATA_TYPE = "bio_data"
        const val AFFECTIVE_DATA_TYPE = "affective_data"
    }
}
