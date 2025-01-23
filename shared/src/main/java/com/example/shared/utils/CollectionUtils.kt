package com.example.shared.utils

import java.util.concurrent.ConcurrentHashMap

fun <T : Any> ConcurrentHashSet(): MutableSet<T> {
    return ConcurrentHashMap<T, Unit>().keySet(Unit)
}

fun <K, V> Map<K, V>.toPairs(): Collection<Pair<K, V>> {
    return entries.map { it.key to it.value }
}
