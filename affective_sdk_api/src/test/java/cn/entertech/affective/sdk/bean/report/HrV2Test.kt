package cn.entertech.affective.sdk.bean.report

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class HrV2Test {
    private val gson = Gson()

    @Test
    fun parsesAverageHeartRate() {
        val hrV2 = gson.fromJson("""{"hr_avg":84}""", HrV2::class.java)

        assertEquals(84, hrV2.hrAvg)
    }
}
