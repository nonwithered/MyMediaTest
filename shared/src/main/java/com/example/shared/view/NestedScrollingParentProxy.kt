package com.example.shared.view

import android.view.View
import android.view.ViewGroup
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat

class NestedScrollingParentProxy(
    view: ViewGroup,
    private val delegate: Delegate,
) : BaseNestedScrollingParent {

    override val parentHelper = NestedScrollingParentHelper(view)

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return (axes and delegate.scrollAxes) != 0
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        delegate.onPreScroll(target, dx, dy, consumed, type)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray,
    ) {
        delegate.onPostScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed)
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        return delegate.onPreFling(target, velocityX, velocityY)
    }

    override fun onNestedFling(
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean,
    ): Boolean {
        return delegate.onPostFling(target, velocityX, velocityY, consumed)
    }

    interface Delegate {

        val scrollAxes: Int
            get() = ViewCompat.SCROLL_AXIS_NONE

        fun onPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        }

        fun onPostScroll(
            target: View,
            dxConsumed: Int,
            dyConsumed: Int,
            dxUnconsumed: Int,
            dyUnconsumed: Int,
            type: Int,
            consumed: IntArray,
        ) {
        }

        fun onPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
            return false
        }

        fun onPostFling(
            target: View,
            velocityX: Float,
            velocityY: Float,
            consumed: Boolean,
        ): Boolean {
            return false
        }
    }
}
