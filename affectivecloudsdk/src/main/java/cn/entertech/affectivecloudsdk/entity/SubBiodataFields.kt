package cn.entertech.affectivecloudsdk.entity

import com.google.gson.annotations.SerializedName

data class SubBiodataFields(@SerializedName("subEEGFields") var subEEGFields:List<String>? = null,@SerializedName("subHrFields") var subHrFields:List<String>? = null){
    override fun toString(): String {
        return "SubBiodataFields(subEEGFields=$subEEGFields, subHrFields=$subHrFields)"
    }
}