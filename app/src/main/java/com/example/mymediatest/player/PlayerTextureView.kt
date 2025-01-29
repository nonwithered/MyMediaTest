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
),
    BasePlayerHelper.Holder,
    LateInitProxy.Owner {

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
}
