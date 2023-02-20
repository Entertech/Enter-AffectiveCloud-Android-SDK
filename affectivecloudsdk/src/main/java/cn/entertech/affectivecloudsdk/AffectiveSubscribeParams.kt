package cn.entertech.affectivecloudsdk

import cn.entertech.affectivecloudsdk.interfaces.OptionalParamsList
import java.lang.IllegalStateException

class AffectiveSubscribeParams internal constructor(builder: Builder):OptionalParamsList {
    private var affectiveSubList:  List<Any>? = null

    init {
        affectiveSubList = builder.affectiveSubList
    }

    override fun body(): List<Any>? {
        return affectiveSubList
    }

    class Builder {
        var affectiveSubList = ArrayList<String>()

        fun requestAttention(): Builder {
            affectiveSubList.add("attention")
            return this
        }

        fun requestRelaxation(): Builder {
            affectiveSubList.add("relaxation")
            return this
        }

        fun requestPressure(): Builder {
            affectiveSubList.add("pressure")
            return this
        }

        fun requestPleasure(): Builder {
            affectiveSubList.add("pleasure")
            return this
        }

        fun requestArousal(): Builder {
            affectiveSubList.add("arousal")
            return this
        }

        fun requestCoherence():Builder{
            affectiveSubList.add("coherence")
            return this
        }

        fun requestFlow():Builder{
            affectiveSubList.add("meditation")
            return this
        }


        fun requestSleep(): Builder {
            affectiveSubList.add("sleep")
            return this
        }

        fun requestSsvepMultiClassify(): Builder {
            affectiveSubList.add("ssvep-multi-classify")
            return this
        }

        fun build(): AffectiveSubscribeParams {
            if (affectiveSubList.isEmpty()) {
                throw IllegalStateException("no affective data request,call request.. before build")
            }
            return AffectiveSubscribeParams(this)
        }
    }
}