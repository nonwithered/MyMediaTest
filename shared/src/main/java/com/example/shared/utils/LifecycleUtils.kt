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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

private fun <T> LifecycleOwner.observe(liveData: LiveData<T>, observer: Observer<in T>): () -> Unit {
    liveData.observe(this, observer)
    return {
        liveData.removeObserver(observer)
    }
}

private fun <T> LifecycleOwner.collect(flow: Flow<T>, collector: FlowCollector<T>): () -> Unit {
    val job = lifecycleScope.launch {
        flow.collect(collector)
    }
    return {
        job.cancel()
    }
}

fun <T> LifecycleOwner.bind(state: LiveData<T>, block: (T?) -> Unit): () -> Unit {
    return observe(state, block)
}

fun <T> LifecycleOwner.bind(state: Flow<T>, block: (T) -> Unit): () -> Unit {
    return collect(state, block)
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

fun <T> LifecycleOwner.connect(lhs: MutableLiveData<T>, rhs: MutableLiveData<T>) {
    bind(lhs, rhs)
    bind(rhs, lhs)
}

fun <T> LifecycleOwner.connect(lhs: MutableLiveData<T?>, rhs: MutableStateFlow<T?>) {
    bind(lhs, rhs)
    bind(rhs, lhs)
}

fun <T> LifecycleOwner.connect(lhs: MutableStateFlow<T>, rhs: MutableStateFlow<T>) {
    bind(lhs, rhs)
    bind(rhs, lhs)
}

fun <T> LifecycleOwner.connect(lhs: MutableStateFlow<T?>, rhs: MutableLiveData<T?>) {
    bind(lhs, rhs)
    bind(rhs, lhs)
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
