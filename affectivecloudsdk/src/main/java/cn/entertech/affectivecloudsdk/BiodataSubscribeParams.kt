package cn.entertech.affectivecloudsdk

import cn.entertech.affectivecloudsdk.interfaces.SubscribeParams
import java.lang.IllegalStateException

class BiodataSubscribeParams internal constructor(builder: Builder) : SubscribeParams {
    private var mSubMap: HashMap<Any, Any>? = null

    init {
        mSubMap = builder.subMap
    }

    override fun body(): HashMap<Any, Any>? {
        return mSubMap
    }

    class Builder {
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

        fun build(): BiodataSubscribeParams {
            if (eegSubList.isNotEmpty()) {
                subMap["eeg"] = eegSubList
            }
            if (hrSubList.isNotEmpty()) {
                subMap["hr"] = eegSubList
            }
            if (subMap.isEmpty()) {
                throw IllegalStateException("no data requested,pls call request..method before build")
            }
            return BiodataSubscribeParams(this)
        }
    }
}