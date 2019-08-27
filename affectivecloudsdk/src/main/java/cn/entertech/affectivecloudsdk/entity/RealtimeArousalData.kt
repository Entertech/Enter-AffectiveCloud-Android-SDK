package cn.entertech.affectivecloudsdk.entity

data class RealtimeArousalData(
    var arousal: Double? = null
){
    override fun toString(): String {
        return "RealtimeArousalData(arousal=$arousal)"
    }
}
