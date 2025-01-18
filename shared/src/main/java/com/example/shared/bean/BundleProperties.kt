package com.example.shared.bean

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable
import kotlin.reflect.KClass

open class BundleProperties(
    private val bundle: Bundle = Bundle(),
) : BaseProperties<Any>() {

    fun asBundle(): Bundle = bundle

    @Suppress("IMPLICIT_CAST_TO_ANY")
    override fun getPropertyValue(type: KClass<*>, k: String): Any? = bundle.run {
        if (!containsKey(k)) {
            return null
        }
        val v = when (type) {
            Boolean::class -> getBoolean(k)
            Int::class -> getInt(k)
            Long::class -> getLong(k)
            Short::class -> getShort(k)
            Byte::class -> getByte(k)
            Float::class -> getFloat(k)
            Double::class -> getDouble(k)
            Char::class -> getChar(k)
            String::class -> getString(k)
            Bundle::class -> getBundle(k)
            Serializable::class -> getSerializable(k)
            Parcelable::class -> getParcelable(k)
            else -> null
        }
        if (!type.isInstance(v)) {
            return null
        }
        return v
    }

    override fun setPropertyValue(type: KClass<*>, k: String, v: Any?) {
        if (v === null) {
            bundle.remove(k)
            return
        }
        if (!type.isInstance(v)) {
            return
        }
        when (type) {
            Boolean::class -> bundle.putBoolean(k, v as Boolean)
            Int::class -> bundle.putInt(k, v as Int)
            Long::class -> bundle.putLong(k, v as Long)
            Short::class -> bundle.putShort(k, v as Short)
            Byte::class -> bundle.putByte(k, v as Byte)
            Float::class -> bundle.putFloat(k, v as Float)
            Double::class -> bundle.putDouble(k, v as Double)
            Char::class -> bundle.putChar(k, v as Char)
            String::class -> bundle.putString(k, v as String)
            Bundle::class -> bundle.putBundle(k, v as Bundle)
            Serializable::class -> bundle.putSerializable(k, v as Serializable)
            else -> {
            }
        }
    }
}
