package cn.entertech.affective.sdk.bean

import com.google.gson.annotations.SerializedName

enum class BioDataCategory(
    @SerializedName("value")
    var value: String
) {
    EEG("eeg"), HR("hr-v2"), MCEEG("mceeg"),
    SCEEG("sceeg"), BCG("bcg"), GYRO("gyro"),
    PEPR("pepr"),
}