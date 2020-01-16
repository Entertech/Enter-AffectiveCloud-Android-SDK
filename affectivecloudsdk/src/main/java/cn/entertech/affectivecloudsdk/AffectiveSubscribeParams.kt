package cn.entertech.affectivecloudsdk

import cn.entertech.affectivecloudsdk.interfaces.OptionalParams
import java.lang.IllegalStateException

class AffectiveSubscribeParams internal constructor(builder: Builder):OptionalParams {
    private var mSubMap: HashMap<Any, Any>? = null

    init {
        mSubMap = builder.subMap
    }

    override fun body(): HashMap<Any, Any>? {
        return mSubMap
    }

    class Builder {
        internal var subMap: HashMap<Any, Any> = HashMap()
        private var attentionSubList: ArrayList<String> = ArrayList()
        private var relaxationSubList: ArrayList<String> = ArrayList()
        private var pressureSubList: ArrayList<String> = ArrayList()
        private var pleasureSubList: ArrayList<String> = ArrayList()
        private var arousalSubList: ArrayList<String> = ArrayList()
        private var coherenceSubList: ArrayList<String> = ArrayList()
        private var sleepSubList: ArrayList<String> = ArrayList()

        fun requestAttention(): Builder {
            attentionSubList.add("attention")
            return this
        }

        fun requestRelaxation(): Builder {
            relaxationSubList.add("relaxation")
            return this
        }

        fun requestPressure(): Builder {
            pressureSubList.add("pressure")
            return this
        }

        fun requestPleasure(): Builder {
            pleasureSubList.add("pleasure")
            return this
        }

        fun requestArousal(): Builder {
            arousalSubList.add("arousal")
            return this
        }

        fun requestCoherence():Builder{
            coherenceSubList.add("coherence")
            return this
        }

        fun requestSleepDegree(): Builder {
            sleepSubList.add("sleep_degree")
            return this
        }

        fun requestSleepState(): Builder {
            sleepSubList.add("sleep_state")
            return this
        }

        fun requestAllSleepData(): Builder {
            sleepSubList.clear()
            sleepSubList.add("sleep_degree")
            sleepSubList.add("sleep_state")
            return this
        }

        fun build(): AffectiveSubscribeParams {
            if (attentionSubList.isNotEmpty()) {
                subMap["attention"] = attentionSubList
            }
            if (relaxationSubList.isNotEmpty()) {
                subMap["relaxation"] = relaxationSubList
            }

            if (pressureSubList.isNotEmpty()) {
                subMap["pressure"] = pressureSubList
            }

            if (pleasureSubList.isNotEmpty()) {
                subMap["pleasure"] = pleasureSubList
            }

            if (arousalSubList.isNotEmpty()) {
                subMap["arousal"] = arousalSubList
            }
            if (sleepSubList.isNotEmpty()) {
                subMap["sleep"] = sleepSubList
            }
            if (coherenceSubList.isNotEmpty()){
                subMap["coherence"] = coherenceSubList
            }
            if (subMap.isEmpty()) {
                throw IllegalStateException("no data request,call request.. before build")
            }
            return AffectiveSubscribeParams(this)
        }
    }
}