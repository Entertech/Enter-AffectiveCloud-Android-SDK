package cn.entertech.affectivecloudsdk.entity

import com.google.gson.annotations.SerializedName

enum class Service(@SerializedName("value") var value: String) {
    EEG("eeg"), HR("hr-v2"), MCEEG("mceeg"), BCG("bcg"),GYRO("gyro"),
    ATTENTION("attention"), RELAXATION("relaxation"),
    PRESSURE("pressure"), PLEASURE("pleasure"),
    AROUSAL("arousal"), SLEEP("sleep"), COHERENCE("coherence")
}