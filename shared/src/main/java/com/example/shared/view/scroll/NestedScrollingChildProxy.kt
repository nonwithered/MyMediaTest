package com.example.shared.view.scroll

import android.view.View
import androidx.core.view.NestedScrollingChildHelper

class NestedScrollingChildProxy(view: View) : BaseNestedScrollingChild {

    override val childHelper = NestedScrollingChildHelper(view)

    init {
        isNestedScrollingEnabled = true
    }

    fun delegate(scrollAxes: Int, delegate: NestedScrollingParentProxy.Delegate): NestedScrollingParentProxy.Delegate {
        return Delegate(scrollAxes, delegate, this)
    }

    private class Delegate(
        override val scrollAxes: Int,
        private val delegate: NestedScrollingParentProxy.Delegate,
        proxy: NestedScrollingChildProxy,
    ) : BaseNestedScrollingChild by proxy, NestedScrollingParentProxy.Delegate {

        override fun onPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
            if (!startNestedScroll(scrollAxes, type)) {
                return
            }
            val parentConsumed = IntArray(2)
            dispatchNestedPreScroll(dx, dy, parentConsumed, null, type)
            delegate.onPreScroll(target, dx, dy, consumed, type)
            consumed[0] += parentConsumed[0]
            consumed[1] += parentConsumed[1]
        }

        override fun onPostScroll(
            target: View,
            dxConsumed: Int,
            dyConsumed: Int,
            dxUnconsumed: Int,
            dyUnconsumed: Int,
            type: Int,
            consumed: IntArray,
        ) {
            if (!startNestedScroll(scrollAxes, type)) {
                return
            }
            delegate.onPostScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed)
            val parentConsumed = IntArray(2)
            dispatchNestedScroll(
                dxConsumed + consumed[0],
                dyConsumed + consumed[1],
                dxUnconsumed - consumed[0],
                dyUnconsumed - consumed[1],
                null,
                type,
                parentConsumed,
            )
            consumed[0] += parentConsumed[0]
            consumed[1] += parentConsumed[1]
        }

        override fun onPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
            return try {
                dispatchNestedPreFling(velocityY, velocityY) || delegate.onPreFling(target, velocityX, velocityY)
            } finally {
                stopNestedScroll()
            }
        }

        override fun onPostFling(
            target: View,
            velocityX: Float,
            velocityY: Float,
            consumed: Boolean,
        ): Boolean {
            return try {
                var handled = delegate.onPostFling(target, velocityX, velocityY, consumed)
                handled = dispatchNestedFling(velocityY, velocityY, consumed || handled) || handled
                !consumed && handled
            } finally {
                stopNestedScroll()
            }
        }
    }
}
