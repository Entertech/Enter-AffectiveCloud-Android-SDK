package cn.entertech.affective.sdk.bean.report

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Type

class HrV2Test{




    // 创建 Gson 实例并注册自定义反序列化器
    val gson = GsonBuilder()
        .registerTypeAdapter(HrV2::class.java, HrV2Deserializer())
        .create()

    @Test
    fun test(){
        val jsonString="{hr_avg:84.4343940}"
        val hrV2=gson.fromJson(jsonString,HrV2::class.java)
        println("hrAvg: ${hrV2.hrAvg}")
    }
}

