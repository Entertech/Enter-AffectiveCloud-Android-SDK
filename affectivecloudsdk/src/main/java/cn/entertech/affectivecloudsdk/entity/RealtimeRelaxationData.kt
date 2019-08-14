package cn.entertech.affectivecloudsdk.entity

data class RealtimeRelaxationData(
    var relaxation: Double? = null
){

    override fun toString(): String {
        return "RealtimeRelaxationData(relaxation=$relaxation)"
    }
}
