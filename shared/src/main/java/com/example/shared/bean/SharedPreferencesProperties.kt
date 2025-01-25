package com.example.shared.bean

import android.content.SharedPreferences
import kotlin.reflect.KClass

class SharedPreferencesProperties(
    private val sharedPreferences: SharedPreferences,
) : BaseProperties<Any>() {

    override fun getPropertyValue(type: KClass<*>, k: String): Any? = sharedPreferences.run {
        if (!contains(k)) {
            return null
        }
        val v = when (type) {
            Boolean::class -> getBoolean(k, false)
            Int::class -> getInt(k, 0)
            Long::class -> getLong(k, 0)
            Short::class -> getInt(k, 0).toShort()
            Byte::class -> getInt(k, 0).toByte()
            Float::class -> getFloat(k, 0f)
            Double::class -> getFloat(k, 0f).toDouble()
            Char::class -> getString(k, null)?.takeIf { it.length == 1 }?.first()!!
            String::class -> getString(k, null)
            else -> null
        }
        return v
    }

    override fun setPropertyValue(type: KClass<*>, k: String, v: Any?): Unit = sharedPreferences.edit().apply {
        if (v === null) {
            remove(k)
        }
        when (type) {
            Boolean::class -> putBoolean(k, v as Boolean)
            Int::class -> putInt(k, v as Int)
            Long::class -> putLong(k, v as Long)
            Short::class -> putInt(k, v as Int)
            Byte::class -> putInt(k, v as Int)
            Float::class -> putFloat(k, v as Float)
            Double::class -> putFloat(k, v as Float)
            Char::class -> putString(k, (v as Char).toString())
            String::class -> putString(k, v as String)
            else -> {
            }
        }
    }.apply()
}
