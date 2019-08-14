package cn.entertech.affectivecloudsdk.entity

data class RealtimePressureData(
    var pressure: Double? = null
){
    override fun toString(): String {
        return "RealtimePressureData(pressure=$pressure)"
    }
}
