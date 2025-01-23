package com.example.shared.utils

import java.util.concurrent.ConcurrentHashMap

fun <T : Any> ConcurrentHashSet(): MutableSet<T> {
    return ConcurrentHashMap<T, Unit>().keySet(Unit)
}
