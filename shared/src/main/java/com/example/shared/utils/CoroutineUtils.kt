package com.example.shared.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.lang.ref.Reference
import kotlin.coroutines.CoroutineContext
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

interface CoroutineScopeHelper : CoroutineScope {

    fun <T> Flow<T>.launchCollect(block: suspend (T) -> Unit) {
        launch {
            collect(block)
        }
    }

    fun <T, S> Flow<T>.launchCollect(ref: Reference<S>, block: suspend (self: S, T) -> Unit) {
        launch {
            collect {
                val self = ref.get()
                if (self !== null) {
                    block(self, it)
                }
            }
        }
    }
}

private class CoroutineScopeHelperImpl(
    scope: CoroutineScope,
) : CoroutineScopeHelper, CoroutineScope by scope

private val CoroutineScope.asHelper: CoroutineScopeHelper
    get() = CoroutineScopeHelperImpl(this)

private class AutoCoroutineScope(
    ref: Any,
    dispatcher: CoroutineDispatcher,
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() + dispatcher

    init {
        Cleaner.common.register(ref) {
            coroutineContext.cancel()
        }
    }
}

fun CoroutineScope.launchCoroutineScope(block: suspend CoroutineScopeHelper.() -> Unit): Job {
    return launch {
        coroutineScope {
            asHelper.block()
        }
    }
}

fun <T> CoroutineScope.asyncCoroutineScope(block: suspend CoroutineScopeHelper.() -> T): Deferred<T> {
    return async {
        coroutineScope {
            asHelper.block()
        }
    }
}

fun Any.autoCoroutineScope(dispatcher: CoroutineDispatcher): CoroutineScopeHelper {
    return AutoCoroutineScope(this, dispatcher).asHelper
}

val Any.autoMainCoroutineScope: CoroutineScopeHelper
    get() {
        return autoCoroutineScope(Dispatchers.Main.immediate)
    }
