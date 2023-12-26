package cn.entertech.affective.sdk.bean

import java.io.Serializable

class RealtimeAffectiveData(
    /**
     * 实时注意力数据
     * */
    var realtimeAttentionData: RealtimeAttentionData? = null,
    /**
     * 实时放松度数据
     * */
    var realtimeRelaxationData: RealtimeRelaxationData? = null,
    /**
     * 实时压力水平数据
     * */
    var realtimePressureData: RealtimePressureData? = null,
    /**
     * 实时愉悦度数据
     * */
    var realtimePleasureData: RealtimePleasureData? = null,
    /**
     * 实时激活度数据
     * */
    var realtimeArousalData: RealtimeArousalData? = null,
    /**
     * 实时睡眠数据
     * */
    var realtimeSleepData: RealtimeSleepData? = null,
    /**
     * 实时和谐度数据
     * */
    var realtimeCoherenceData: RealtimeCoherenceData? = null,
    var realtimeSsvepMultiClassifyData: RealtimeSsvepMultiClassifyData? = null,
    /**
     * 实时智慧数据
     * */
    var realtimeFlowData: RealtimeFlowData? = null
) : Serializable {

    override fun toString(): String {
        return "RealtimeAffectiveData(realtimeAttentionData=$realtimeAttentionData, realtimeRelaxationData=$realtimeRelaxationData, realtimePressureData=$realtimePressureData, realtimePleasureData=$realtimePleasureData, realtimeArousalData=$realtimeArousalData, realtimeSleepData=$realtimeSleepData, realtimeCoherenceData=$realtimeCoherenceData, realtimeSsvepMultiClassifyData=$realtimeSsvepMultiClassifyData, realtimeFlowData=$realtimeFlowData)"
    }
}
