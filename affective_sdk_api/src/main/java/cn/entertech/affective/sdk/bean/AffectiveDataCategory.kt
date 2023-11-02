package cn.entertech.affective.sdk.bean

import com.google.gson.annotations.SerializedName

enum class AffectiveDataCategory(
    @SerializedName("value")
    var value: String
) {
    DCEEG_SSVEP("dceeg-ssvep"),
    SSVEP_MULTI_CLASSIFY("ssvep-multi-classify"),
    ATTENTION("attention"), RELAXATION("relaxation"),
    PRESSURE("pressure"), PLEASURE("pleasure"),
    AROUSAL("arousal"), SLEEP("sleep"),
    COHERENCE("coherence"), FLOW("meditation")
}