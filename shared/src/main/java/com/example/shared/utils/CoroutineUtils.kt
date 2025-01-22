package com.example.shared.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
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

fun <T : Any> T.autoScope(coroutineContext: CoroutineContext = EmptyCoroutineContext): CoroutineScope {
    val scope = CoroutineScope(coroutineContext + SupervisorJob())
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
