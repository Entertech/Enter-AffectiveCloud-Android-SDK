package cn.entertech.affectivecloudsdk

import cn.entertech.affectivecloudsdk.interfaces.BaseApi

class EnterAffectiveCloudApiFactory private constructor() {
    companion object{
        fun createApi(websocketAddress: String, appKey: String, appSecret: String,userId:String): BaseApi {
            return EnterAffectiveCloudApiImpl(websocketAddress, appKey, appSecret,userId)
        }
    }
}