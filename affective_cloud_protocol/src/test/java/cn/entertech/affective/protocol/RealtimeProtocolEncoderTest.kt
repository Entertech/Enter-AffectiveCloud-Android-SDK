package cn.entertech.affective.protocol

import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeAttentionData
import cn.entertech.affective.sdk.bean.RealtimeBioData
import cn.entertech.affective.sdk.bean.RealtimeHrData
import cn.entertech.affective.sdk.bean.RealtimeRelaxationData
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlin.test.Test
import kotlin.test.assertEquals

class RealtimeProtocolEncoderTest {
    private val encoder = GsonRealtimeProtocolEncoder(Gson())

    @Test
    fun encodesVersionedBioEnvelopeWithTypedHeartRate() {
        val json = encoder.encodeBioData(
            RealtimeBioData(realtimeHrData = RealtimeHrData(hr = 72.0, hrv = 15.0)),
            timestampMillis = 1234L,
        )
        val root = JsonParser.parseString(json).asJsonObject

        assertEquals("1", root["version"].asString)
        assertEquals("bio_data", root["type"].asString)
        assertEquals(1234L, root["timestampMillis"].asLong)
        assertEquals(72.0, root["payload"].asJsonObject["heartRate"].asJsonObject["hr"].asDouble)
    }

    @Test
    fun encodesTypedAffectivePayload() {
        val json = encoder.encodeAffectiveData(
            RealtimeAffectiveData(
                realtimeAttentionData = RealtimeAttentionData(0.8),
                realtimeRelaxationData = RealtimeRelaxationData(0.6),
            ),
            timestampMillis = 5678L,
        )
        val payload = JsonParser.parseString(json).asJsonObject["payload"].asJsonObject

        assertEquals(0.8, payload["attention"].asDouble)
        assertEquals(0.6, payload["relaxation"].asDouble)
    }
}
