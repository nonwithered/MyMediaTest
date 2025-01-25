package com.example.shared.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shared.utils.Tuple3
import com.example.shared.utils.getValue
import com.example.shared.utils.inflate
import kotlin.reflect.KClass

abstract class BaseViewAdapter<T : Any, VH: BaseViewHolder<T>> : RecyclerView.Adapter<VH>() {

    protected abstract val factory: List<BaseViewFactory<T, VH>>

    protected abstract val items: List<T>

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val factory = factory[viewType]
        val itemView = parent.inflate(factory.layoutId)
        val viewHolder = factory.createViewHolder(itemView)
        return viewHolder
    }

    final override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.onBind(item, position)
    }

    final override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        holder.onUnBind()
    }

    final override fun getItemViewType(position: Int): Int {
        return factory.indexOfFirst {
            val item = items[position]
            it.checkItemType(item)
        }
    }

    final override fun getItemCount(): Int {
        return items.size
    }

    companion object {

        fun <T : Any, VH: BaseViewHolder<T>> simple(
            vararg tuple: Tuple3<KClass<in T>, KClass<out VH>, Int>,
            block: () -> List<T>,
        ): BaseViewAdapter<T, VH> {
            return object : BaseViewAdapter<T, VH>() {

                override val items by block

                override val factory = tuple.map { BaseViewFactory.simple(it) }
            }
        }
    }
}
