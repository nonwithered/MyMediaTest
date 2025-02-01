package com.example.mymediatest.play

import android.content.Context
import android.net.Uri
import android.view.Surface
import com.example.mymediatest.play.base.CommonPlayerHelper
import com.example.mymediatest.play.support.AVSupport

abstract class CodecPlayerHelperFactory<T : AVSupport<T>>(
    context: Context,
    uri: Uri,
    surface: Surface,
    listener: CommonPlayerHelper.Listener,
) : CommonPlayerHelper.Controller(
    context = context,
    uri = uri,
    surface = surface,
    listener = listener,
) {

    protected abstract val support: AVSupport<T>

    init {


    }
}
