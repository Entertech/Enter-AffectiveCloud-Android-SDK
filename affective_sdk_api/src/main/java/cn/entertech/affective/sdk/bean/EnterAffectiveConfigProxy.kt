package cn.entertech.affective.sdk.bean


data class EnterAffectiveConfigProxy(
    val availableAffectiveServices: List<Service>,
    val availableBiodataServices: List<Service>,
    val sex: String = "",
    val userId: String = "",
    val appSecret: String = "",
    val appKey: String = "",
    val age: Int = 0,
)

