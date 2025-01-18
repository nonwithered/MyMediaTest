package com.example.shared.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KProperty

operator fun <T> StateFlow<T>.getValue(owner: Any, property: KProperty<*>): T {
    return value
}

operator fun <T> MutableStateFlow<T>.setValue(owner: Any, property: KProperty<*>, v: T) {
    value = v
}

operator fun <T> LiveData<T>.getValue(owner: Any, property: KProperty<*>): T? {
    return value
}

operator fun <T> MutableLiveData<T>.setValue(owner: Any, property: KProperty<*>, v: T?) {
    value = v
}
