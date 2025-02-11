package com.example.shared.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.ref.Reference
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
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

operator fun AtomicBoolean.getValue(owner: Any?, property: KProperty<*>): Boolean {
    return get()
}

operator fun AtomicBoolean.setValue(owner: Any?, property: KProperty<*>, v: Boolean) {
    set(v)
}

operator fun AtomicInteger.getValue(owner: Any?, property: KProperty<*>): Int {
    return get()
}

operator fun AtomicInteger.setValue(owner: Any?, property: KProperty<*>, v: Int) {
    set(v)
}

operator fun AtomicLong.getValue(owner: Any?, property: KProperty<*>): Long {
    return get()
}

operator fun AtomicLong.setValue(owner: Any?, property: KProperty<*>, v: Long) {
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

val <T : Any> (() -> T).once: () -> T?
    get() {
        val ref = AtomicReference(this)
        return {
            var result: T? = null
            ref.tryClear { r ->
                result = r()
            }
            result
        }
    }

@get:JvmName("runOnce")
val (() -> Unit).once: () -> Unit
    get() {
        val r = (this as (() -> Any)).once
        return {
            r()
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

class CloseableGroup : AutoCloseable {

    private val lock: Lock = ReentrantLock()

    private val list = mutableListOf<AutoCloseable>()

    operator fun plusAssign(closeable: AutoCloseable): Unit = lock.withLock {
        list += closeable
    }

    override fun close() : Unit = lock.withLock {
        while (list.isNotEmpty()) {
            list.removeLastOrNull()?.close()
        }
    }
}

typealias TimeStamp = Pair<Long, TimeUnit>
