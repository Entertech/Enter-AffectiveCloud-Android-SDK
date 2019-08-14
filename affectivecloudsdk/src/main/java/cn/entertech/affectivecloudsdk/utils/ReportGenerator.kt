package cn.entertech.affectivecloudsdk.utils

import cn.entertech.biomoduledemo.entity.ResponseBody
import java.lang.IllegalStateException

class ReportGenerator {
    private var reportServiceList: List<String>? = null
    private var item: HashMap<Any, Any?> = HashMap()

    fun init(services: List<String>) {
        this.reportServiceList = services
    }

    fun appendResponse(responseBody: ResponseBody): HashMap<Any, Any?>? {
        if (reportServiceList == null || reportServiceList!!.isEmpty()) {
            throw IllegalStateException("please init ReportGenerator first")
        }
        if (responseBody.data != null) {
            var data = responseBody.data
            var keys = responseBody.data.keys
            keys.forEach {
                if (reportServiceList!!.contains(it)) {
                    item[it] = data[it]
                }
            }
        }
        for (dataType in reportServiceList!!) {
            if (!item.containsKey(dataType)) {
                return null
            }
        }
        return item
    }

    fun getReport():HashMap<Any, Any?>?{
        return item
    }

}