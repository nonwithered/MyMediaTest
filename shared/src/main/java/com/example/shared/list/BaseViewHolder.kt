package com.example.shared.list

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder<in T>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    protected val context: Context
        get() = itemView.context

    open fun onBind(item: T, position: Int) {
    }

    open fun onUnBind() {
    }
}
