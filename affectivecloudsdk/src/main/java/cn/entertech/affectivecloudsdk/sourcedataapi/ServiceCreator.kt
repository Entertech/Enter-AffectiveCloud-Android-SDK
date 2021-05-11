package cn.entertech.affectivecloudsdk.sourcedataapi

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal object ServiceCreator {
    private const val BASE_URL = "https://api-test.affectivecloud.cn/"
    private var retrofit:Retrofit? = null
    init {
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    fun <T> create(serviceClass: Class<T>): T = retrofit!!.create(serviceClass)

    inline fun <reified T> create(): T = create(T::class.java)

}
