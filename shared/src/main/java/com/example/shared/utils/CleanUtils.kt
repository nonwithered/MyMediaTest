package com.example.shared.utils

import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

val (() -> Unit).asCloseable: AutoCloseable
    get() = AutoCloseable(this)

val AtomicReference<*>.asCloseable: AutoCloseable
    get() = { set(null) }.asCloseable

class Cleaner(
    name: String
) : AutoCloseable {

    private val name = "Cleaner-$name"

    private val executor = Executors.newSingleThreadExecutor { r ->
        defaultThreadFactory.newThread(r).also { t ->
            t.name = name
        }
    }

    private val queue = ReferenceQueue<Any>()

    private val task: Runnable = Runnable {
        loopOnce()
    }

    private val refs = ConcurrentLinkedQueue<Any?>()

    override fun close() {
        if (this === common) {
            return
        }
        executor.shutdownNow()
    }

    private fun loopOnce() {
        runCatching {
            val cleanable = queue.remove() as? Cleanable
            if (cleanable !== null) {
                cleanable.close()
                refs -= cleanable
            }
        }.onFailure { e ->
            name.logE(e) { "onFailure" }
        }
    }

    private class Cleanable(
        ref: Any,
        queue: ReferenceQueue<Any>,
        private val action: () -> Unit,
    ) : PhantomReference<Any>(ref, queue),
        AutoCloseable {

        override fun close() {
            action()
        }
    }

    fun register(ref: Any, block: () -> Unit): AutoCloseable {
        val tag = name
        val name = ref.toString()
        val cleanable = Cleanable(ref, queue, {
            block()
            tag.logD { "clear $name" }
        }.once)
        refs += cleanable
        executor.execute(task)
        tag.logD { "register $name" }
        return cleanable
    }

    companion object {

        val common = Cleaner("Common")

        private val defaultThreadFactory = Executors.defaultThreadFactory()
    }
}

fun Cleaner.registerWeak(ref: Any, block: () -> Unit): AutoCloseable {
    val blockRef = block.once
    val blockWeak = blockRef.weak
    register(ref) {
        blockWeak.get()?.invoke()
    }
    return blockRef.asCloseable
}
