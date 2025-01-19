package com.example.shared.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.ref.Reference
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KProperty

operator fun <T> StateFlow<T>.getValue(owner: Any?, property: KProperty<*>): T {
    return value
}

operator fun <T> MutableStateFlow<T>.setValue(owner: Any?, property: KProperty<*>, v: T) {
    value = v
}

operator fun <T> LiveData<T>.getValue(owner: Any?, property: KProperty<*>): T? {
    return value
}

operator fun <T> MutableLiveData<T>.setValue(owner: Any?, property: KProperty<*>, v: T?) {
    value = v
}

operator fun <T> AtomicReference<T>.getValue(owner: Any?, property: KProperty<*>): T? {
    return get()
}

operator fun <T> AtomicReference<T>.setValue(owner: Any?, property: KProperty<*>, v: T?) {
    set(v)
}

operator fun <T> ThreadLocal<T>.getValue(owner: Any?, property: KProperty<*>): T? {
    return get()
}

operator fun <T> ThreadLocal<T>.setValue(owner: Any?, property: KProperty<*>, v: T?) {
    set(v)
}

operator fun <T> Reference<T>.getValue(owner: Any?, property: KProperty<*>): T? {
    return get()
}

operator fun <T> (() -> T).getValue(owner: Any?, property: KProperty<*>): T {
    return this()
}
