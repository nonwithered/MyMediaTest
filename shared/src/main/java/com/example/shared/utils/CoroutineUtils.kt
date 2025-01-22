package com.example.shared.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
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

class AutoCoroutineScope(
    owner: Any,
    coroutineContext: CoroutineContext,
) : CoroutineScope, AutoCloseable {

    init {
        Cleaner.common.register(owner) {
            close()
        }
    }

    override val coroutineContext: CoroutineContext = SupervisorJob() + coroutineContext

    override fun close() {
        coroutineContext.cancel()
    }
}

fun <T : Any> T.autoScope(coroutineContext: CoroutineContext = EmptyCoroutineContext): AutoCoroutineScope {
    return AutoCoroutineScope(this, coroutineContext)
}

fun <T : Any, R> AutoCoroutineScope.withOwner(
    ref: T,
    block: suspend CoroutineScope.(weak: () -> T?) -> R,
): Deferred<R> {
    val weak by ref.weak
    return async {
        block {
            weak
        }
    }
}

fun <T : Any, V> AutoCoroutineScope.withOwnerCollect(
    ref: T,
    flow: Flow<V>,
    block: suspend CoroutineScope.(it: V, owner: T) -> Unit,
): Job {
    return withOwner(ref) { weak ->
        flow.collect {
            weak()?.let { owner ->
                block(it, owner)
            }
        }
    }
}
