package cn.entertech.biomoduledemo.affective

import cn.entertech.affective.data.provider.attention.AttentionListCacheDataProvider
import cn.entertech.affective.data.provider.hr.HrListCacheDataProvider
import cn.entertech.affective.data.provider.relaxation.RelaxationListCacheDataProvider
import cn.entertech.affective.protocol.LegacyCloudProtocolEncoder
import cn.entertech.affective.sdk.api.AffectiveRealtimeEvent

class AffectiveDemoPipeline(
    private val protocolEncoder: LegacyCloudProtocolEncoder,
    private val attentionProvider: AttentionListCacheDataProvider,
    private val relaxationProvider: RelaxationListCacheDataProvider,
    private val hrProvider: HrListCacheDataProvider,
    private val outboundMessage: (String) -> Unit,
) {
    fun onEvent(event: AffectiveRealtimeEvent) {
        when (event) {
            is AffectiveRealtimeEvent.BioData -> event.data?.let { data ->
                hrProvider.providerData(data)
                outboundMessage(protocolEncoder.encodeBioData(data))
            }

            is AffectiveRealtimeEvent.AffectiveData -> event.data?.let { data ->
                attentionProvider.providerData(data)
                relaxationProvider.providerData(data)
                outboundMessage(protocolEncoder.encodeAffectiveData(data))
            }
        }
    }

    fun clearDisplayData() {
        attentionProvider.clear()
        relaxationProvider.clear()
        hrProvider.clear()
    }
}
