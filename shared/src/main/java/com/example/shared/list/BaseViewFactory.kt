package com.example.shared.list

import android.view.View
import androidx.annotation.LayoutRes
import com.example.shared.utils.Tuple3
import com.example.shared.utils.cross
import com.example.shared.utils.firstOrNull
import com.example.shared.utils.newInstanceDefaultSafe
import com.example.shared.utils.newInstanceSafe
import kotlin.reflect.KClass

interface BaseViewFactory<in T, out VH: BaseViewHolder<*>> {

    @get:LayoutRes
    val layoutId: Int

    fun checkItemType(item: T): Boolean

    fun createViewHolder(itemView: View): VH

    companion object {

        fun <T : Any, VH : BaseViewHolder<T>> simple(
            tuple: Tuple3<KClass<in T>, KClass<out VH>, Int>,
        ): BaseViewFactory<T, VH> {
            val (itemType, vhType, id) = tuple
            val create = { v: View ->
                firstOrNull(
                    { vhType.java.newInstanceSafe(v.javaClass to v).getOrNull() },
                    { vhType.newInstanceSafe(View::class to v).getOrNull() },
                    { vhType.newInstanceDefaultSafe().getOrNull() },
                )!!
            }
            return object : BaseViewFactory<Any?, VH> {

                override val layoutId: Int
                    get() = id

                override fun createViewHolder(itemView: View): VH {
                    return create(itemView)
                }

                override fun checkItemType(item: Any?): Boolean {
                    return itemType.isInstance(item)
                }
            }
        }

        inline fun <reified T : Any, reified VH : BaseViewHolder<T>> simple(
            @LayoutRes id: Int,
        ): BaseViewFactory<T, VH> {
            return simple(
                T::class to VH::class cross id,
            )
        }
    }
}