package com.example.shared.utils

import java.lang.ref.PhantomReference
import java.lang.ref.ReferenceQueue
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
            }
        }.onFailure { e ->
            name.logE(e) { "onFailure" }
        }
    }

    private class Cleanable(
        ref: Any,
        queue: ReferenceQueue<Any>,
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
            name.logD { "clean $name" }
            r.run()
        }
    }

    fun register(ref: Any, block: () -> Unit): AutoCloseable {
        val cleanable = Cleanable(ref, queue, block)
        executor.execute(task)
        name.logD { "register ${cleanable.name}" }
        return cleanable
    }

    companion object {

        val common = Cleaner("Common")

        private val defaultThreadFactory = Executors.defaultThreadFactory()
    }
}
