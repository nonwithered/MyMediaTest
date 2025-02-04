package com.example.mymediatest.play.view

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.example.mymediatest.play.base.BasePlayerHelper
import com.example.shared.utils.LateInitProxy
import com.example.shared.utils.accessibilityClassNameAdapter

class PlayerGLSurfaceView(
    context: Context,
    attributeSet: AttributeSet,
) : GLSurfaceView(
    context,
    attributeSet,
),
    BasePlayerHelper.Holder,
    LateInitProxy.Owner {

    override var player: BasePlayerHelper by LateInitProxy()

    override fun onPropertyInit(proxy: LateInitProxy<*>) {
        when {
            player === proxy.get() -> player.onInit(this)
        }
    }

    override fun getAccessibilityClassName(): CharSequence {
        return accessibilityClassNameAdapter
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val result = player.onMeasure(widthMeasureSpec, heightMeasureSpec) { width, height ->
            setMeasuredDimension(width, height)
        }
        if (!result) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }
}
