package cn.entertech.affectivecloudsdk.entity

data class RealtimePleasureData(
    var pressure: Double? = null
){
    override fun toString(): String {
        return "RealtimePleasureData(pressure=$pressure)"
    }
}
