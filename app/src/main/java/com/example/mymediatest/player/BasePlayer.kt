package com.example.mymediatest.player

import android.content.Context
import android.view.View
import androidx.annotation.CallSuper
import com.example.shared.utils.ViewSupport

abstract class BasePlayer private constructor(
    protected val context: Context,
    protected val viewAdapter: ViewSupport.Adapter<View>,
) : ViewSupport by viewAdapter {

    protected constructor(
        context: Context,
    ) : this(
        context,
        ViewSupport.Adapter(),
    )

    @CallSuper
    open fun onInit(view: View) {
        viewAdapter.view = view
    }

    open fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        setMeasuredDimension: (width: Int, height: Int) -> Unit,
    ): Boolean {
        return false
    }

    interface Holder : ViewSupport {

        var player: BasePlayer
    }
}
