package com.example.shared.bean

import kotlin.reflect.KClass

open class MapProperties<T : Any>(
    private val map: MutableMap<String, T?> = mutableMapOf(),
) : BaseProperties<T>() {

    fun asMap(): Map<String, T?> = map

    override fun getPropertyValue(type: KClass<*>, k: String): T? {
        val v = map[k]
        if (!type.isInstance(v)) {
            return null
        }
        return v
    }

    override fun setPropertyValue(type: KClass<*>, k: String, v: T?) {
        if (v === null) {
            map -= k
            return
        }
        if (!type.isInstance(v)) {
            return
        }
        map[k] = v
    }
}
