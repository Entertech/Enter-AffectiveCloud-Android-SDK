package cn.entertech.affectivecloudsdk.entity

import com.google.gson.annotations.SerializedName

data class RealtimeSleepData(
    @SerializedName("sleepDegree") var sleepDegree: Double? = null,
    @SerializedName("sleepState") var sleepState: Double? = null
){

    override fun toString(): String {
        return "RealtimeSleepData(sleepDegree=$sleepDegree, sleepState=$sleepState)"
    }
}
