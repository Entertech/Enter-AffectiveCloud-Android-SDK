package cn.entertech.affective.sdk.bean


data class EnterAffectiveConfigProxy(
    val availableBiaCategory: List<BioDataCategory> =BioDataCategory.values().toList() ,
    val availableAffectiveCategories: List<AffectiveDataCategory> = AffectiveDataCategory.values().toList(),
    val sex: String = "",
    val userId: Int = -1,
    val appSecret: String = "",
    val appKey: String = "",
    val age: Int = 0,
)

