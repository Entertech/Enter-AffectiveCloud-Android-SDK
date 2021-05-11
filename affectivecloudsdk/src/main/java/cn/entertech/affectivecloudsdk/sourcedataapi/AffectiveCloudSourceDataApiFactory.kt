package cn.entertech.affectivecloudsdk.sourcedataapi

class AffectiveCloudSourceDataApiFactory private constructor() {
    companion object {
        fun createApi(
            url:String,
            appKey: String,
            appSecret: String,
            userId: String
        ): AffectiveCloudSourceDataApi {
            return AffectiveCloudSourceDataApi(url,appKey, appSecret, userId)
        }
    }
}