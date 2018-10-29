package edu.byu.uapi.spi.dictionary

import edu.byu.uapi.spi.input.ListParamReader
import edu.byu.uapi.spi.input.PathParamReader
import edu.byu.uapi.spi.scalars.ScalarType
import kotlin.reflect.KClass

interface TypeDictionary {
    fun <Type : Any> pathDeserializer(type: KClass<Type>): MaybeTypeFailure<PathParamReader<Type>>

    fun <Type: Any> listParamReader(type: KClass<Type>): MaybeTypeFailure<ListParamReader<Type>>

    fun <Type: Any> scalarType(type: KClass<Type>): ScalarType<Type>?
    fun isScalarType(type: KClass<*>): Boolean
}
