package com.example.shared.utils

import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

private fun mapKClassPairToJavaClass(vararg args: Pair<KClass<*>, *>): Array<Pair<Class<*>, *>> {
    return mapOf(*args).mapKeys { it.key.java }.toPairs().toTypedArray()
}

fun <T : Any> Class<T>.newInstanceSafe(vararg args: Pair<Class<*>, *>): Result<T> {
    return mapOf(*args).runCatching {
        getConstructor(*keys.toTypedArray()).newInstance(*values.toTypedArray())
    }
}

fun <T : Any> KClass<T>.newInstanceSafe(vararg args: Pair<KClass<*>, *>): Result<T> {
    return java.newInstanceSafe(*mapKClassPairToJavaClass(*args))
}

fun <T : Any> KClass<T>.newInstanceDefaultSafe(): Result<T> {
    return runCatching {
        java.getConstructor().newInstance()
    }
}

fun <T : Any> CharSequence.parseClass(): Class<T>? {
    return runCatching {
        @Suppress("UNCHECKED_CAST")
        Class.forName(this.toString()) as? Class<T>
    }.getOrNull()
}

fun <T : Any, R> Class<T>.invokeStaticMethodSafe(name: String, vararg args: Pair<Class<*>, *>): Result<R> {
    return mapOf(*args).runCatching {
        @Suppress("UNCHECKED_CAST")
        getDeclaredMethod(name, *keys.toTypedArray()).invoke(null, *values.toTypedArray()) as R
    }
}

fun <T : Any, R> KClass<T>.invokeStaticMethodSafe(name: String, vararg args: Pair<KClass<*>, *>): Result<R> {
    return java.invokeStaticMethodSafe(name, *mapKClassPairToJavaClass(*args))
}

fun Class<*>.getTypeArguments(): Result<List<Class<*>>> {
    return runCatching {
        (genericSuperclass as ParameterizedType).actualTypeArguments.map { it as Class<*> }
    }
}
