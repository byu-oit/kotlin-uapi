package edu.byu.uapidsl.dsl

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <Type> setOnce(): ReadWriteProperty<Any, Type> {
    return SetOnceDelegate()
}

internal class SetOnceDelegate<PropType> : ReadWriteProperty<Any, PropType> {

    private var value: PropType? = null
    private var initialized = false

    val isInitialized: Boolean
        get() = this.initialized

    override fun getValue(thisRef: Any, property: KProperty<*>): PropType {
        if (!initialized && !property.returnType.isMarkedNullable) {
            throw UninitializedPropertyAccessException("non-nullable setOnce property ${property.name} has not been initialized")
        }
        @Suppress("UNCHECKED_CAST")
        return value as PropType
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: PropType) {
        if (initialized) {
            throw SetOnceException(property.name)
        } else {
            this.value = value
            initialized = true
        }
    }
}

class SetOnceException(prop: String) : Exception(
    "You can only set the property $prop once"
)

