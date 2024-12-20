package cn.entertech.affective.sdk.manager

interface IAffectiveIGetReportListener<Key> {
    fun getAffectiveReportError(key: Key, error: cn.entertech.affective.sdk.bean.Error?) {

    }

    fun getBioReportError(key: Key, error: cn.entertech.affective.sdk.bean.Error?) {}

    fun onError(key: Key, error: cn.entertech.affective.sdk.bean.Error?) {}

    fun onSuccess(key: Key, entity: cn.entertech.affective.sdk.bean.UploadReportEntity?) {}
}