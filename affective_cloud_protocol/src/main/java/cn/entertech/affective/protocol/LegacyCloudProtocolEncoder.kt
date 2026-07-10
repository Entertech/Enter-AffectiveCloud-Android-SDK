package cn.entertech.affective.protocol

import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData
import cn.entertech.affectivecloudsdk.entity.AffectiveCloudResponseBody
import com.google.gson.Gson

class LegacyCloudProtocolEncoder internal constructor(
    private val gson: Gson,
) {
    constructor() : this(Gson())

    fun encodeBioData(data: RealtimeBioData): String =
        gson.toJson(AffectiveCloudResponseBody.obtain(data))

    fun encodeAffectiveData(data: RealtimeAffectiveData): String =
        gson.toJson(AffectiveCloudResponseBody.obtain(data))
}
