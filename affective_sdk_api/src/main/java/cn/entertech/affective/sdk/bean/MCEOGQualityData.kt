package cn.entertech.affective.sdk.bean

import cn.entertech.affective.sdk.utils.map
import java.io.Serializable


class MCEOGQualityData(
    mapData: Map<Any, Any>
): Serializable {
    private val _vEOG_L: Any by map(mapData, "vEOG-L")
    val vEOG_L: Double
        get() {
            return _vEOG_L as Double
        }

    private val _vEOG_R: Any by map(mapData, "vEOG-R")
    val vEOG_R: Double
        get() {
            return _vEOG_R as Double
        }
}