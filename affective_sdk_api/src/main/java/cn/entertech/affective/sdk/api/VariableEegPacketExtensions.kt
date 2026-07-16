package cn.entertech.affective.sdk.api

import cn.entertech.affective.sdk.VariableEegPacketAdapter

fun IAffectiveDataAnalysisService.appendVariableEEGPacket(
    packet: ByteArray,
    adapter: VariableEegPacketAdapter,
    isEar: Boolean = false,
) {
    adapter.append(packet).forEach { legacyPacket ->
        appendEEGData(legacyPacket, isEar)
    }
}
