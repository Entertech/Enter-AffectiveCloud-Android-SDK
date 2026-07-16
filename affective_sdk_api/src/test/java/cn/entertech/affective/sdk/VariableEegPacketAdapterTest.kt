package cn.entertech.affective.sdk

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class VariableEegPacketAdapterTest {
    @Test
    fun keepsLegacyTwentyBytePacketUnchanged() {
        val adapter = VariableEegPacketAdapter()
        val packet = ByteArray(20) { it.toByte() }

        val result = adapter.append(packet)

        assertEquals(1, result.size)
        assertArrayEquals(packet, result.single())
    }

    @Test
    fun splitsSixtyTwoBytePacketAndCarriesRemainingSamplePair() {
        val adapter = VariableEegPacketAdapter()
        val packet = ByteArray(62) { it.toByte() }

        val result = adapter.append(packet)

        assertEquals(3, result.size)
        assertArrayEquals(
            byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19),
            result[0],
        )
        assertArrayEquals(
            byteArrayOf(0, 2, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37),
            result[1],
        )
        assertArrayEquals(
            byteArrayOf(0, 3, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55),
            result[2],
        )

        val nextPacket = byteArrayOf(
            10, 11,
            100, 101, 102, 103, 104, 105,
            106, 107, 108, 109, 110, 111,
        )
        val nextResult = adapter.append(nextPacket)

        assertEquals(1, nextResult.size)
        assertArrayEquals(
            byteArrayOf(0, 4, 56, 57, 58, 59, 60, 61, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111),
            nextResult.single(),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsDataSizeThatIsNotSamplePairAligned() {
        VariableEegPacketAdapter().append(ByteArray(9))
    }
}
