package com.example.shared.utils

import java.util.ServiceLoader
import kotlin.reflect.KClass

fun <T> Class<T>.loadService(): List<T> {
    return ServiceLoader.load(this).iterator().asSequence().toList()
}

fun <T : Any> KClass<T>.loadService(): List<T> {
    return java.loadService()
}

inline fun <reified T : Any> loadService(): List<T> {
    return T::class.loadService()
}
