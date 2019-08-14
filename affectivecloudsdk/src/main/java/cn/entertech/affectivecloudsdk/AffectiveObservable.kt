package cn.entertech.affectivecloudsdk

import cn.entertech.affectivecloudsdk.entity.*
import cn.entertech.affectivecloudsdk.interfaces.*
import java.lang.IllegalStateException

class AffectiveObservable internal constructor(builder: Builder) :
    Observable<RealtimeAffectiveData, SubAffectiveDataFields> {
    private var mApi: BaseApi? = null
    private var mSubMap: HashMap<Any, Any>? = null

    init {
        mApi = builder.api
        mSubMap = builder.subMap
    }

    override fun subscribe(observer: Observer<RealtimeAffectiveData, SubAffectiveDataFields>) {
        mApi?.subscribeAffectiveData(mSubMap!!, object :
            Callback2<RealtimeAffectiveData> {
            override fun onSuccess(t: RealtimeAffectiveData?) {
                observer.onRealtimeDataResponseSuccess(t)
            }

            override fun onError(error: Error?) {
                observer.onRealtimeDataResponseError(error)
            }

        }, object : Callback2<SubAffectiveDataFields> {
            override fun onSuccess(t: SubAffectiveDataFields?) {
                observer.onSubscribeSuccess(t)
            }

            override fun onError(error: Error?) {
                observer.onSubscribeError(error)
            }

        })
    }

    override fun unsubscribe(callback: Callback2<SubAffectiveDataFields>) {
        mApi?.unsubscribeAffectiveData(mSubMap!!,callback)
    }

    class Builder constructor(internal var api: BaseApi) {
        internal var subMap: HashMap<Any, Any> = HashMap()
        private var attentionSubList: ArrayList<String> = ArrayList()
        private var relaxationSubList: ArrayList<String> = ArrayList()
        private var pressureSubList: ArrayList<String> = ArrayList()
        private var pleasureSubList: ArrayList<String> = ArrayList()
        private var arousalSubList: ArrayList<String> = ArrayList()
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

        fun build(): AffectiveObservable {
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
            if (subMap.isEmpty()) {
                throw IllegalStateException("no data request,call request.. before build")
            }
            return AffectiveObservable(this)
        }
    }
}