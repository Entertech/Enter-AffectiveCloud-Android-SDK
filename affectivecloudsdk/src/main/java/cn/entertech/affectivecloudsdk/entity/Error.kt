package cn.entertech.affectivecloudsdk.entity

data class Error(var errorCode:Int,var errorMsg:String){
    override fun toString(): String {
        return super.toString()
    }
}