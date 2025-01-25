package com.example.shared.bean

import com.example.shared.utils.equalsCombined
import com.example.shared.utils.hashCodeCombined
import com.example.shared.utils.plusAssign
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

interface BasePropertyOwner<V : Any> {

    fun getPropertyValue(type: KClass<*>, k: String): V?

    fun setPropertyValue(type: KClass<*>, k: String, v: V?)
}

data class PropertyProxy<V : Any, T : V>(
    val k: String,
    val type: KClass<*>,
) {

    fun getValue(owner: BasePropertyOwner<V>): T? {
        val v = owner.getPropertyValue(type, k)
        if (v === null) {
            return null
        }
        if (!type.isInstance(v)) {
            return null
        }
        @Suppress("UNCHECKED_CAST")
        return v as? T
    }

    fun setValue(owner: BasePropertyOwner<V>, v: T?) {
        if (v === null) {
            owner.setPropertyValue(type, k, null)
            return
        }
        if (!type.isInstance(v)) {
            return
        }
        owner.setPropertyValue(type, k, v)
    }

    operator fun getValue(owner: BasePropertyOwner<V>, property: KProperty<*>): T? {
        return getValue(owner)
    }

    operator fun setValue(owner: BasePropertyOwner<V>, property: KProperty<*>, v: T?) {
        setValue(owner, v)
    }
}

inline fun <V : Any, reified T : V> createPropertyProxy(owner: BasePropertyOwner<V>, k: String, v: T?): PropertyProxy<V, T> {
    val type = T::class
    if (v !== null) {
        owner.setPropertyValue(type, k, v)
    }
    return PropertyProxy(k, type)
}

abstract class BaseProperties<V : Any> : BasePropertyOwner<V> {

    protected val label: String
        get() = javaClass.simpleName

    protected val proxyList: MutableList<PropertyProxy<V, out V>> = mutableListOf()

    private val propertyList: List<V?>
        get() = proxyList.map {
            it.getValue(this)
        }

    protected inline fun <reified T : V> String.property(v: T? = null): PropertyProxy<V, T> {
        val proxy = createPropertyProxy(this@BaseProperties, this, v)
        proxyList += proxy
        return proxy
    }

    override fun hashCode(): Int {
        return proxyList.hashCodeCombined
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is BaseProperties<*>) {
            return false
        }
        return propertyList equalsCombined other.propertyList
    }

    override fun toString(): String {
        return buildString {
            val builder = this
            builder += label
            builder += "["
            proxyList.dump(builder)
            builder += "]"
        }
    }

    private fun PropertyProxy<V, out V>.dump(builder: Appendable) {
        builder += k
        builder += "="
        builder += getValue(this@BaseProperties).toString()
    }

    private fun Collection<PropertyProxy<V, out V>>.dump(builder: Appendable) {
        forEach {
            it.dump(builder)
            builder += ","
        }
    }
}
