package edu.byu.uapi.spi.dictionary

import edu.byu.uapi.spi.scalars.ScalarType
import kotlin.reflect.KClass

interface TypeDictionary {
    fun <Type: Any> scalarType(type: KClass<Type>): ScalarType<Type>?
    fun isScalarType(type: KClass<*>): Boolean
}
