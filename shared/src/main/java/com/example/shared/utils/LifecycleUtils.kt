package com.example.shared.utils

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

private fun <T> LifecycleOwner.observe(liveData: LiveData<T>, observer: Observer<in T>): () -> Unit {
    liveData.observe(this, observer)
    return {
        liveData.removeObserver(observer)
    }
}

fun <T> LifecycleOwner.bind(state: LiveData<T>, block: (T?) -> Unit): () -> Unit {
    return observe(state, block)
}

private fun <T> LifecycleOwner.collect(flow: Flow<T>, collector: FlowCollector<T>): () -> Unit {
    val job = lifecycleScope.launch {
        flow.collect(collector)
    }
    return {
        job.cancel()
    }
}

fun <T> LifecycleOwner.bind(state: Flow<T>, block: (T) -> Unit): () -> Unit {
    return collect(state, block)
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
