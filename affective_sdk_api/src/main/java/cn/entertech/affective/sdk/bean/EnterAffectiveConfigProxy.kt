package cn.entertech.affective.sdk.bean


data class EnterAffectiveConfigProxy(
    val availableBiaCategory: List<BioDataCategory>? = null,
    val availableAffectiveCategories: List<AffectiveDataCategory>? = null,
    val sex: String = "",
    val userId: String = "",
    val appSecret: String = "",
    val appKey: String = "",
    val age: Int = 0,
)

