package com.example.mymediatest.player

import android.content.Context
import android.graphics.SurfaceTexture
import android.net.Uri
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import com.example.shared.utils.autoCoroutineScope
import com.example.shared.utils.launchCoroutineScope
import com.example.shared.utils.weak
import kotlinx.coroutines.flow.MutableStateFlow

abstract class BasePlayer(
    protected val context: Context,
) : SurfaceHolder.Callback2, TextureView.SurfaceTextureListener {

    protected val videoSize = MutableStateFlow(0 to 0)

    protected open var surfaceSize = 0 to 0

    protected open var surface: Surface? = null

    protected lateinit var view: View

    val uri = MutableStateFlow(null as Uri?)

    fun onInit(v: SurfaceView) {
        view = v
        onInit()
        v.holder.addCallback(this)
        val weak = weak
        val videoSize = videoSize
        v.autoCoroutineScope.launchCoroutineScope {
            videoSize.launchCollect(weak) { self, it ->
                val (videoWidth, videoHeight) = it
                if (videoWidth != 0 && videoHeight != 0) {
                    (self.view as SurfaceView).holder.setFixedSize(videoWidth, videoHeight)
                    self.view.requestLayout()
                }
            }
        }
    }

    fun onInit(v: TextureView) {
        view = v
        onInit()
        v.surfaceTextureListener = this
        val weak = weak
        val videoSize = videoSize
        view.autoCoroutineScope.launchCoroutineScope {
            videoSize.launchCollect(weak) { self, it ->
                val (videoWidth, videoHeight) = it
                if (videoWidth != 0 && videoHeight != 0) {
                    self.view.requestLayout()
                }
            }
        }
    }

    protected open fun onInit() {
    }

    fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        setMeasuredDimension: (width: Int, height: Int) -> Unit,
    ) {
        val (width, height) = VideoViewHelper.onMeasure(
            measureSpec = widthMeasureSpec to heightMeasureSpec,
            videoSize = videoSize.value,
        )
        setMeasuredDimension(width, height)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        surface = holder.surface
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        surface = null
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        surfaceSize = width to height
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
        surfaceSize = width to height
    }

    override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {
    }

    interface Holder {

        var player: BasePlayer
    }
}
