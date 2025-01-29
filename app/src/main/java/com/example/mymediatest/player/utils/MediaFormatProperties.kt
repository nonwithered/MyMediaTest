package com.example.mymediatest.player.utils

import android.media.MediaFormat
import com.example.shared.bean.BaseProperties
import java.nio.ByteBuffer
import kotlin.reflect.KClass

open class MediaFormatProperties(
    private val format: MediaFormat = MediaFormat(),
) : BaseProperties<Any>() {

    fun asMediaFormat(): MediaFormat {
        return format
    }

    override fun getPropertyValue(type: KClass<*>, k: String): Any? = format.run {
        if (!containsKey(k)) {
            return null
        }
        when (type) {
            Number::class -> getNumber(k)
            Int::class -> getInteger(k)
            Long::class -> getLong(k)
            Short::class -> getInteger(k).toShort()
            Byte::class -> getInteger(k).toByte()
            Float::class -> getFloat(k)
            Double::class -> getFloat(k).toDouble()
            String::class -> getString(k)
            ByteBuffer::class -> getByteBuffer(k)
            else -> null
        }
    }

    override fun setPropertyValue(type: KClass<*>, k: String, v: Any?) = format.run {
        if (v === null) {
            removeKey(k)
            return
        }
        when (type) {
            Number::class -> setFloat(k, (v as Number).toFloat())
            Int::class -> setInteger(k, v as Int)
            Long::class -> setLong(k, v as Long)
            Short::class -> setInteger(k, (v as Short).toInt())
            Byte::class -> setInteger(k, (v as Byte).toInt())
            Float::class -> setFloat(k, v as Float)
            Double::class -> setFloat(k, (v as Double).toFloat())
            String::class -> setString(k, v as String)
            ByteBuffer::class -> setByteBuffer(k, v as ByteBuffer)
            else -> {}
        }
    }
}
