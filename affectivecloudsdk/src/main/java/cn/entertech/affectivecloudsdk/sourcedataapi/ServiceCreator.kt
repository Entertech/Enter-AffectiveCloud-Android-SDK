package cn.entertech.affectivecloudsdk.sourcedataapi

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal object ServiceCreator {
    private var retrofit:Retrofit? = null
    fun <T> create(url:String,serviceClass: Class<T>): T {
        retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit!!.create(serviceClass)
    }
}
