package cn.entertech.affectivecloudsdk.sourcedataapi

import cn.entertech.affectivecloudsdk.entity.SourceDataApiAuthRequest
import cn.entertech.affectivecloudsdk.entity.SourceDataApiAuthResponse
import cn.entertech.affectivecloudsdk.entity.SourceDataRecord
import cn.entertech.affectivecloudsdk.entity.SourceDataRecordPageList
import retrofit2.Response

internal object Network {
    var retrofitService: RetrofitService? = null

    fun createRetrofitService(url: String) {
        retrofitService = ServiceCreator.create(url,RetrofitService::class.java)
    }

    suspend fun auth(sourceDataApiAuthRequest: SourceDataApiAuthRequest): Resource<SourceDataApiAuthResponse> =
        fire { retrofitService?.auth(sourceDataApiAuthRequest) }

    suspend fun getSourceDataPageList(
        token: String,
        page: Int, pageSize: Int
    ): Resource<SourceDataRecordPageList> = fire {
        retrofitService?.getSourceDataPageList(
            "JWT $token",
            page, pageSize
        )
    }

    suspend fun getSourceDataRecordByUserId(
        token: String,
        userId: String
    ): Resource<List<SourceDataRecord>> = fire {
        retrofitService?.getSourceDataRecordByUserId(
            "JWT $token",
            userId
        )
    }

    suspend fun getSourceDataRecordById(token: String, id: Int): Resource<SourceDataRecord> =
        fire { retrofitService?.getSourceDataRecordById("JWT $token", id) }


    suspend fun deleteSourceDataRecordById(token: String, id: Int): Resource<Response<Unit>> =
        fire { retrofitService?.deleteSourceDataRecordById("JWT $token", id) }

    private suspend fun <T : Any> fire(block: suspend () -> T?): Resource<T> {
        return try {
            ResponseHandler.handleSuccess(block())
        } catch (e: Exception) {
            ResponseHandler.handleException(e)
        }
    }

}