package com.example.shared.bean

import kotlin.reflect.KClass

open class MapProperties<T : Any>(
    private val map: MutableMap<String, T?> = mutableMapOf(),
) : BaseProperties<T>() {

    fun asMap(): Map<String, T?> = map

    override fun getPropertyValue(type: KClass<*>, k: String): T? {
        return map[k]
    }

    override fun setPropertyValue(type: KClass<*>, k: String, v: T?) {
        if (v === null) {
            map -= k
            return
        }
        map[k] = v
    }
}
