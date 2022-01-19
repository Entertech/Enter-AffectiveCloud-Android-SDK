package cn.entertech.affectivecloudsdk

class AlgorithmParams internal constructor(builder: Builder) {
    var algorithmParams: HashMap<Any, Any?>? = null

    enum class ChannelNum(var value: Int) {
        CHANNEL_NUM_1(1), CHANNEL_NUM_2(2), CHANNEL_NUM_3(3), CHANNEL_NUM_4(4),
        CHANNEL_NUM_5(5), CHANNEL_NUM_6(6), CHANNEL_NUM_7(7), CHANNEL_NUM_8(8)
    }

    enum class Tolerance(var value: Int) {
        LEVEL_0(0), LEVEL_1(1), LEVEL_2(2), LEVEL_3(3), LEVEL_4(4)
    }

    enum class FilterMode(var value: String) {
        BASIC("basic"), SMART("smart"), HARD("hard")
    }

    enum class PowerMode(var value: String) {
        DB("db"), RATE("rate")
    }

    init {
        algorithmParams = builder.algorithmParams
    }

    fun body(): HashMap<Any, Any?>? {
        return algorithmParams
    }

    class Builder {
        internal var algorithmParams: HashMap<Any, Any?> = HashMap()

        fun eeg(eeg: AlgorithmParamsEEG): Builder {
            algorithmParams["eeg"] = eeg.body()
            return this
        }
        fun mceeg(eeg: AlgorithmParamsMCEEG): Builder {
            algorithmParams["mceeg"] = eeg.body()
            return this
        }

        fun build(): AlgorithmParams {
            return AlgorithmParams(this)
        }
    }
}