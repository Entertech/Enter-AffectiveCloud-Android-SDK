package cn.entertech.affectivecloudsdk.entity

class RealtimeSsvepMultiClassifyData {
    var ssvepClass: Int? = null
    var ssvepProb: Map<String, Double>? = null
    override fun toString(): String {
        return "RealtimeSsvepMultiClassifyData(ssvepClass=$ssvepClass, ssvepProb=$ssvepProb)"
    }

}
