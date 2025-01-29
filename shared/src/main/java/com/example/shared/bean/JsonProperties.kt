package com.example.shared.bean

import org.json.JSONArray
import org.json.JSONObject
import kotlin.reflect.KClass

open class JsonProperties(
    private val json: JSONObject = JSONObject(),
) : BaseProperties<Any>() {

    fun asJson(): JSONObject = json

    override fun getPropertyValue(type: KClass<*>, k: String): Any? = json.run {
        if (!has(k)) {
            return null
        }
        when (type) {
            Boolean::class -> getBoolean(k)
            Int::class -> getInt(k)
            Long::class -> getLong(k)
            Short::class -> getInt(k).toShort()
            Byte::class -> getInt(k).toByte()
            Float::class -> getDouble(k).toFloat()
            Double::class -> getDouble(k)
            Char::class -> getString(k).takeIf { it.length == 1 }?.first()!!
            String::class -> getString(k)
            JSONArray::class -> getJSONArray(k)
            JSONObject::class -> getJSONObject(k)
            else -> null
        }
    }

    override fun setPropertyValue(type: KClass<*>, k: String, v: Any?): Unit = json.run {
        if (v === null) {
            remove(k)
            return
        }
        when (type) {
            Boolean::class -> put(k, v as Boolean)
            Int::class -> put(k, v as Int)
            Long::class -> put(k, v as Long)
            Short::class -> put(k, (v as Short).toInt())
            Byte::class -> put(k, (v as Byte).toInt())
            Float::class -> put(k, (v as Float).toDouble())
            Double::class -> put(k, v as Double)
            Char::class -> put(k, (v as Char).toString())
            String::class -> put(k, v as String)
            else -> put(k, v)
        }
    }
}
