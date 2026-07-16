package cn.entertech.affective.sdk.api

import cn.entertech.affective.sdk.bean.Error

sealed interface AffectiveServiceResult<out Value> {
    data class Success<Value>(val value: Value) : AffectiveServiceResult<Value>
    data class Failure(val error: Error) : AffectiveServiceResult<Nothing>
}
