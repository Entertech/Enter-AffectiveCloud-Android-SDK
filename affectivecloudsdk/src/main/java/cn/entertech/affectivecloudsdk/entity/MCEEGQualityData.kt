package cn.entertech.affectivecloudsdk.entity

import cn.entertech.affectivecloudsdk.utils.map

class MCEEGQualityData(mapData: Map<Any, Any>){

    private val _fp1_fpz: Any by map(mapData, "Fp1-Fpz")
    private val _fp2_fpz: Any by map(mapData, "Fp2-Fpz")
    private val _O1_fpz: Any by map(mapData, "O1-Fpz")
    private val _O2_fpz: Any by map(mapData, "O2-Fpz")
    private val _po3_fpz: Any by map(mapData, "Po3-Fpz")
    private val _po4_fpz: Any by map(mapData, "Po4-Fpz")

    val fp1_fpz: Double
        get() {
            return _fp1_fpz as Double
        }

    val fp2_fpz: Double
        get() {
            return _fp2_fpz as Double
        }
    val O1_fpz: Double
        get() {
            return _O1_fpz as Double
        }
    val O2_fpz: Double
        get() {
            return _O2_fpz as Double
        }
    val po3_fpz: Double
        get() {
            return _po3_fpz as Double
        }
    val po4_fpz: Double
        get() {
            return _po4_fpz as Double
        }
}