package cn.entertech.affectivecloudsdk.entity

data class SubAffectiveDataFields(
    var subAttentionFields: List<String>? = null,
    var subRelaxationFields: List<String>? = null,
    var subPressureFields: List<String>? = null,
    var subPleasureFields: List<String>? = null,
    var subArousalFields: List<String>? = null,
    var subSleepFields: List<String>? = null
)