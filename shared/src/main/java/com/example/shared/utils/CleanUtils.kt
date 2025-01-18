package com.example.shared.utils

import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue
import java.util.concurrent.Executors

object CommonCleaner : AutoCloseable {

    private val executor = Executors.newSingleThreadExecutor()

    private val queue = ReferenceQueue<Any>()

    override fun close() {
        executor.shutdownNow()
    }

    init {
        executor.execute(::loop)
    }

    private fun loop() {
        while (true) {
            loopOnce()
        }
    }

    private fun loopOnce() {
        (queue.remove() as Cleanable<*>).run()
    }

    private class Cleanable<T : Any>(
        ref: T,
        queue: ReferenceQueue<in T>,
        action: Runnable,
    ) : PhantomReference<T>(ref, queue),
        Runnable by action

    fun register(ref: Any, block: () -> Unit) {
        Cleanable(ref, queue, block)
    }
}

fun register(ref: Any, block: () -> Unit) {
    CommonCleaner.register(ref, block)
}
