package com.example.mymediatest.player

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView
import com.example.shared.utils.accessibilityClassNameAdapter
import com.example.shared.utils.autoCoroutineScope
import com.example.shared.utils.launchCoroutineScope

class VideoTextureView(
    context: Context,
    attributeSet: AttributeSet,
) : TextureView(
    context,
    attributeSet
), TextureView.SurfaceTextureListener,
    MediaPlayerHelper.MediaPlayerHelperHolder {

    override val helper = MediaPlayerHelper(context)

    init {
        surfaceTextureListener = this
        autoCoroutineScope.launchCoroutineScope {
            helper.uri.launchCollect {
                requestLayout()
                invalidate()
            }
            helper.videoSize.launchCollect {
                val (videoWidth, videoHeight) = it
                if (videoWidth != 0 && videoHeight != 0) {
                    requestLayout()
                }
            }
        }
    }

    override fun getAccessibilityClassName(): CharSequence {
        return accessibilityClassNameAdapter
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val (width, height) = VideoViewHelper.onMeasure(
            measureSpec = widthMeasureSpec to heightMeasureSpec,
            videoSize = helper.videoSize.value,
        )
        setMeasuredDimension(width, height)
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        helper.surface = Surface(surface)
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        helper.surface = null
        return true
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }
}
