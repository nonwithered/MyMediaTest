package com.example.mymediatest.play.base

import android.content.Context
import android.view.View
import androidx.annotation.CallSuper
import com.example.shared.utils.ViewSupport
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

abstract class BasePlayerHelper private constructor(
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

    open fun asRenderer(): GLRenderer? = null

    interface Holder : ViewSupport {

        var player: BasePlayerHelper
    }

    interface GLRenderer {

        fun onSurfaceCreated(gl: GL10?, config: EGLConfig?)

        fun onSurfaceChanged(gl: GL10?, width: Int, height: Int)

        fun onDrawFrame(gl: GL10?)
    }
}
