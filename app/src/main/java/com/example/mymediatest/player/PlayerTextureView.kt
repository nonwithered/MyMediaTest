package com.example.mymediatest.player

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView
import com.example.shared.utils.LateInitProxy
import com.example.shared.utils.accessibilityClassNameAdapter

class PlayerTextureView(
    context: Context,
    attributeSet: AttributeSet,
) : TextureView(
    context,
    attributeSet,
), BasePlayer.Holder, LateInitProxy.Owner {

    override var player: BasePlayer by LateInitProxy()

    override fun onInit() {
        player.onInit(this)
    }

    override fun getAccessibilityClassName(): CharSequence {
        return accessibilityClassNameAdapter
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        player.onMeasure(widthMeasureSpec, heightMeasureSpec) { width, height ->
            setMeasuredDimension(width, height)
        }
    }
}
