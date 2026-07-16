package cn.entertech.affective.sdk

import java.io.ByteArrayOutputStream

/**
 * Converts variable-length dual-channel EEG packets into legacy 20-byte EEG packets.
 *
 * Input packet format:
 * - 2-byte sequence
 * - N sample pairs, each pair is left 3 bytes + right 3 bytes
 *
 * Output packet format:
 * - 2-byte sequence
 * - 3 sample pairs, matching the existing local/cloud appendEEGData(ByteArray) contract
 */
class VariableEegPacketAdapter {
    private val samplePairCache = ByteArrayOutputStream()
    private var sequenceHigh = -1
    private var sequenceLow = -1

    @Synchronized
    fun append(packet: ByteArray): List<ByteArray> {
        require(packet.size >= MIN_PACKET_SIZE) {
            "packet size must be at least $MIN_PACKET_SIZE bytes"
        }
        require((packet.size - SEQUENCE_BYTES) % SAMPLE_PAIR_BYTES == 0) {
            "packet data size must be a multiple of $SAMPLE_PAIR_BYTES bytes"
        }

        if (sequenceHigh == -1 || sequenceLow == -1) {
            sequenceHigh = packet[0].toUnsignedInt()
            sequenceLow = packet[1].toUnsignedInt()
        }

        samplePairCache.write(packet, SEQUENCE_BYTES, packet.size - SEQUENCE_BYTES)

        val cached = samplePairCache.toByteArray()
        val outputCount = cached.size / LEGACY_DATA_BYTES
        if (outputCount == 0) return emptyList()

        val packets = ArrayList<ByteArray>(outputCount)
        var offset = 0
        repeat(outputCount) {
            val legacyPacket = ByteArray(LEGACY_PACKET_BYTES)
            legacyPacket[0] = sequenceHigh.toByte()
            legacyPacket[1] = sequenceLow.toByte()
            System.arraycopy(cached, offset, legacyPacket, SEQUENCE_BYTES, LEGACY_DATA_BYTES)
            packets.add(legacyPacket)
            offset += LEGACY_DATA_BYTES
            incrementSequence()
        }

        samplePairCache.reset()
        if (offset < cached.size) {
            samplePairCache.write(cached, offset, cached.size - offset)
        }
        return packets
    }

    @Synchronized
    fun clear() {
        samplePairCache.reset()
        sequenceHigh = -1
        sequenceLow = -1
    }

    private fun incrementSequence() {
        sequenceLow += 1
        if (sequenceLow > MAX_UNSIGNED_BYTE) {
            sequenceLow = 0
            sequenceHigh += 1
            if (sequenceHigh > MAX_UNSIGNED_BYTE) {
                sequenceHigh = 0
            }
        }
    }

    private fun Byte.toUnsignedInt(): Int = toInt() and MAX_UNSIGNED_BYTE

    private companion object {
        private const val SEQUENCE_BYTES = 2
        private const val SAMPLE_PAIR_BYTES = 6
        private const val LEGACY_SAMPLE_PAIRS = 3
        private const val LEGACY_DATA_BYTES = SAMPLE_PAIR_BYTES * LEGACY_SAMPLE_PAIRS
        private const val LEGACY_PACKET_BYTES = SEQUENCE_BYTES + LEGACY_DATA_BYTES
        private const val MIN_PACKET_SIZE = SEQUENCE_BYTES + SAMPLE_PAIR_BYTES
        private const val MAX_UNSIGNED_BYTE = 0xff
    }
}
