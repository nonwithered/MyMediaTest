package com.example.mymediatest.player

import android.content.Context
import android.net.Uri
import android.view.View
import com.example.shared.utils.ViewSupport
import kotlinx.coroutines.flow.MutableStateFlow

abstract class BasePlayer(
    protected val context: Context,
) {

    val uri = MutableStateFlow(null as Uri?)

    open fun onInit(view: View) {
    }

    open fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int,
        setMeasuredDimension: (width: Int, height: Int) -> Unit,
    ): Boolean {
        return false
    }

    interface Holder : ViewSupport {

        var player: BasePlayer
    }
}
