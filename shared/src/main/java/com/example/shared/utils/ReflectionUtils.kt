package com.example.shared.utils

import kotlin.reflect.KClass

@JvmName("newInstanceVarargClass")
inline fun <reified T : Any> newInstance(vararg args: Pair<Class<*>, *>): T? {
    return mapOf(*args).runCatching {
        T::class.java.getConstructor(*keys.toTypedArray()).newInstance(*values.toTypedArray())
    }.getOrNull()
}

@JvmName("newInstanceVarargKClass")
inline fun <reified T : Any> newInstance(vararg args: Pair<KClass<*>, *>): T? {
    return newInstance<T>(*mapOf(*args).mapKeys { it.key.java }.toPairs().toTypedArray())
}

@JvmName("newInstanceVarargAny")
inline fun <reified T : Any> newInstance(vararg args: Any): T? {
    return newInstance<T>(*args.map { it.javaClass to it }.toTypedArray())
}

@JvmName("newInstanceVarargDefault")
inline fun <reified T : Any> newInstance(): T? {
    return runCatching {
        T::class.java.getConstructor().newInstance()
    }.getOrNull()
}
