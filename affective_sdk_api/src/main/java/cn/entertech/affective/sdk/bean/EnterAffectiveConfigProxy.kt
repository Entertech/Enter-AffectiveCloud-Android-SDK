package cn.entertech.affective.sdk.bean

import cn.entertech.affective.sdk.bean.type.BioDataCategory
import cn.entertech.affective.sdk.bean.type.PhysiologyDataCategory


data class EnterAffectiveConfigProxy(
    val availableBiaCategory: List<BioDataCategory> = BioDataCategory.getDefaultList(),
    val availableAffectiveCategories: List<PhysiologyDataCategory> = PhysiologyDataCategory.getDefaultList(),
    val sex: String = "",
    val userId: String = "",
    val appSecret: String = "",
    val limitWaveValue: Boolean = true,
    val appKey: String = "",
    val age: Int = 0,
) {
    companion object {
        const val SEX_MAN = "m"
        const val SEX_WOMEN = "f"
        const val SEX_OTHER = "0"
    }
}

