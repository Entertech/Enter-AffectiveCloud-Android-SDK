package cn.entertech.affective.sdk.utils

object AffectiveLogHelper {

    var printer:ILogPrinter=DefaultLogPrinter

    fun d(tag:String,msg:String){
        printer.d(tag,msg)
    }

    fun i(tag:String,msg:String){
        printer.i(tag,msg)
    }

    fun e(tag: String,msg:String){
        printer.e(tag,msg)
    }
}