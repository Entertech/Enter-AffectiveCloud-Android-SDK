package cn.entertech.affectivecloudsdk.sourcedataapi

import cn.entertech.affectivecloudsdk.entity.SourceDataApiAuthRequest
import cn.entertech.affectivecloudsdk.entity.SourceDataRecord
import cn.entertech.affectivecloudsdk.entity.SourceDataRecordPageList
import cn.entertech.affectivecloudsdk.utils.MD5Encode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
class AffectiveCloudSourceDataApi internal constructor(
    private var url:String,
    private var appKey: String,
    private var appSecret: String,
    private var userId: String
) {
    private var userName: String? = null
    private var password: String? = null
    private var mToken: String? = null

    init {
        Network.createRetrofitService(url)
        var timestamp = "${System.currentTimeMillis()}"
        var userIdEncoded = MD5Encode(userId).toUpperCase()
        userName =
            "{\"app_key\": \"${appKey}\", \"timestamp\": \"${timestamp}\", \"user_id\": \"${userIdEncoded}\"}"
        var md5Params =
            "app_key=$appKey&app_secret=$appSecret&timestamp=$timestamp&user_id=$userIdEncoded"
        password = MD5Encode(md5Params).toUpperCase()
    }

    fun auth(success: (String?) -> Unit, failure: (String?) -> Unit) {
        var sourceDataApiAuthRequest = SourceDataApiAuthRequest()
        sourceDataApiAuthRequest.username = userName
        sourceDataApiAuthRequest.password = password
        GlobalScope.launch (Dispatchers.IO){
            var resource = Network.auth(sourceDataApiAuthRequest)
            if (resource.status == Status.SUCCESS){
                mToken = resource.data?.token
                if (mToken == null){
                    failure.invoke("token not found")
                }else{
                    success.invoke(mToken)
                }
            }else{
                failure.invoke(resource.message)
            }
        }
    }

    fun getSourceDataPageList(
        page: Int,
        pageSize:Int,
        success: (SourceDataRecordPageList?) -> Unit,
        failure: (String?) -> Unit
    ) {
        if (mToken == null) {
            throw IllegalStateException("please invoke auth method first")
        }
        GlobalScope.launch(Dispatchers.IO){
            var resource = Network.getSourceDataPageList(mToken!!,page,pageSize)
            if (resource.status == Status.SUCCESS){
                success.invoke(resource.data)
            }else{
                failure.invoke(resource.message)
            }
        }
    }

    fun getSourceDataByUserId(
        userId: String,
        success: (List<SourceDataRecord>?) -> Unit,
        failure: (String?) -> Unit
    ) {
        var md5UserId = MD5Encode(userId)
        if (mToken == null) {
            throw IllegalStateException("please invoke auth method first")
        }
        GlobalScope.launch(Dispatchers.IO){
            var resource = Network.getSourceDataRecordByUserId(mToken!!,md5UserId)
            if (resource.status == Status.SUCCESS){
                success.invoke(resource.data)
            }else{
                failure.invoke(resource.message)
            }
        }
    }

    fun getSourceDataById(
        id: Int,
        success: (SourceDataRecord?) -> Unit,
        failure: (String?) -> Unit
    ) {
        if (mToken == null) {
            throw IllegalStateException("please invoke auth method first")
        }
        GlobalScope.launch(Dispatchers.IO){
            var resource = Network.getSourceDataRecordById(mToken!!,id)
            if (resource.status == Status.SUCCESS){
                success.invoke(resource.data)
            }else{
                failure.invoke(resource.message)
            }
        }
    }

    fun deleteSourceDataRecordById(
        id: Int,
        success: () -> Unit,
        failure: (String?) -> Unit
    ) {
        if (mToken == null) {
            throw IllegalStateException("please invoke auth method first")
        }
        GlobalScope.launch(Dispatchers.IO){
            var resource = Network.deleteSourceDataRecordById(mToken!!,id)
            if (resource.status == Status.SUCCESS){
                success.invoke()
            }else{
                failure.invoke(resource.message)
            }
        }
    }

}