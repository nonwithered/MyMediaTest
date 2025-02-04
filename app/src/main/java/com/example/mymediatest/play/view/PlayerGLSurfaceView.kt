package com.example.mymediatest.play.view

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.example.mymediatest.play.base.BasePlayerHelper
import com.example.shared.utils.LateInitProxy
import com.example.shared.utils.accessibilityClassNameAdapter
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PlayerGLSurfaceView(
    context: Context,
    attributeSet: AttributeSet,
) : GLSurfaceView(
    context,
    attributeSet,
),
    GLSurfaceView.Renderer,
    BasePlayerHelper.Holder,
    LateInitProxy.Owner {

    init {
        setRenderer(this)
    }

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

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        player.asRenderer()?.onSurfaceCreated(gl, config)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        player.asRenderer()?.onSurfaceChanged(gl, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        player.asRenderer()?.onDrawFrame(gl)
    }
}
