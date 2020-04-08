package cn.entertech.affectivecloudsdk.entity

import com.google.gson.annotations.SerializedName

data class Error(@SerializedName("code") var code:Int, @SerializedName("msg") var msg:String){
    override fun toString(): String {
        return "Error(code=$code, msg='$msg')"
    }
}