package cn.entertech.affectivecloudsdk.entity

data class SourceDataRecord(
    val age: Int,
    val app: Int,
    val case: List<Int>,
    val client_id: String,
    val close_time: String,
    val data_type: String,
    val device: Any,
    val gmt_create: String,
    val gmt_modify: String,
    val mode: List<Int>,
    val rec: String,
    val record_id: Int,
    val session_id: String,
    val sex: String,
    val start_time: String,
    val url: String
)