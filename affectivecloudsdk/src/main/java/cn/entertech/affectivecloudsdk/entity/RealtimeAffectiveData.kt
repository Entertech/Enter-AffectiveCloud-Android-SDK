package cn.entertech.affectivecloudsdk.entity

class RealtimeAffectiveData(
    var realtimeAttentionData: RealtimeAttentionData? = null,
    var realtimeRelaxationData: RealtimeRelaxationData? = null,
    var realtimePressureData: RealtimePressureData? = null,
    var realtimePleasureData: RealtimePleasureData? = null,
    var realtimeArousalData: RealtimeArousalData? = null,
    var realtimeSleepData: RealtimeSleepData? = null,
    var realtimeCoherenceData: RealtimeCoherenceData? = null
){
    override fun toString(): String {
        return "RealtimeAffectiveData(realtimeAttentionData=$realtimeAttentionData, realtimeRelaxationData=$realtimeRelaxationData, realtimePressureData=$realtimePressureData, realtimePleasureData=$realtimePleasureData, realtimeArousalData=$realtimeArousalData, realtimeSleepData=$realtimeSleepData, realtimeCoherenceData=$realtimeCoherenceData)"
    }
}
