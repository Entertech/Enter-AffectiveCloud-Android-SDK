package cn.entertech.affective.sdk.bean

import java.io.Serializable

class RealtimeBCGData: Serializable {
    var bcgWave: ArrayList<Double>? = null
    var bcgQuality: Double? = null
    var hr: Double? = null
    var hrv: Double? = null
}