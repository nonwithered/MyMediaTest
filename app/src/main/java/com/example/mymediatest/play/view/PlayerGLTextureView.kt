package com.example.mymediatest.play.view

import android.content.Context
import android.util.AttributeSet
import com.example.mymediatest.play.base.BasePlayerHelper
import com.example.shared.utils.LateInitProxy
import com.example.shared.utils.accessibilityClassNameAdapter
import com.example.shared.view.gl.GLTextureView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PlayerGLTextureView(
    context: Context,
    attributeSet: AttributeSet,
) : GLTextureView(
    context,
    attributeSet,
),
    GLTextureView.Renderer,
    BasePlayerHelper.Holder,
    LateInitProxy.Owner {

    init {
        renderer = this
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
