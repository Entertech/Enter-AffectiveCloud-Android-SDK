package cn.entertech.affectivecloudsdk.utils

import cn.entertech.affectivecloudsdk.entity.Service
import cn.entertech.biomoduledemo.entity.ResponseBody
import java.lang.IllegalStateException

class ReportGenerator {
    private var reportServiceList: List<Service>? = null
    private var item: HashMap<Any, Any?> = HashMap()

    fun init(services: List<Service>) {
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
                for (service in reportServiceList!!) {
                    if (it == service.value)
                        item[it] = data[it]
                }
            }
        }
        for (service in reportServiceList!!) {
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