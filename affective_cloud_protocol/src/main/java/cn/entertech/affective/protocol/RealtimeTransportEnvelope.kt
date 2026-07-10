package cn.entertech.affective.protocol

data class RealtimeTransportEnvelope<Payload>(
    val version: String,
    val type: String,
    val timestampMillis: Long,
    val payload: Payload,
)
