package com.example.shared.utils

import android.app.Activity
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import androidx.annotation.IdRes
import androidx.annotation.IntDef
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import com.example.shared.R
import kotlinx.coroutines.CoroutineScope
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
val <T : View> T.autoViewScope: CoroutineScope
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

interface ViewSupport {

    fun requestLayout()

    fun invalidate()

    fun getParent(): ViewParent?

    fun getWidth(): Int

    fun getHeight(): Int

    @Visibility
    fun getVisibility(): Int

    fun setVisibility(@Visibility visibility: Int)

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(
        View.VISIBLE,
        View.INVISIBLE,
        View.GONE,
    )
    annotation class Visibility

    companion object {

        const val VISIBILITY_MASK = View.VISIBLE or View.INVISIBLE or View.GONE

        val ViewSupport.parent: ViewParent?
            get() = getParent()

        val ViewSupport.width: Int
            get() = getWidth()

        val ViewSupport.height: Int
            get() = getHeight()

        @get:Visibility
        var ViewSupport.visibility: Int
            get() = getVisibility()
            set(@Visibility value) = setVisibility(visibility)
    }

    class Adapter<T : View> : ViewSupport {

        var view: T? = null

        override fun requestLayout() {
            view?.requestLayout()
        }

        override fun invalidate() {
            view?.invalidate()
        }

        override fun getParent(): ViewParent? {
            return view?.parent
        }

        override fun getWidth(): Int {
            return view?.width.elseZero
        }

        override fun getHeight(): Int {
            return view?.height.elseZero
        }

        @Visibility
        override fun getVisibility(): Int {
            return view?.visibility.elseValue(VISIBILITY_MASK)
        }

        override fun setVisibility(@Visibility visibility: Int) {
            view?.visibility = visibility
        }
    }
}
