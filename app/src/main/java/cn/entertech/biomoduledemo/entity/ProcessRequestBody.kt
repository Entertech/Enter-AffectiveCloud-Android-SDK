package cn.entertech.biomoduledemo.entity

data class ProcessRequestBody(val services: String, val op: String, val data: HashMap<Any, Any>)