package cn.entertech.affective.sdk.bean

import java.io.Serializable

class RealtimeSsvepMultiClassifyData : Serializable {
    var ssvepClass: Int? = null
    var ssvepProb: Map<String, Double>? = null
    override fun toString(): String {
        return "RealtimeSsvepMultiClassifyData(ssvepClass=$ssvepClass, ssvepProb=$ssvepProb)"
    }

}
