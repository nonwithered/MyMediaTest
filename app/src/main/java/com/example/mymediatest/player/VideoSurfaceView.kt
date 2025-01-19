package com.example.mymediatest.player

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.shared.utils.accessibilityClassNameAdapter
import com.example.shared.utils.autoCoroutineScope
import com.example.shared.utils.launchCoroutineScope

class VideoSurfaceView(
    context: Context,
    attributeSet: AttributeSet,
) : SurfaceView(
    context,
    attributeSet
), SurfaceHolder.Callback2,
    MediaPlayerHelper.MediaPlayerHelperHolder {

    override val helper = MediaPlayerHelper(context)

    init {
        holder.addCallback(this)
        autoCoroutineScope.launchCoroutineScope {
            helper.uri.launchCollect {
                requestLayout()
                invalidate()
            }
            helper.videoSize.launchCollect {
                val (videoWidth, videoHeight) = it
                if (videoWidth != 0 && videoHeight != 0) {
                    holder.setFixedSize(videoWidth, videoHeight)
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

    override fun surfaceCreated(holder: SurfaceHolder) {
        helper.surface = holder.surface
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        helper.surface = null
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        helper.surfaceSize.value = width to height

    }

    override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
    }
}
