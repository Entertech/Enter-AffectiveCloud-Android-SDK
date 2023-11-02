package cn.entertech.affectivecloudsdk.utils

import cn.entertech.affective.sdk.bean.AffectiveDataCategory
import cn.entertech.affective.sdk.bean.BioDataCategory
import cn.entertech.affectivecloudsdk.entity.ResponseBody
import java.lang.IllegalStateException

class ReportGenerator {
    private var reportBioDataCategoryList: List<BioDataCategory>? = null
    private var reportAffectiveDataCategoryList: List<AffectiveDataCategory>? = null

    fun setBioDataCategories(bioDataCategories:List<BioDataCategory>){
        this.reportBioDataCategoryList=bioDataCategories
    }
    fun setAffectiveDataCategories(affectiveDataCategories:List<AffectiveDataCategory>){
        this.reportAffectiveDataCategoryList=affectiveDataCategories
    }


    fun appendResponse(responseBody: ResponseBody): HashMap<Any, Any?>? {
        if (reportBioDataCategoryList?.isEmpty()!=false || reportAffectiveDataCategoryList?.isEmpty()!=false) {
            throw IllegalStateException("please init ReportGenerator first")
        }
        val item=HashMap<Any,Any?>()
        var data = responseBody.data
        var keys = responseBody.data.keys
        keys.forEach {
            for (service in reportBioDataCategoryList!!) {
                if (it == service.value)
                    item[it] = data[it]
            }
            for (service in reportAffectiveDataCategoryList!!) {
                if (it == service.value)
                    item[it] = data[it]
            }
        }
        for (service in reportBioDataCategoryList!!) {
            if (!item.containsKey(service.value)) {
                return null
            }
        }
        for (service in reportAffectiveDataCategoryList!!) {
            if (!item.containsKey(service.value)) {
                return null
            }
        }
        return item
    }


    fun appendBioDataResponse(responseBody: ResponseBody): HashMap<Any, Any?>?{
        if (reportBioDataCategoryList?.isEmpty()!=false) {
            throw IllegalStateException("please set breportBioDataCategoryList first")
        }
        val item=HashMap<Any,Any?>()
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

    fun appendAffectiveDataResponse(responseBody: ResponseBody): HashMap<Any, Any?>?{
        if (reportAffectiveDataCategoryList?.isEmpty()!=false) {
            throw IllegalStateException("please set AffectiveDataResponse first")
        }
        val item=HashMap<Any,Any?>()
        var data = responseBody.data
        var keys = responseBody.data.keys

        keys.forEach {
            for (service in reportAffectiveDataCategoryList!!) {
                if (it == service.value)
                    item[it] = data[it]
            }
        }
        for (service in reportAffectiveDataCategoryList!!) {
            if (!item.containsKey(service.value)) {
                return null
            }
        }
        return item
    }

}