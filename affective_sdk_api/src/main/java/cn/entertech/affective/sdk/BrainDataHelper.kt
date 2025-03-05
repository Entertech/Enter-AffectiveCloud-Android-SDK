package cn.entertech.affective.sdk

import cn.entertech.affective.sdk.utils.AffectiveLogHelper
import cn.entertech.affective.sdk.utils.CharUtil

object BrainDataHelper {
    private const val TAG = "BrainDataHelper"

    /**
     * sceeg 数据中的第一个数据包序号
     * */
    private var highByte: Int = -1

    /**
     * sceeg 数据中的第二个数据包序号
     * */
    private var lowByte: Int = -1

    private val sceegDataCache = ArrayDeque<Byte>()

    /**
     * sceeg 转换成 eeg
     * scceg 数据 1个2字节数据包序号 + 5个3字节的数据
     * eeg 数据 1个2字节数据包序号 + 6个3字节的数据
     *
     * 方案：sceeg 只有一个通道数据，eeg有左右两个通道数据，sceeg数据复制一份也就是左右两个相同通道数据
     * @param hasOrder true 表示 sceegData中有序号
     * */
    fun sceegToEeg(sceegData: ByteArray, hasOrder: Boolean = true): List<ByteArray> {
        val eegData = ByteArray(20)
        val result = ArrayList<ByteArray>()
        if (hasOrder) {
            val sceegDataSize = sceegData.size
            AffectiveLogHelper.d(
                TAG, "sceegData ${sceegData.contentToString()} size $sceegDataSize "
            )
            if (sceegDataSize < 17) {
                return emptyList()
            }
            if (highByte == -1 || lowByte == -1) {
                highByte = CharUtil.converUnchart(sceegData[0])
                lowByte = CharUtil.converUnchart(sceegData[1])
            } else {
                incrementSceegOrder()
            }
        } else {
            incrementSceegOrder()
        }
        eegData[0] = highByte.toByte()
        eegData[1] = lowByte.toByte()
        var currentSceegDataIndex = 2
        var currentEegDataIndex = 2
        for (i in 0..2) {
            val first: Byte
            val second: Byte
            val third: Byte
            if (sceegDataCache.size >= 3) {
                first = sceegDataCache.removeFirst()
                second = sceegDataCache.removeFirst()
                third = sceegDataCache.removeFirst()
            } else {
                first = sceegData[currentSceegDataIndex++]
                second = sceegData[currentSceegDataIndex++]
                third = sceegData[currentSceegDataIndex++]
            }
            eegData[currentEegDataIndex++] = first
            eegData[currentEegDataIndex++] = second
            eegData[currentEegDataIndex++] = third
            eegData[currentEegDataIndex++] = first
            eegData[currentEegDataIndex++] = second
            eegData[currentEegDataIndex++] = third
        }
        result.add(eegData)
        for (i in currentSceegDataIndex until sceegData.size) {
            sceegDataCache.addLast(sceegData[i])
        }
        val cacheSize = sceegDataCache.size
        AffectiveLogHelper.d(TAG, "sceegDataCache: $cacheSize")
        if (cacheSize >= 9) {
            result.addAll(sceegToEeg(ByteArray(0), false))
        }
        if (hasOrder) {
            result.forEach {
                AffectiveLogHelper.d(TAG, "result ${it.contentToString()}")
            }
        }
        return result
    }

    /**
     * 增加sceeg 序号
     * */
    private fun incrementSceegOrder(incrementStep: Int = 1) {
        lowByte += incrementStep
        if (lowByte > 255) {
            lowByte -= 256
            highByte++
            if (highByte > 255) {
                highByte = 0
            }
        }
    }

}