package cn.entertech.affectivecloudsdk.sourcedataapi

class AffectiveCloudSourceDataApiFactory private constructor() {
    companion object {
        fun createApi(
            appKey: String,
            appSecret: String,
            userId: String
        ): AffectiveCloudSourceDataApi {
            return AffectiveCloudSourceDataApi(appKey, appSecret, userId)
        }
    }
}