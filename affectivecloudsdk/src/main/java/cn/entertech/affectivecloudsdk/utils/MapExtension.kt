package cn.entertech.affectivecloudsdk.utils

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <T, TValue> T.map(properties: Map<Any, TValue>, key: String): ReadOnlyProperty<T, TValue> {
    return object : ReadOnlyProperty<T, TValue> {
        override fun getValue(thisRef: T, property: KProperty<*>) = properties[key]!!
    }
}