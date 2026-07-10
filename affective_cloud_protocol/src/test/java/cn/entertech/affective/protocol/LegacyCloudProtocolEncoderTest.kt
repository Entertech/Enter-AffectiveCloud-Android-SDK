package cn.entertech.affective.protocol

import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeAttentionData
import cn.entertech.affective.sdk.bean.RealtimeRelaxationData
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyCloudProtocolEncoderTest {
    @Test
    fun preservesLegacyAffectiveJsonStructure() {
        val actual = LegacyCloudProtocolEncoder(Gson()).encodeAffectiveData(
            RealtimeAffectiveData(
                realtimeAttentionData = RealtimeAttentionData(0.8),
                realtimeRelaxationData = RealtimeRelaxationData(0.6),
            ),
        )
        val expected = """
            {
              "code": 0,
              "request": {"services": "affective", "op": "subscribe"},
              "data": {
                "attention": {"attention": 0.8},
                "relaxation": {"relaxation": 0.6},
                "pressure": {"pressure": 0.0},
                "pleasure": {"pleasure": 0.0},
                "arousal": {"arousal": 0.0},
                "coherence": {"coherence": 0.0},
                "meditation": {"meditation": 0.0}
              },
              "msg": ""
            }
        """.trimIndent()

        assertEquals(JsonParser.parseString(expected), JsonParser.parseString(actual))
    }
}
