package com.example.shared.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes

fun ViewGroup.inflate(@LayoutRes layoutId: Int): View {
    return LayoutInflater.from(context).inflate(layoutId, this, false)
}

inline fun <reified T : View> View.findView(@IdRes viewId: Int): T? {
    return findViewById<View>(viewId) as? T
}
