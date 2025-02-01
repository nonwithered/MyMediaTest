package com.example.mymediatest.test.base

import android.os.Bundle
import com.example.mymediatest.select.CasePageActivity.PageModel
import com.example.mymediatest.select.CaseParamsFragment
import com.example.shared.bean.BundleProperties
import com.example.shared.bean.PropertyProxy
import com.example.shared.utils.TAG
import com.example.shared.utils.getTypeArguments
import com.example.shared.utils.invokeStaticMethodSafe
import com.example.shared.utils.logD
import com.example.shared.utils.newInstanceSafe
import com.example.shared.utils.viewModel
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

abstract class PlayerParamsFragment<P: PlayerParamsFragment.BaseParams, T : Any> : PlayerFragment<T>() {

    protected val params: P by lazy {
        val p = javaClass
            .getTypeArguments()
            .getOrThrow()
            .first()
            .newInstanceSafe(
                Bundle::class.java to pageData.paramsExtras!!,
            ).getOrThrow()
        TAG.logD { "params $p" }
        @Suppress("UNCHECKED_CAST")
        p as P
    }

    private val pageData by lazy {
        requireActivity().viewModel<PageModel>().pageData!!
    }

    open class BaseParams(
        bundle: Bundle = Bundle(),
    ) : BundleProperties(bundle) {

        protected data class EnumAdapter<T : Enum<T>>(
            private val type: KClass<T>,
            private val proxy: PropertyProxy<Any, String>,
        ) {

            operator fun getValue(owner: BaseParams, property: KProperty<*>): T {
                return type.invokeStaticMethodSafe<T, T>(
                    "valueOf",
                    String::class to proxy.getValue(owner)!!,
                ).getOrThrow()
            }

            operator fun setValue(owner: BaseParams, property: KProperty<*>, v: T) {
                proxy.setValue(owner, v.name)
            }

        }

        protected inline fun <reified T : Enum<T>> KClass<T>.adapt(): EnumAdapter<T> {
            return EnumAdapter(
                T::class,
                java.simpleName.property(),
            )
        }
    }

    internal open class BaseParamsBuilder<T : BaseParams>(params: T) : CaseParamsFragment<T>(params) {

        protected inline fun <reified T : Enum<T>> KMutableProperty0<T>.adapt() {
            option(*T::class.invokeStaticMethodSafe<T, Array<T>>("values").getOrThrow())
        }
    }
}
