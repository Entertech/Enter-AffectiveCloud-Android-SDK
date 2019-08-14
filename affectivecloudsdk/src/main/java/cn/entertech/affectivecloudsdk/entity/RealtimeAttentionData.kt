package cn.entertech.affectivecloudsdk.entity

data class RealtimeAttentionData(
    var attention: Double? = null
){
    override fun toString(): String {
        return "RealtimeAttentionData(attention=$attention)"
    }
}
