package cn.entertech.affectivecloudsdk

import cn.entertech.affectivecloudsdk.interfaces.BaseApi

class EnterAffectiveCloudApiFactory private constructor() {
    fun createApi(websocketAddress: String, appKey: String, appSecret: String, userName: String,userId:String): BaseApi {
        return EnterAffectiveCloudApiImpl(websocketAddress, appKey, appSecret, userName,userId)
    }
}