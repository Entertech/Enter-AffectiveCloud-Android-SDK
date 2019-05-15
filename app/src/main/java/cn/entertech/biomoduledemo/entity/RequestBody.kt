package cn.entertech.biomoduledemo.entity

data class RequestBody(val services: String, val op: String, val kwargs: HashMap<Any, Any>)