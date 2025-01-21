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
        private val tag: String,
        action: Runnable,
    ) : PhantomReference<Any>(ref, queue),
        AutoCloseable {

        val name: String = ref.toString()

        private val action = AtomicReference(action)

        override fun close() {
            val r = action.get()
            if (!action.compareAndSet(r, null)) {
                return
            }
            tag.logD { "clean $name" }
            r.run()
        }
    }

    fun register(ref: Any, block: () -> Unit): AutoCloseable {
        val cleanable = Cleanable(ref, queue, name, block)
        refs += cleanable
        executor.execute(task)
        name.logD { "register ${cleanable.name}" }
        return cleanable
    }

    companion object {

        val common = Cleaner("Common")

        private val defaultThreadFactory = Executors.defaultThreadFactory()
    }
}
