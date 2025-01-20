package com.example.mymediatest.player

import android.content.Context
import android.graphics.SurfaceTexture
import android.net.Uri
import android.view.Surface
import android.view.SurfaceHolder
import android.view.TextureView
import android.view.View
import com.example.shared.utils.Vec2
import com.example.shared.utils.app
import com.example.shared.utils.autoCoroutineScope
import com.example.shared.utils.launchCoroutineScope
import com.example.shared.utils.logD
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

open class BasePlayerHelper(
    protected val context: Context,
) : SurfaceHolder.Callback2, TextureView.SurfaceTextureListener {

    protected val TAG = javaClass.simpleName

    protected val videoSize = MutableStateFlow(0 to 0)

    private val _surfaceSize = MutableStateFlow(0 to 0)
    protected val surfaceSize = _surfaceSize as StateFlow<Vec2<Int>>

    val uri = MutableStateFlow(null as Uri?)
    protected open var surface: Surface? = null

    protected open fun onInit(view: View) = Unit

    override fun surfaceCreated(holder: SurfaceHolder) {
        surface = holder.surface
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        surface = null
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        _surfaceSize.value = width to height
    }

    override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
    }

    override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
        surface = Surface(texture)
    }

    override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
        surface = null
        return true
    }

    override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
        _surfaceSize.value = width to height
    }

    override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {
    }

    private companion object {

        private val empty by lazy {
            BasePlayerHelper(app)
        }
    }

    interface Holder {

        val TAG
            get() = javaClass.simpleName

        var videoSize: Vec2<Int>

        var helper: BasePlayerHelper
            get() = empty
            set(value) {
                if (helper !== empty) {
                    throw IllegalStateException("$helper")
                }
                TAG.logD { "helper set $value" }
                value.onInit(this as View)
                autoCoroutineScope.launchCoroutineScope {
                    value.videoSize.launchCollect {
                        val (videoWidth, videoHeight) = it
                        if (videoWidth != 0 && videoHeight != 0) {
                            videoSize = it
                            requestLayout()
                        }
                    }
                }
            }
    }
}
