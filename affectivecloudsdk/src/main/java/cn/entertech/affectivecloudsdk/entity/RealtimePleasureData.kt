package cn.entertech.affectivecloudsdk.entity

data class RealtimePleasureData(
    var pleasure: Double? = null
){
    override fun toString(): String {
        return "RealtimePleasureData(pleasure=$pleasure)"
    }
}
