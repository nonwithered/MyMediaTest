package com.example.mymediatest.player

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView
import com.example.shared.utils.accessibilityClassNameAdapter

class PlayerTextureView(
    context: Context,
    attributeSet: AttributeSet,
) : TextureView(
    context,
    attributeSet,
), BasePlayerHelper.Holder {

    override var player = super.player
        set(value) {
            super.player = value
            field = value
            surfaceTextureListener = value
        }
    override var videoSize = 0 to 0

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
