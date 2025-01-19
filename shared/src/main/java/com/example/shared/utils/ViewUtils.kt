package com.example.shared.utils

import android.app.Activity
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import com.example.shared.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

fun ViewGroup.inflate(@LayoutRes layoutId: Int, attach: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutId, this, attach)
}

inline fun <reified T : View> Activity.findView(@IdRes viewId: Int): T? {
    return findViewById<View>(viewId) as? T
}

inline fun <reified T : View> View.findView(@IdRes viewId: Int): T? {
    return findViewById<View>(viewId) as? T
}

inline fun <reified T : Any> View.tag(@IdRes key: Int, crossinline block: () -> T): T {
    val tag = getTag(key) ?: block().also {
        setTag(key, it)
    }
    return tag as T
}

@get:MainThread
val View.coroutineScope: CoroutineScope
    get() = tag(R.id.shared_view_coroutine_scope) {
        ViewCoroutineScope(this)
    }

private class ViewCoroutineScope(view: View) : CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.Main.immediate

    init {
        CommonCleaner.register(view) {
            coroutineContext.cancel()
        }
    }
}

val View.isRtl: Boolean
    get() = layoutDirection == View.LAYOUT_DIRECTION_RTL

fun interface OnLayoutChangeListenerAdapter {

    fun onLayoutChange(v: View?, rect: Rect, oldRect: Rect)
}

fun View.addOnLayoutChangeListenerAdapter(listener: OnLayoutChangeListenerAdapter) {
    addOnLayoutChangeListener { v: View?,
                                left: Int,
                                top: Int,
                                right: Int,
                                bottom: Int,
                                oldLeft: Int,
                                oldTop: Int,
                                oldRight: Int,
                                oldBottom: Int ->
        listener.onLayoutChange(
            v = v,
            rect = Rect(
                left,
                top,
                right,
                bottom,
            ),
            oldRect = Rect(
                oldLeft,
                oldTop,
                oldRight,
                oldBottom,
            )
        )
    }
}
