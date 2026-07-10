package cn.entertech.affective.sdk.api

import cn.entertech.affective.sdk.bean.Error

sealed interface AffectiveAlgorithmResult<out Value> {
    data class Success<Value>(val value: Value) : AffectiveAlgorithmResult<Value>
    data class Failure(val error: Error) : AffectiveAlgorithmResult<Nothing>
}
