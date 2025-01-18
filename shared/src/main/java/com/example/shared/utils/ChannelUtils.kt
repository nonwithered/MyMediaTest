package com.example.shared.utils

import kotlinx.coroutines.channels.ReceiveChannel

suspend fun <E> ReceiveChannel<E>.forEach(block: suspend (E) -> Unit) {
    val iterator = iterator()
    while (iterator.hasNext()) {
        block(iterator.next())
    }
}
