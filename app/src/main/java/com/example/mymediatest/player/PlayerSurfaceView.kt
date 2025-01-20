package com.example.mymediatest.player

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceView
import com.example.shared.utils.accessibilityClassNameAdapter

class PlayerSurfaceView(
    context: Context,
    attributeSet: AttributeSet,
) : SurfaceView(
    context,
    attributeSet,
), BasePlayerHelper.Holder {

    override var helper = super.helper
        set(value) {
            super.helper = value
            field = value
            holder.addCallback(value)
        }
    override var videoSize = 0 to 0
        set(value) {
            field = value
            val (videoWidth, videoHeight) = value
            holder.setFixedSize(videoWidth, videoHeight)
        }

    override fun getAccessibilityClassName(): CharSequence {
        return accessibilityClassNameAdapter
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val (width, height) = VideoViewHelper.onMeasure(
            measureSpec = widthMeasureSpec to heightMeasureSpec,
            videoSize = videoSize,
        )
        setMeasuredDimension(width, height)
    }
}
