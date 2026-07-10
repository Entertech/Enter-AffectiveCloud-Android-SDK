package cn.entertech.affective

import cn.entertech.affectivesdk.manager.internal.IntBatchBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class IntBatchBufferTest {
    @Test
    fun drainsInOrderOnlyWhenThresholdIsReached() {
        val buffer = IntBatchBuffer(threshold = 3)

        assertNull(buffer.add(1))
        assertNull(buffer.add(2))
        assertEquals(listOf(1, 2, 3), buffer.add(3))
        assertEquals(0, buffer.size)
    }

    @Test
    fun addAllDrainsEveryAccumulatedValue() {
        val buffer = IntBatchBuffer(threshold = 3)
        buffer.add(1)

        assertEquals(listOf(1, 2, 3, 4), buffer.addAll(listOf(2, 3, 4)))
        assertEquals(0, buffer.size)
    }

    @Test
    fun rejectsNonPositiveThresholds() {
        assertFailsWith<IllegalArgumentException> { IntBatchBuffer(0) }
    }
}
