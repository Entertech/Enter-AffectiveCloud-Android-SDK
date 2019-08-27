package cn.entertech.affectivecloudsdk.entity

data class Error(var code:Int,var msg:String){
    override fun toString(): String {
        return "Error(code=$code, msg='$msg')"
    }
}