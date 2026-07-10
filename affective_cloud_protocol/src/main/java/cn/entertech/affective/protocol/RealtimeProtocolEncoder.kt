package cn.entertech.affective.protocol

import cn.entertech.affective.sdk.bean.RealtimeAffectiveData
import cn.entertech.affective.sdk.bean.RealtimeBioData

interface RealtimeProtocolEncoder {
    fun encodeBioData(
        data: RealtimeBioData,
        timestampMillis: Long = System.currentTimeMillis(),
    ): String

    fun encodeAffectiveData(
        data: RealtimeAffectiveData,
        timestampMillis: Long = System.currentTimeMillis(),
    ): String
}
