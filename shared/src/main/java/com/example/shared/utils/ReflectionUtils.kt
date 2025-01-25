package com.example.shared.utils

import kotlin.reflect.KClass

@JvmName("newInstanceVarargClass")
fun <T : Any> Class<T>.newInstance(vararg args: Pair<Class<*>, *>): T? {
    return mapOf(*args).runCatching {
        getConstructor(*keys.toTypedArray()).newInstance(*values.toTypedArray())
    }.onFailure { e ->
        TAG.logE(e) { "newInstance ${args.toList()}" }
    }.getOrNull()
}

@JvmName("newInstanceVarargKClass")
fun <T : Any> Class<T>.newInstance(vararg args: Pair<KClass<*>, *>): T? {
    return newInstance(*mapOf(*args).mapKeys { it.key.java }.toPairs().toTypedArray())
}

@JvmName("newInstanceVarargAny")
fun <T : Any> Class<T>.newInstance(vararg args: Any): T? {
    return newInstance(*args.map { it.javaClass to it }.toTypedArray())
}

@JvmName("newInstanceVarargDefault")
fun <T : Any> Class<T>.newInstanceDefault(): T? {
    return runCatching {
        getConstructor().newInstance()
    }.onFailure { e ->
        TAG.logE(e) { "newInstanceDefault" }
    }.getOrNull()
}

@JvmName("newInstanceVarargClass")
fun <T : Any> KClass<T>.newInstance(vararg args: Pair<Class<*>, *>): T? {
    return java.newInstance(*args)
}

@JvmName("newInstanceVarargKClass")
fun <T : Any> KClass<T>.newInstance(vararg args: Pair<KClass<*>, *>): T? {
    return java.newInstance(*args)
}

@JvmName("newInstanceVarargAny")
fun <T : Any> KClass<T>.newInstance(vararg args: Any): T? {
    return java.newInstance(*args)
}

@JvmName("newInstanceVarargDefault")
fun <T : Any> KClass<T>.newInstanceDefault(): T? {
    return java.newInstanceDefault()
}

@JvmName("newInstanceVarargClass")
inline fun <reified T : Any> newInstance(vararg args: Pair<Class<*>, *>): T? {
    return T::class.newInstance(*args)
}

@JvmName("newInstanceVarargKClass")
inline fun <reified T : Any> newInstance(vararg args: Pair<KClass<*>, *>): T? {
    return T::class.newInstance(*args)
}

@JvmName("newInstanceVarargAny")
inline fun <reified T : Any> newInstance(vararg args: Any): T? {
    return T::class.newInstance(*args)
}

@JvmName("newInstanceVarargDefault")
inline fun <reified T : Any> newInstanceDefault(): T? {
    return T::class.newInstanceDefault()
}
