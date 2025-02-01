package com.example.shared.utils

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.coroutineScope

suspend fun <E> ReceiveChannel<E>.forEach(block: suspend (E) -> Boolean) {
    coroutineScope {
        val iterator = iterator()
        while (iterator.hasNext()) {
            if (!block(iterator.next())) {
                cancel()
            }
        }
    }
}
