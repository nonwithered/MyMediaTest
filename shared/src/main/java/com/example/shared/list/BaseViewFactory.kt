package com.example.shared.list

import android.view.View
import androidx.annotation.LayoutRes

interface BaseViewFactory<in T, out VH: BaseViewHolder<T>> {

    @get:LayoutRes
    val layoutId: Int

    fun checkItemType(item: T): Boolean

    fun createViewHolder(itemView: View): VH
}
