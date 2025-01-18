package com.example.shared.utils

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.ContentProvider
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
import android.content.pm.ServiceInfo
import kotlin.reflect.KClass

val Class<*>.componentName: ComponentName
    get() = ComponentName(app, name)

val KClass<*>.componentName: ComponentName
    get() = java.componentName

inline fun <reified T : Any> componentName(): ComponentName {
    return T::class.componentName
}

fun getPackageInfo(
    packageName: String,
    flags: Int,
): PackageInfo {
    return app.packageManager.getPackageInfo(packageName, flags)
}

fun getApplicationInfo(
    packageName: String,
    flags: Int,
): ApplicationInfo {
    return app.packageManager.getApplicationInfo(packageName, flags)
}
inline fun <reified T : Activity> getActivityInfo(flags: Int): ActivityInfo {
    return app.packageManager.getActivityInfo(componentName<T>(), flags)
}

inline fun <reified T : Service> getServiceInfo(flags: Int): ServiceInfo {
    return app.packageManager.getServiceInfo(componentName<T>(), flags)
}

inline fun <reified T : BroadcastReceiver> getReceiverInfo(flags: Int): ActivityInfo {
    return app.packageManager.getReceiverInfo(componentName<T>(), flags)
}

inline fun <reified T : ContentProvider> getProviderInfo(flags: Int): ProviderInfo {
    return app.packageManager.getProviderInfo(componentName<T>(), flags)
}

fun Context.info(flags: Int = PackageManager.GET_META_DATA) = getPackageInfo(packageName, flags)

fun Application.info(flags: Int = PackageManager.GET_META_DATA) = getApplicationInfo(packageName, flags)

@JvmName("getActivityInfoTyped")
inline fun <reified T : Activity> T.info(flags: Int = PackageManager.GET_META_DATA) = getActivityInfo<T>(flags)

@JvmName("getServiceInfoTyped")
inline fun <reified T : Service> T.info(flags: Int = PackageManager.GET_META_DATA) = getServiceInfo<T>(flags)

@JvmName("getReceiverInfoTyped")
inline fun <reified T : BroadcastReceiver> T.info(flags: Int = PackageManager.GET_META_DATA) = getReceiverInfo<T>(flags)

@JvmName("getProviderInfoTyped")
inline fun <reified T : ContentProvider> T.info(flags: Int = PackageManager.GET_META_DATA) = getProviderInfo<T>(flags)
