package com.example.mymediatest.player

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceView
import com.example.shared.utils.LateInitProxy
import com.example.shared.utils.accessibilityClassNameAdapter

class PlayerSurfaceView(
    context: Context,
    attributeSet: AttributeSet,
) : SurfaceView(
    context,
    attributeSet,
), BasePlayer.Holder, LateInitProxy.Owner {

    override var player: BasePlayer by LateInitProxy()

    override fun onPropertyInit(proxy: LateInitProxy<*>) {
        when {
            proxy == player -> player.onInit(this)
        }
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
