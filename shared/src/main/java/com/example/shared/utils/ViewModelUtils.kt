package com.example.shared.utils

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

class MutableLiveDataStable<T> : MutableLiveData<T> {

    constructor() : super()
    constructor(value: T) : super(value)

    override fun setValue(value: T) {
        if (value == getValue()) {
            return
        }
        super.setValue(value)
    }
}

@MainThread
fun <T : ViewModel> ViewModelStoreOwner.viewModel(type: Class<T>): T {
    return ViewModelProvider(this)[type]
}

@MainThread
fun <T : ViewModel> ViewModelStoreOwner.viewModel(type: KClass<T>): T {
    return viewModel(type.java)
}

@MainThread
inline fun <reified T : ViewModel> ViewModelStoreOwner.viewModel(): T {
    return viewModel(T::class)
}

/**
 * @see kotlinx.coroutines.flow.asStateFlow
 * @see kotlinx.coroutines.flow.asSharedFlow
 */
fun <T> MutableLiveData<T>.asLiveData(): LiveData<T> = this

val <T> MutableLiveData<T>.asConst: LiveData<T>
    get() = asLiveData()
val <T> MutableStateFlow<T>.asConst: StateFlow<T>
    get() = asStateFlow()
val <T> MutableSharedFlow<T>.asConst: SharedFlow<T>
    get() = asSharedFlow()

suspend fun <V> LiveData<V>.collect(block: suspend (V?) -> Unit) {
    val channel = Channel<V?>(Channel.UNLIMITED)
    val observer = Observer<V?> {
        channel.trySend(it)
    }
    observeForever(observer)
    try {
        channel.forEach {
            block(it)
        }
    } finally {
        removeObserver(observer)
    }
}

fun <T> LifecycleOwner.bind(state: LiveData<T>, block: suspend (T?) -> Unit): () -> Unit {
    return lifecycleScope.launch {
        state.collect {
            block(it)
        }
    }::dispose.once
}

fun <T> LifecycleOwner.bind(state: Flow<T>, block: suspend (T) -> Unit): () -> Unit {
    return lifecycleScope.launch {
        state.collect {
            block(it)
        }
    }::dispose.once
}

fun <T> LifecycleOwner.bind(source: LiveData<out T>, target: MutableLiveData<in T>): () -> Unit {
    return bind(source) {
        target.value = it
    }
}

fun <T> LifecycleOwner.bind(source: LiveData<out T>, target: MutableStateFlow<in T?>): () -> Unit {
    return bind(source) {
        target.value = it
    }
}

fun <T> LifecycleOwner.bind(source: StateFlow<T>, target: MutableStateFlow<in T>): () -> Unit {
    return bind(source) {
        target.value = it
    }
}

fun <T> LifecycleOwner.bind(source: StateFlow<T>, target: MutableLiveData<in T>): () -> Unit {
    return bind(source) {
        target.value = it
    }
}

fun <T> LifecycleOwner.connect(lhs: MutableLiveData<T>, rhs: MutableLiveData<T>): () -> Unit {
    val first = bind(lhs, rhs)
    val second = bind(rhs, lhs)
    return {
        first()
        second()
    }
}

fun <T> LifecycleOwner.connect(lhs: MutableLiveData<T?>, rhs: MutableStateFlow<T?>): () -> Unit {
    val first = bind(lhs, rhs)
    val second = bind(rhs, lhs)
    return {
        first()
        second()
    }
}

fun <T> LifecycleOwner.connect(lhs: MutableStateFlow<T>, rhs: MutableStateFlow<T>): () -> Unit {
    val first = bind(lhs, rhs)
    val second = bind(rhs, lhs)
    return {
        first()
        second()
    }
}

fun <T> LifecycleOwner.connect(lhs: MutableStateFlow<T?>, rhs: MutableLiveData<T?>): () -> Unit {
    val first = bind(lhs, rhs)
    val second = bind(rhs, lhs)
    return {
        first()
        second()
    }
}

fun <V> CoroutineScope.launchBind(
    state: Flow<V>,
    block: suspend CoroutineScope.(it: V) -> Unit,
): Job {
    return launch {
        state.collect {
            block(it)
        }
    }
}

fun <V> CoroutineScope.launchBind(
    state: LiveData<V>,
    block: suspend CoroutineScope.(it: V?) -> Unit,
): Job {
    return launch {
        state.collect {
            block(it)
        }
    }
}

fun <T : Any, V> CoroutineScope.launchBind(
    state: Flow<V>,
    ref: T,
    block: suspend CoroutineScope.(it: V, owner: T) -> Unit,
): Job {
    val weak by ref.weak
    return launchBind(state) {
        weak?.let {  owner ->
            block(it, owner)
        }
    }
}

fun <T : Any, V> CoroutineScope.launchBind(
    state: LiveData<V>,
    ref: T,
    block: suspend CoroutineScope.(it: V?, owner: T) -> Unit,
): Job {
    val weak by ref.weak
    return launchBind(state) {
        weak?.let {  owner ->
            block(it, owner)
        }
    }
}
