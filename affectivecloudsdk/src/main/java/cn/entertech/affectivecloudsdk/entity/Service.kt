package cn.entertech.affectivecloudsdk.entity

enum class Service(var value: String) {
    EEG("eeg"), HR("hr"),
    ATTENTION("attention"), RELAXATION("relaxation"),
    PRESSURE("pressure"), PLEASURE("pleasure"),
    AROUSAL("arousal"), SLEEP("sleep")
}