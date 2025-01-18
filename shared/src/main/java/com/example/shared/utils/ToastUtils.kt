package com.example.shared.utils

import android.widget.Toast
import java.lang.ref.Reference
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference

private val currentToast = AtomicReference<Reference<Toast>>()

fun makeToast(text: String): () -> Unit {
    val toast = Toast.makeText(app, text, Toast.LENGTH_SHORT)
    currentToast.get()?.get()?.cancel()
    toast.show()
    val ref = WeakReference(toast)
    currentToast.set(ref)
    return {
        toast.cancel()
        currentToast.compareAndSet(ref, null)
    }
}
