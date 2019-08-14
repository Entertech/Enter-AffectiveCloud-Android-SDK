package cn.entertech.affectivecloudsdk

import cn.entertech.affectivecloudsdk.entity.Error
import cn.entertech.affectivecloudsdk.entity.RealtimeBioData
import cn.entertech.affectivecloudsdk.entity.SubBiodataFields
import cn.entertech.affectivecloudsdk.interfaces.*
import java.lang.IllegalStateException

class BiodataObservable internal constructor(builder: Builder) :
    Observable<RealtimeBioData, SubBiodataFields> {
    private var mApi: BaseApi? = null
    private var mSubMap: HashMap<Any, Any>? = null

    init {
        mApi = builder.api
        mSubMap = builder.subMap
    }

    override fun subscribe(observer: Observer<RealtimeBioData, SubBiodataFields>) {
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
        mApi?.unsubscribeBioData(mSubMap!!, callback)
    }

    class Builder constructor(internal var api: BaseApi) {
        internal var subMap: HashMap<Any, Any> = HashMap()
        private var eegSubList: ArrayList<String> = ArrayList()
        private var hrSubList: ArrayList<String> = ArrayList()

        fun requestEEGLeftWave(): Builder {
            eegSubList.add("eegl_wave")
            return this
        }

        fun requestEEGRightWave(): Builder {
            eegSubList.add("eegr_wave")
            return this
        }

        fun requestEEGAlphaPower(): Builder {
            eegSubList.add("eeg_alpha_power")
            return this
        }

        fun requestEEGBetaPower(): Builder {
            eegSubList.add("eeg_beta_power")
            return this
        }

        fun requestEEGThetaPower(): Builder {
            eegSubList.add("eeg_theta_power")
            return this
        }

        fun requestEEGDeltaPower(): Builder {
            eegSubList.add("eeg_delta_power")
            return this
        }

        fun requestEEGGammaPower(): Builder {
            eegSubList.add("eeg_gamma_power")
            return this
        }

        fun requestEEGQuality(): Builder {
            eegSubList.add("eeg_quality")
            return this
        }

        fun requestAllEEGData(): Builder {
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

        fun requestHeartRate(): Builder {
            hrSubList.add("hr")
            return this
        }

        fun requestHRV(): Builder {
            hrSubList.add("hrv")
            return this
        }

        fun requestAllHrData(): Builder {
            hrSubList.clear()
            hrSubList.add("hr")
            hrSubList.add("hrv")
            return this
        }

        fun build(): BiodataObservable {
            if (eegSubList.isNotEmpty()) {
                subMap["eeg"] = eegSubList
            }
            if (hrSubList.isNotEmpty()) {
                subMap["hr"] = eegSubList
            }
            if (subMap.isEmpty()) {
                throw IllegalStateException("no data requested,pls call request..method before build")
            }
            return BiodataObservable(this)
        }
    }
}