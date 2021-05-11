package cn.entertech.affectivecloudsdk.entity

data class SourceDataRecordListWithPages(
    val count: Int,
    val next: String,
    val previous: Any,
    val results: List<SourceDataRecord>
)