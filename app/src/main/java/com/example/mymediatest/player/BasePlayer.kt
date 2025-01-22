package com.example.mymediatest.player

import android.content.Context
import android.graphics.SurfaceTexture
import android.net.Uri
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import com.example.shared.utils.ViewSupport
import com.example.shared.utils.autoViewScope
import com.example.shared.utils.launchBind
import kotlinx.coroutines.flow.MutableStateFlow

abstract class BasePlayer(
    protected val context: Context,
    private val viewAdapter: ViewSupport.Adapter<View> = ViewSupport.Adapter()
) : ViewSupport by viewAdapter,
    SurfaceHolder.Callback2,
    TextureView.SurfaceTextureListener {

    protected val videoSize = MutableStateFlow(0 to 0)

    protected open var surfaceSize = 0 to 0

    protected open var surface: Surface? = null

    val uri = MutableStateFlow(null as Uri?)

    fun init(view: SurfaceView) {
        onInit(view)
        view.holder.addCallback(this)
        view.autoViewScope.launchBind(videoSize, this) { it, owner ->
            val (videoWidth, videoHeight) = it
            if (videoWidth != 0 && videoHeight != 0) {
                (owner.viewAdapter.view as SurfaceView).holder.setFixedSize(videoWidth, videoHeight)
                owner.requestLayout()
            }
        }
    }

    fun init(view: TextureView) {
        onInit(view)
        view.surfaceTextureListener = this
        view.autoViewScope.launchBind(videoSize, this) { it, owner ->
            val (videoWidth, videoHeight) = it
            if (videoWidth != 0 && videoHeight != 0) {
                owner.requestLayout()
            }
        }
    }

    protected open fun onInit(view: View) {
        viewAdapter.view = view
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

    interface Holder : ViewSupport {

        var player: BasePlayer
    }
}
