package cn.entertech.biomoduledemo.affective

import cn.entertech.affective.data.provider.attention.AttentionListCacheDataProvider
import cn.entertech.affective.data.provider.hr.HrListCacheDataProvider
import cn.entertech.affective.data.provider.relaxation.RelaxationListCacheDataProvider
import cn.entertech.affective.protocol.LegacyCloudProtocolEncoder
import cn.entertech.affective.sdk.api.AffectiveRealtimeEvent
import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeAttentionData
import cn.entertech.affective.sdk.bean.RealtimeRelaxationData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AffectiveDemoPipelineTest {
    @Test
    fun routesAffectiveEventToTransportAndDisplayProviders() {
        val attention = AttentionListCacheDataProvider()
        val relaxation = RelaxationListCacheDataProvider()
        val messages = mutableListOf<String>()
        val pipeline = AffectiveDemoPipeline(
            protocolEncoder = LegacyCloudProtocolEncoder(),
            attentionProvider = attention,
            relaxationProvider = relaxation,
            hrProvider = HrListCacheDataProvider(),
            outboundMessage = messages::add,
        )

        pipeline.onEvent(
            AffectiveRealtimeEvent.AffectiveData(
                sourceKey = "local",
                data = RealtimeAffectiveData(
                    realtimeAttentionData = RealtimeAttentionData(0.8),
                    realtimeRelaxationData = RealtimeRelaxationData(0.6),
                ),
            ),
        )

        assertEquals(listOf(0.8), attention.data.value)
        assertEquals(listOf(0.6), relaxation.data.value)
        assertEquals(1, messages.size)
        assertTrue(messages.single().contains("\"services\":\"affective\""))
    }
}
