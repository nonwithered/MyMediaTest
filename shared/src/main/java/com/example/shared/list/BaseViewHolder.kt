package com.example.shared.list

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.shared.utils.CloseableGroup
import com.example.shared.utils.asCloseable

abstract class BaseViewHolder<in T>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    protected val context: Context
        get() = itemView.context

    private val closeableGroup = CloseableGroup()

    protected fun defer(block: () -> Unit) {
        closeableGroup += block.asCloseable
    }

    open fun onBind(item: T, position: Int) {
    }

    open fun onUnBind() {
        closeableGroup.close()
    }
}
