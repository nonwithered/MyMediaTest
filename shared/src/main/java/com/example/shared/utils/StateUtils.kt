package com.example.shared.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.ref.Reference
import java.lang.ref.WeakReference
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

fun <T : Any> AtomicReference<T>.tryClear(block: (T) -> Unit): Boolean {
    val ref = get()
    if (!compareAndSet(ref, null)) {
        return false
    }
    block(ref)
    return true
}

val (() -> Unit).once: () -> Unit
    get() {
        val ref = AtomicReference(this)
        return {
            ref.tryClear { r ->
                r()
            }
        }
    }

val <T : Any> T.weak: Reference<T>
    get() = WeakReference(this)

class LateInitProxy<V : Any> : AtomicReference<V?>() {

    override fun equals(other: Any?): Boolean {
        return get() === other
    }

    override fun hashCode(): Int {
        return get().hashCode()
    }

    override fun toString(): String {
        return get().toString()
    }

    interface Owner {

        fun onPropertyInit(proxy: LateInitProxy<*>)
    }

    operator fun getValue(owner: Owner, property: KProperty<*>): V {
        return get()!!
    }

    operator fun setValue(owner: Owner, property: KProperty<*>, v: V) {
        if (!compareAndSet(null, v)) {
            throw IllegalStateException(toString())
        }
        owner.onPropertyInit(this)
    }
}

val (() -> Unit).asCloseable: AutoCloseable
    get() = AutoCloseable(this)

val AtomicReference<*>.asCloseable: AutoCloseable
    get() = { set(null) }.asCloseable
