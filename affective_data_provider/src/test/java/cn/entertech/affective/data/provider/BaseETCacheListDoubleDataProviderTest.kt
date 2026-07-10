package cn.entertech.affective.data.provider

import cn.entertech.affective.data.provider.attention.AttentionListCacheDataProvider
import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeAttentionData
import kotlin.test.Test
import kotlin.test.assertEquals

class BaseETCacheListDoubleDataProviderTest {
    @Test
    fun affectiveInputRespectsMaximumCacheSize() {
        val provider = AttentionListCacheDataProvider(maxCacheSize = 2)

        (1..3).forEach { value ->
            provider.providerData(
                RealtimeAffectiveData(
                    realtimeAttentionData = RealtimeAttentionData(value.toDouble()),
                ),
            )
        }

        assertEquals(listOf(2.0, 3.0), provider.data.value)
    }
}
