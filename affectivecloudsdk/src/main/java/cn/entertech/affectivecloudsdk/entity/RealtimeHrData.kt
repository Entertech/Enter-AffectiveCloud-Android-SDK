package cn.entertech.affectivecloudsdk.entity

data class RealtimeHrData(
    var hr: Double? = null,
    var hrv: Double? = null
) {
    override fun toString(): String {
        return "RealtimeHrData(hr=$hr, hrv=$hrv)"
    }
}
