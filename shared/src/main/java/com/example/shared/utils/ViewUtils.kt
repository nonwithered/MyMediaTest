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
import kotlinx.coroutines.Dispatchers

fun ViewGroup.inflate(@LayoutRes layoutId: Int, attach: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutId, this, attach)
}

inline fun <reified T : Any> Activity.findView(@IdRes viewId: Int): T? {
    return findViewById<View>(viewId) as? T
}

inline fun <reified T : Any> View.findView(@IdRes viewId: Int): T? {
    return findViewById<View>(viewId) as? T
}

inline fun <reified T : Any> View.tag(@IdRes key: Int, crossinline block: () -> T): T {
    val tag = getTag(key) ?: block().also {
        setTag(key, it)
    }
    return tag as T
}

@get:MainThread
val <T : View> T.autoViewScope: AutoCoroutineScope
    get() = tag(R.id.shared_view_coroutine_scope) {
        autoScope(Dispatchers.Main.immediate)
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

val View.accessibilityClassNameAdapter: CharSequence
    get() = javaClass.name
