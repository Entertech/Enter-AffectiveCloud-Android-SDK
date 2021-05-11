package cn.entertech.affectivecloudsdk.sourcedataapi

import cn.entertech.affectivecloudsdk.entity.SourceDataApiAuthRequest
import cn.entertech.affectivecloudsdk.entity.SourceDataApiAuthResponse
import cn.entertech.affectivecloudsdk.entity.SourceDataRecord
import cn.entertech.affectivecloudsdk.entity.SourceDataRecordListWithPages
import retrofit2.Response
import retrofit2.http.*

interface RetrofitService {
    @POST("/api-token-auth/")
    suspend fun auth(@Body body: SourceDataApiAuthRequest): SourceDataApiAuthResponse

    @GET("/v1/sourceDataRecords/")
    suspend fun getSourceDataListWithPages(
        @Header("Authorization") authorization: String,
        @Query("page") page:Int,
        @Query("page_size") page_size:Int
    ): SourceDataRecordListWithPages

    @GET("/v1/sourceDataRecords/")
    suspend fun getSourceDataRecordByUserId(
        @Header("Authorization") authorization: String,
        @Query("client_id") userId: String
    ): List<SourceDataRecord>

    @GET("/v1/sourceDataRecords/{id}/")
    suspend fun getSourceDataRecordById(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): SourceDataRecord

    @DELETE("/v1/sourceDataRecords/{id}/")
    suspend fun deleteSourceDataRecordById(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Response<Unit>
}