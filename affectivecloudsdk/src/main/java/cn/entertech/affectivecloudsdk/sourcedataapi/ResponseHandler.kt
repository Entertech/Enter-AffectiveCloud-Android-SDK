package cn.entertech.affectivecloudsdk.sourcedataapi

import cn.entertech.affective.sdk.utils.AffectiveLogHelper
import retrofit2.HttpException
import java.net.SocketTimeoutException

open class ResponseHandler {
    companion object{
        fun <T : Any> handleSuccess(data: T?): Resource<T> {
            return Resource.success(data)
        }

        fun <T : Any> handleException(e: Exception): Resource<T> {
            AffectiveLogHelper.d("########","handle exception e is ${e.printStackTrace()}")
            return when (e) {
                is HttpException -> Resource.error(getErrorMessage(e.code()), null)
                is SocketTimeoutException -> Resource.error(getErrorMessage(-1), null)
                else -> Resource.error(getErrorMessage(Int.MAX_VALUE), null)
            }
        }

        private fun getErrorMessage(code: Int): String {
            return when (code) {
                -1 -> "Timeout"
                401 -> "Unauthorised"
                404 -> "Not found"
                else -> "Something went wrong"
            }
        }
    }
}