package cn.entertech.affectivecloudsdk

import cn.entertech.affectivecloudsdk.entity.*
import cn.entertech.affectivecloudsdk.interfaces.*
import java.lang.IllegalStateException

class AffectiveObservable internal constructor(api: BaseApi) :
    Observable<RealtimeAffectiveData, SubAffectiveDataFields> {
    private var mApi: BaseApi? = null
    private var mSubMap: HashMap<Any, Any>
    private var attentionSubList: ArrayList<String>
    private var relaxationSubList: ArrayList<String>
    private var pressureSubList: ArrayList<String>
    private var pleasureSubList: ArrayList<String>
    private var arousalSubList: ArrayList<String>
    private var sleepSubList: ArrayList<String>

    init {
        mApi = api
        mSubMap = HashMap()
        attentionSubList = ArrayList()
        relaxationSubList = ArrayList()
        pressureSubList = ArrayList()
        pleasureSubList = ArrayList()
        arousalSubList = ArrayList()
        sleepSubList = ArrayList()
    }

    companion object {
        fun create(api: BaseApi): AffectiveObservable {
            return AffectiveObservable(api)
        }
    }

    override fun subscribe(observer: Observer<RealtimeAffectiveData, SubAffectiveDataFields>) {
        checkEmpty()
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
        checkEmpty()
        mApi?.unsubscribeAffectiveData(mSubMap!!, callback)
    }

    fun requestAttention(): AffectiveObservable {
        attentionSubList.add("attention")
        return this
    }

    fun requestRelaxation(): AffectiveObservable {
        relaxationSubList.add("relaxation")
        return this
    }

    fun requestPressure(): AffectiveObservable {
        pressureSubList.add("pressure")
        return this
    }

    fun requestPleasure(): AffectiveObservable {
        pleasureSubList.add("pleasure")
        return this
    }

    fun requestArousal(): AffectiveObservable {
        arousalSubList.add("arousal")
        return this
    }

    fun requestSleepDegree(): AffectiveObservable {
        sleepSubList.add("sleep_degree")
        return this
    }

    fun requestSleepState(): AffectiveObservable {
        sleepSubList.add("sleep_state")
        return this
    }

    fun requestAllSleepData(): AffectiveObservable {
        sleepSubList.clear()
        sleepSubList.add("sleep_degree")
        sleepSubList.add("sleep_state")
        return this
    }

    fun checkEmpty() {
        if (attentionSubList.isNotEmpty()) {
            mSubMap["attention"] = attentionSubList
        }
        if (relaxationSubList.isNotEmpty()) {
            mSubMap["relaxation"] = relaxationSubList
        }

        if (pressureSubList.isNotEmpty()) {
            mSubMap["pressure"] = pressureSubList
        }

        if (pleasureSubList.isNotEmpty()) {
            mSubMap["pleasure"] = pleasureSubList
        }

        if (arousalSubList.isNotEmpty()) {
            mSubMap["arousal"] = arousalSubList
        }
        if (sleepSubList.isNotEmpty()) {
            mSubMap["sleep"] = sleepSubList
        }
        if (mSubMap.isEmpty()) {
            throw IllegalStateException("no data request,call request.. before build")
        }
    }
}