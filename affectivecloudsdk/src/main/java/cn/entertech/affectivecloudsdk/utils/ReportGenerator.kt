package cn.entertech.affectivecloudsdk.utils

import cn.entertech.affectivecloudsdk.entity.ResponseBody
import cn.entertech.affective.sdk.bean.BioOrAffectiveDataCategory
import java.lang.IllegalStateException

class ReportGenerator {
    private var reportBioOrAffectiveDataCategoryList: List<BioOrAffectiveDataCategory>? = null
    private var item: HashMap<Any, Any?> = HashMap()

    fun init(bioOrAffectiveDataCategories: List<BioOrAffectiveDataCategory>) {
        this.reportBioOrAffectiveDataCategoryList = bioOrAffectiveDataCategories
    }

    fun appendResponse(responseBody: ResponseBody): HashMap<Any, Any?>? {
        if (reportBioOrAffectiveDataCategoryList == null || reportBioOrAffectiveDataCategoryList!!.isEmpty()) {
            throw IllegalStateException("please init ReportGenerator first")
        }
        var data = responseBody.data
        var keys = responseBody.data.keys
        keys.forEach {
            for (service in reportBioOrAffectiveDataCategoryList!!) {
                if (it == service.value)
                    item[it] = data[it]
            }
        }
        for (service in reportBioOrAffectiveDataCategoryList!!) {
            if (!item.containsKey(service.value)) {
                return null
            }
        }
        return item
    }

    fun getReport(): HashMap<Any, Any?>? {
        return item
    }

}