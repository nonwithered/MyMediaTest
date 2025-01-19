package com.example.shared.utils

import android.app.Application
import android.content.Context
import com.example.shared.app.BaseApplication
import kotlin.reflect.KClass

val app: Application
    get() = BaseApplication.globalApplication

fun <T : Any> Context.getSystemService(type: KClass<T>): T {
    return getSystemService(type)
}

inline fun <reified T : Any> Context.getSystemService(): T {
    return getSystemService(T::class)
}
