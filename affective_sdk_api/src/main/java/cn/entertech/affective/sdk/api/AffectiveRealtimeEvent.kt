package cn.entertech.affective.sdk.api

import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData

sealed interface AffectiveRealtimeEvent {
    val sourceKey: String

    data class BioData(
        override val sourceKey: String,
        val data: RealtimeBioData?,
    ) : AffectiveRealtimeEvent

    data class AffectiveData(
        override val sourceKey: String,
        val data: RealtimeAffectiveData?,
    ) : AffectiveRealtimeEvent
}
