package cn.entertech.affective.data.provider

import cn.entertech.affective.data.provider.pipeline.DataFilter
import cn.entertech.affective.data.provider.pipeline.DataSelector
import cn.entertech.affective.data.provider.pipeline.DisplayDataProvider
import cn.entertech.affective.data.provider.pipeline.SlidingWindowPolicy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DisplayDataProviderTest {
    @Test
    fun keepsOnlyNewestPoints() {
        val provider = DisplayDataProvider(
            initial = emptyList<Int>(),
            selector = DataSelector<Int, Int> { it },
            filter = DataFilter { true },
            windowPolicy = SlidingWindowPolicy(3),
        )

        (1..5).forEach(provider::accept)

        assertEquals(listOf(3, 4, 5), provider.data.value)
    }

    @Test
    fun clearResetsState() {
        val provider = DisplayDataProvider(
            initial = emptyList<Int>(),
            selector = DataSelector<Int, Int> { it },
            filter = DataFilter { true },
            windowPolicy = SlidingWindowPolicy(3),
        )
        provider.accept(1)

        provider.clear()

        assertTrue(provider.data.value.isEmpty())
    }

    @Test
    fun slidingWindowRequiresPositiveSize() {
        assertFailsWith<IllegalArgumentException> { SlidingWindowPolicy<Int>(0) }
    }
}
