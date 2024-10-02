package cn.entertech.affectivecloudsdk.utils

import cn.entertech.affective.sdk.bean.type.BioDataCategory
import cn.entertech.affective.sdk.bean.type.PhysiologyDataCategory
import cn.entertech.affectivecloudsdk.entity.ResponseBody
import java.lang.IllegalStateException

class ReportGenerator {
    private var reportBioDataCategoryList: List<BioDataCategory>? = null
    private var reportAffectiveDataCategoryList: List<PhysiologyDataCategory>? = null
    private val affectiveItem = HashMap<Any, Any?>()
    private val item = HashMap<Any, Any?>()
    private val affectiveItemSet = HashSet<String>()

    fun setBioDataCategories(bioDataCategories: List<BioDataCategory>) {
        this.reportBioDataCategoryList = bioDataCategories
    }

    fun setAffectiveDataCategories(affectiveDataCategories: List<PhysiologyDataCategory>) {
        this.reportAffectiveDataCategoryList = affectiveDataCategories
        affectiveDataCategories.forEach {
            affectiveItemSet.add(it.value)
        }
    }


    fun appendBioDataResponse(responseBody: ResponseBody): HashMap<Any, Any?>? {
        if (reportBioDataCategoryList?.isEmpty() != false) {
            throw IllegalStateException("please set breportBioDataCategoryList first")
        }
        var data = responseBody.data
        var keys = responseBody.data.keys

        keys.forEach {
            for (service in reportBioDataCategoryList!!) {
                if (it == service.value)
                    item[it] = data[it]
            }
        }
        for (service in reportBioDataCategoryList!!) {
            if (!item.containsKey(service.value)) {
                return null
            }
        }
        return item
    }

    fun appendAffectiveDataResponse(responseBody: ResponseBody): HashMap<Any, Any?>? {
        if (affectiveItemSet.isEmpty()) {
            throw IllegalStateException("please set AffectiveDataResponse first")
        }
        val data = responseBody.data
        val keys = responseBody.data.keys

        keys.forEach {
            if (affectiveItemSet.remove(it)) {
                affectiveItem[it] = data[it]
            }
        }
        return if (affectiveItemSet.isEmpty()) {
            affectiveItem
        } else {
            null
        }
    }

}