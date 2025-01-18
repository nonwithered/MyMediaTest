package com.example.shared.list

import android.view.View
import androidx.annotation.LayoutRes

interface BaseViewFactory<in T, out VH: BaseViewHolder<T>> {

    val layoutId: Int
        @LayoutRes
        get

    fun checkItemType(item: T): Boolean

    fun createViewHolder(itemView: View): VH
}
