package cn.entertech.affectivecloudsdk.entity

class RealtimePEPRData {
    //脉搏波
    var bcgWave: ArrayList<Double>? = null
    //呼吸波
    var rwWave: ArrayList<Double>? = null
    //脉搏波质量等级。 0：表示未佩戴；1：有数据没信号；2：有数据信号良好
    var bcgQuality: Int? = null
    //呼吸波质量等级。 0：表示未佩戴；1：有数据没信号；2：有数据信号良好
    var rwQuality: Int? = null
    //心率值 单位：BPM
    var hr: Double? = null
    //心率变异性值 单位：毫秒
    var hrv: Double? = null
}