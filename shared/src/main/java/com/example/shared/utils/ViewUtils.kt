package com.example.shared.utils

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
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

val View.coroutineScope: CoroutineScope
    get() = ViewCoroutineScope(this)

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
