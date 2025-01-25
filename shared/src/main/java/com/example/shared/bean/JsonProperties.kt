package com.example.shared.bean

import org.json.JSONArray
import org.json.JSONObject
import kotlin.reflect.KClass

class JsonProperties(
    private val json: JSONObject = JSONObject(),
) : BaseProperties<Any>() {

    fun asJson(): JSONObject = json

    @Suppress("IMPLICIT_CAST_TO_ANY")
    override fun getPropertyValue(type: KClass<*>, k: String): Any? = json.run {
        if (!has(k)) {
            return null
        }
        val v = when (type) {
            Boolean::class -> getBoolean(k)
            Int::class -> getInt(k)
            Long::class -> getLong(k)
            Short::class -> getInt(k).toShort()
            Byte::class -> getInt(k).toByte()
            Float::class -> getDouble(k).toFloat()
            Double::class -> getDouble(k)
            Char::class -> getString(k).takeIf { it.length == 1 }?.first()
            String::class -> getString(k)
            JSONArray::class -> getJSONArray(k)
            JSONObject::class -> getJSONObject(k)
            else -> null
        }
        return v
    }

    override fun setPropertyValue(type: KClass<*>, k: String, v: Any?) {
        if (v === null) {
            json.remove(k)
            return
        }
        json.put(k, v)
    }
}
