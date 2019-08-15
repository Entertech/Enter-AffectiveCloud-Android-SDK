package cn.entertech.affectivecloudsdk

import cn.entertech.affectivecloudsdk.entity.Error
import cn.entertech.affectivecloudsdk.entity.RealtimeBioData
import cn.entertech.affectivecloudsdk.entity.SubBiodataFields
import cn.entertech.affectivecloudsdk.interfaces.*
import java.lang.IllegalStateException

class BiodataObservable internal constructor(api: BaseApi) :
    Observable<RealtimeBioData, SubBiodataFields> {
    private var mApi: BaseApi? = null
    private var mSubMap: HashMap<Any, Any>
    private var eegSubList: ArrayList<String>
    private var hrSubList: ArrayList<String>
    init {
        mSubMap = HashMap()
        eegSubList = ArrayList()
        hrSubList = ArrayList()
        mApi = api
    }

    companion object {
        fun create(api: BaseApi): BiodataObservable {
            return BiodataObservable(api)
        }
    }

    override fun subscribe(observer: Observer<RealtimeBioData, SubBiodataFields>) {
        checkEmpty()
        mApi?.subscribeBioData(mSubMap!!, object :
            Callback2<RealtimeBioData> {
            override fun onSuccess(t: RealtimeBioData?) {
                observer.onRealtimeDataResponseSuccess(t)
            }

            override fun onError(error: Error?) {
                observer.onRealtimeDataResponseError(error)
            }

        }, object : Callback2<SubBiodataFields> {
            override fun onSuccess(t: SubBiodataFields?) {
                observer.onSubscribeSuccess(t)
            }

            override fun onError(error: Error?) {
                observer.onSubscribeError(error)
            }
        })
    }

    override fun unsubscribe(callback: Callback2<SubBiodataFields>) {
        checkEmpty()
        mApi?.unsubscribeBioData(mSubMap!!, callback)
    }

    private fun checkEmpty() {
        if (eegSubList.isNotEmpty()) {
            mSubMap["eeg"] = eegSubList
        }
        if (hrSubList.isNotEmpty()) {
            mSubMap["hr"] = eegSubList
        }
        if (mSubMap!!.isEmpty()) {
            throw IllegalStateException("no data requested,pls call request..method before build")
        }
    }

    fun requestEEGLeftWave(): BiodataObservable {
        eegSubList.add("eegl_wave")
        return this
    }

    fun requestEEGRightWave(): BiodataObservable {
        eegSubList.add("eegr_wave")
        return this
    }

    fun requestEEGAlphaPower(): BiodataObservable {
        eegSubList.add("eeg_alpha_power")
        return this
    }

    fun requestEEGBetaPower(): BiodataObservable {
        eegSubList.add("eeg_beta_power")
        return this
    }

    fun requestEEGThetaPower(): BiodataObservable {
        eegSubList.add("eeg_theta_power")
        return this
    }

    fun requestEEGDeltaPower(): BiodataObservable {
        eegSubList.add("eeg_delta_power")
        return this
    }

    fun requestEEGGammaPower(): BiodataObservable {
        eegSubList.add("eeg_gamma_power")
        return this
    }

    fun requestEEGQuality(): BiodataObservable {
        eegSubList.add("eeg_quality")
        return this
    }

    fun requestAllEEGData(): BiodataObservable {
        eegSubList.clear()
        eegSubList.add("eegl_wave")
        eegSubList.add("eegr_wave")
        eegSubList.add("eeg_alpha_power")
        eegSubList.add("eeg_beta_power")
        eegSubList.add("eeg_theta_power")
        eegSubList.add("eeg_delta_power")
        eegSubList.add("eeg_gamma_power")
        eegSubList.add("eeg_quality")
        return this
    }

    fun requestHeartRate(): BiodataObservable {
        hrSubList.add("hr")
        return this
    }

    fun requestHRV(): BiodataObservable {
        hrSubList.add("hrv")
        return this
    }

    fun requestAllHrData(): BiodataObservable {
        hrSubList.clear()
        hrSubList.add("hr")
        hrSubList.add("hrv")
        return this
    }
}