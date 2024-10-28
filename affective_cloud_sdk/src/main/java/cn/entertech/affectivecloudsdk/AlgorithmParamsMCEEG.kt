package cn.entertech.affectivecloudsdk

class AlgorithmParamsMCEEG internal constructor(builder: Builder) {
    var algorithmParamsMCEEG: HashMap<Any, Any>? = null

    init {
        algorithmParamsMCEEG = builder.algorithmParamsMCEEG
    }

    fun body(): HashMap<Any, Any>? {
        return algorithmParamsMCEEG
    }

    class Builder {
        internal var algorithmParamsMCEEG: HashMap<Any, Any> = HashMap()

        fun channelNum(channelNum: AlgorithmParams.ChannelNum): Builder {
            algorithmParamsMCEEG["channel_num"] = channelNum.value
            return this
        }

        fun filterMode(filterMode: AlgorithmParams.FilterMode): Builder {
            algorithmParamsMCEEG["filter_mode"] = filterMode.value
            return this
        }

        fun powerMode(powerMode: AlgorithmParams.PowerMode): Builder {
            algorithmParamsMCEEG["power_mode"] = powerMode.value
            return this
        }

        fun channelPowerVerbose(flag: Boolean): Builder {
            algorithmParamsMCEEG["channel_power_verbose"] = flag
            return this
        }

        fun build(): AlgorithmParamsMCEEG {
            return AlgorithmParamsMCEEG(this)
        }
    }
}