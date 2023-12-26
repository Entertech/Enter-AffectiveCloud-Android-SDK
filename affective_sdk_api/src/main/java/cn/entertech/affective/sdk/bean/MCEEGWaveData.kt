package cn.entertech.affective.sdk.bean

import cn.entertech.affective.sdk.utils.map
import java.io.Serializable


class MCEEGWaveData(mapData: Map<Any, Any>): Serializable {
    private val _fp1_fpz: Any by map(mapData, "Fp1-Fpz")
    private val _fp2_fpz: Any by map(mapData, "Fp2-Fpz")
    private val _O1_fpz: Any by map(mapData, "O1-Fpz")
    private val _O2_fpz: Any by map(mapData, "O2-Fpz")
    private val _po3_fpz: Any by map(mapData, "Po3-Fpz")
    private val _po4_fpz: Any by map(mapData, "Po4-Fpz")
    val fp1_fpz: List<Double>
        get() {
            return _fp1_fpz as List<Double>
        }

    val fp2_fpz: List<Double>
        get() {
            return _fp2_fpz as List<Double>
        }
    val O1_fpz: List<Double>
        get() {
            return _O1_fpz as List<Double>
        }
    val O2_fpz: List<Double>
        get() {
            return _O2_fpz as List<Double>
        }
    val po3_fpz: List<Double>
        get() {
            return _po3_fpz as List<Double>
        }
    val po4_fpz: List<Double>
        get() {
            return _po4_fpz as List<Double>
        }
}
