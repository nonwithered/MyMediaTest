package com.example.shared.utils

import kotlin.jvm.JvmName

inline fun <T : Any> T?.elseValue(crossinline value: () -> T): T {
    return this ?: value()
}

fun <T : Any> T?.elseValue(value: T): T {
    return elseValue { value }
}

val Boolean?.elseTrue
    get() = elseValue(true)
val Boolean?.elseFalse
    get() = elseValue(false)

val Int?.elseZero: Int
    get() = elseValue(0)
val Long?.elseZero: Long
    get() = elseValue(0)
val Short?.elseZero: Short
    get() = elseValue(0)
val Byte?.elseZero: Byte
    get() = elseValue(0)

val Float?.elseZero: Float
    get() = elseValue(0f)
val Double?.elseZero: Double
    get() = elseValue(0.0)

val Char?.elseEmpty: Char
    get() = elseValue('\u0000')

val CharSequence?.elseEmpty: CharSequence
    get() = elseValue("")
val String?.elseEmpty: String
    get() = elseValue("")

val <T> List<T>?.elseEmpty: List<T>
    get() = elseValue(listOf())
val <T> MutableList<T>?.elseEmpty: MutableList<T>
    @JvmName("getElseEmptyMutable")
    get() = elseValue(mutableListOf())

val <T> Set<T>?.elseEmpty: Set<T>
    get() = elseValue(setOf())
val <T> MutableSet<T>?.elseEmpty: MutableSet<T>
    @JvmName("getElseEmptyMutable")
    get() = elseValue(mutableSetOf())

val <K, V> Map<K, V>?.elseEmpty: Map<K, V>
    get() = elseValue(mapOf())
val <K, V> MutableMap<K, V>?.elseEmpty: Map<K, V>
    @JvmName("getElseEmptyMutable")
    get() = elseValue(mutableMapOf())

inline val <reified T> Array<T>?.elseEmpty: Array<T>
    get() = elseValue(arrayOf())

val Iterable<Any?>.hashCodeCombined: Int
    get() {
        var result = 1
        forEach {
            result *= 31
            result += it.hashCode()
        }
        return result
    }

infix fun List<Any?>.equalsCombined(rhs: List<Any?>): Boolean {
    val lhs = this
    if (lhs.size != rhs.size) {
        return false
    }
    repeat(size) { i ->
        if (lhs[i] != rhs[i]) {
            return false
        }
    }
    return true
}

val Int.toStringHex: String
    get() = Integer.toHexString(this)

val Long.toStringHex: String
    get() = java.lang.Long.toHexString(this)

val Short.toStringHex: String
    get() = toInt().toStringHex

val Byte.toStringHex: String
    get() = toInt().toStringHex

val Float.toStringHex: String
    get() = java.lang.Float.toHexString(this)

val Double.toStringHex: String
    get() = java.lang.Double.toHexString(this)

val Char.toStringHex: String
    get() = code.toStringHex
