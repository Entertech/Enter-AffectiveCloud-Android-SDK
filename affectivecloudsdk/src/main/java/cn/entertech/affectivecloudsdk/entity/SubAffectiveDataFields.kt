package cn.entertech.affectivecloudsdk.entity

data class SubAffectiveDataFields(
    var subAttentionFields: List<String>? = null,
    var subRelaxationFields: List<String>? = null,
    var subPressureFields: List<String>? = null,
    var subPleasureFields: List<String>? = null,
    var subArousalFields: List<String>? = null,
    var subSleepFields: List<String>? = null,
    var subCoherenceFields: List<String>? = null
){
    override fun toString(): String {
        return "SubAffectiveDataFields(subAttentionFields=$subAttentionFields, subRelaxationFields=$subRelaxationFields, subPressureFields=$subPressureFields, subPleasureFields=$subPleasureFields, subArousalFields=$subArousalFields, subSleepFields=$subSleepFields, subCoherenceFields=$subCoherenceFields)"
    }
}