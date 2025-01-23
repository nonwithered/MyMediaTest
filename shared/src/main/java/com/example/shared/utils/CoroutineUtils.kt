package com.example.shared.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KClass

fun Job.dispose() {
    cancel()
}

inline fun <reified E : Throwable, R> runCatchingTyped(block: () -> R): Result<R?> {
    return runCatching {
        try {
            block()
        } catch (e: Throwable) {
            if (e !is E) {
                throw e
            }
            null
        }
    }
}

fun <R> runCatchingTyped(types: Map<KClass<out Throwable>, (Throwable) -> R?>, block: () -> R): Result<R?> {
    return runCatching {
        try {
            block()
        } catch (e: Throwable) {
            var match = false
            var result: R? = null
            types.forEach { (k, v) ->
                if (!match && k.isInstance(e)) {
                    match = true
                    @Suppress("UNCHECKED_CAST")
                    result = v(e)
                }
            }
            if (!match) {
                throw e
            }
            result
        }
    }
}

fun <R> runCatchingTyped(vararg types: Pair<KClass<out Throwable>, (Throwable) -> R?>, block: () -> R): Result<R?> {
    return runCatchingTyped(types.toMap(), block)
}

fun <T : Any> T.autoRefScope(coroutineContext: CoroutineContext = EmptyCoroutineContext): CoroutineScope {
    val scope = CoroutineScope(coroutineContext)
    Cleaner.common.register(this) {
        scope.coroutineContext.cancel()
    }
    return scope
}

class CaptureCoroutineScope<T : Any>(
    scope: CoroutineScope,
    ref: T,
) : CoroutineScope by scope {

    val capture by ref.weak
}

fun <T : Any> CoroutineScope.capture(ref: T): CaptureCoroutineScope<T> {
    return CaptureCoroutineScope(this, ref)
}

fun <T : Any, R> CoroutineScope.capture(ref: T, block: CaptureCoroutineScope<T>.() -> R): R {
    return capture(ref).block()
}

suspend fun onDispose(block: () -> Unit): Nothing {
    try {
        suspendCoroutine<Nothing> {
        }
    } finally {
        block()
    }
}

class AutoLauncher(
    msg: () -> String,
    coroutineContext: () -> CoroutineContext,
) {

    constructor(
        msg: String,
        coroutineContext: () -> CoroutineContext,
    ) : this({ msg }, coroutineContext)

    private val msg: String by msg

    private val coroutineContext by coroutineContext

    @Volatile
    private var scope: CoroutineScope? = null

    private var tasks = mutableMapOf<suspend CoroutineScope.() -> Unit, Job?>()

    private val lock: Lock = ReentrantLock()

    private fun dispose(block: suspend CoroutineScope.() -> Unit): Unit = lock.withLock {
        tasks.remove(block)?.cancel()
    }

    fun launch(block: suspend CoroutineScope.() -> Unit): () -> Unit = lock.withLock {
        tasks[block] = scope?.launch(block = block)
        return {
            dispose(block)
        }
    }

    var attach: Boolean
        get() = scope !== null
        set(value) {
            if (value) {
                onAttach()
            } else {
                onDetach()
            }
        }

    private fun onAttach(): Unit = lock.withLock {
        if (scope !== null) {
            return@withLock
        }
        TAG.logD { "onAttach $msg" }
        val coroutineScope = CoroutineScope(coroutineContext)
        scope = coroutineScope
        tasks = tasks.mapValuesTo(mutableMapOf()) {
            coroutineScope.launch(block = it.key)
        }
    }

    private fun onDetach(): Unit = lock.withLock {
        val coroutineScope = scope ?: return@withLock
        TAG.logD { "onDetach $msg" }
        scope = null
        tasks = tasks.mapValuesTo(mutableMapOf()) {
            it.value?.cancel()
            null
        }
        coroutineScope.cancel()
    }
}
