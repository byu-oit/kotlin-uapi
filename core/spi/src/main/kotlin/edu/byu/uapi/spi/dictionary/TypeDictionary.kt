package edu.byu.uapi.spi.dictionary

import edu.byu.uapi.spi.scalars.ScalarType
import kotlin.reflect.KClass

interface TypeDictionary {
    fun <Type: Any> scalarType(type: KClass<Type>): ScalarType<Type>?
    fun isScalarType(type: KClass<*>): Boolean

    fun registerScalarType(scalarType: ScalarType<*>)
    fun unregisterScalarType(scalarType: ScalarType<*>)

    fun addScalarRegistrationListener(listener: ScalarRegistrationListener)
    fun removeScalarRegistrationListener(listener: ScalarRegistrationListener)

    interface ScalarRegistrationListener {
        fun onRegister(type: KClass<*>, scalarType: ScalarType<*>)
        fun onUnregister(type: KClass<*>, scalarType: ScalarType<*>)
    }
}

