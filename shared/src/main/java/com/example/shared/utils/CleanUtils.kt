package com.example.shared.utils

import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

val (() -> Unit).asCloseable: AutoCloseable
    get() = AutoCloseable(this)

val AtomicReference<*>.asCloseable: AutoCloseable
    get() = { set(null) }.asCloseable

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

fun CommonCleaner.registerWeak(ref: Any, block: () -> Unit): AutoCloseable {
    val blockRef = AtomicReference(block)
    val blockWeak = WeakReference(block)
    register(ref) {
        blockWeak.get()?.invoke()
    }
    return blockRef.asCloseable
}
