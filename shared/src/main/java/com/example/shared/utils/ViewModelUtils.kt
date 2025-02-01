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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

val <T> LiveData<T>.asFlow: StateFlow<T?>
    get() = LiveDataFlow(this)

val <T> MutableLiveData<T>.asFlow: MutableStateFlow<T?>
    get() = MutableLiveDataFlow(this)

suspend fun <V> LiveData<V>.collect(block: suspend (V?) -> Unit): Nothing {
    coroutineScope {
        val channel = Channel<V?>(Channel.UNLIMITED)
        val job = async {
            val observer = Observer<V?> {
                channel.trySend(it)
            }
            observeForever(observer)
            onDispose {
                removeObserver(observer)
            }
        }
        channel.forEach {
            block(it)
            true
        }
        job.await()
    }
}

open class LiveDataFlow<T>(
    private val liveData: LiveData<T>,
) : StateFlow<T?> {

    override val value: T?
        get() = liveData.value

    override val replayCache: List<T?>
        get() = listOf(value)

    override suspend fun collect(collector: FlowCollector<T?>): Nothing {
        liveData.collect {
            collector.emit(it)
        }
    }
}

open class MutableLiveDataFlow<T>(
    private val mutableLiveData: MutableLiveData<T>,
) : LiveDataFlow<T>(mutableLiveData), MutableStateFlow<T?>, StateFlow<T?> {

    override var value: T?
        get() = super.value
        set(value) {
            mutableLiveData.value = value
        }

    override suspend fun collect(collector: FlowCollector<T?>): Nothing {
        _subscriptionCount.update {
            it + 1
        }
        try {
            super.collect(collector)
        } finally {
            _subscriptionCount.update {
                it - 1
            }
        }
    }

    private val _subscriptionCount = MutableStateFlow(0)

    override val subscriptionCount: StateFlow<Int>
        get() = _subscriptionCount.asConst

    @ExperimentalCoroutinesApi
    override fun resetReplayCache() {
        throw UnsupportedOperationException()
    }

    override fun tryEmit(value: T?): Boolean {
        this.value = value
        return true
    }

    override suspend fun emit(value: T?) {
        this.value = value
    }

    override fun compareAndSet(expect: T?, update: T?): Boolean {
        if (value != expect) {
            return false
        }
        value = update
        return true
    }
}

fun <T> LifecycleOwner.bind(state: Flow<T>, block: suspend (T) -> Unit): () -> Unit {
    return lifecycleScope.bind(state) {
        block(it)
    }::dispose.once
}

fun <T> LifecycleOwner.bind(source: StateFlow<T>, target: MutableStateFlow<in T>): () -> Unit {
    return bind(source) {
        target.value = it
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

fun <V> CoroutineScope.bind(
    state: Flow<V>,
    block: suspend CoroutineScope.(it: V) -> Unit,
): Job {
    return launch {
        state.collect {
            block(it)
        }
    }
}

fun <T : Any, V> CaptureCoroutineScope<T>.bind(
    state: Flow<V>,
    block: suspend CoroutineScope.(it: V, capture: T) -> Unit,
): Job {
    return launch {
        state.collect {
            capture?.let { capture ->
                block(it, capture)
            }
        }
    }
}
