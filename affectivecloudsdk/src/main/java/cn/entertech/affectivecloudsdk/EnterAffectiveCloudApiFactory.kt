package cn.entertech.affectivecloudsdk

import cn.entertech.affectivecloudsdk.interfaces.BaseApi

class EnterAffectiveCloudApiFactory private constructor() {
    companion object{
        fun createApi(websocketAddress: String, appKey: String, appSecret: String,userId:String,uploadCycle:Int = 0): BaseApi {
            return EnterAffectiveCloudApiImpl(websocketAddress, appKey, appSecret,userId,uploadCycle)
        }
    }
}