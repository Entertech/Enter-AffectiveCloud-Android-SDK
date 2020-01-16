package cn.entertech.affectivecloudsdk.entity

data class RealtimeCoherenceData(
    var coherence: Double? = null
){
    override fun toString(): String {
        return "RealtimeCoherenceData(coherence=$coherence)"
    }
}
