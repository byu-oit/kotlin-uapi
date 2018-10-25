package edu.byu.uapi.spi.dictionary

import edu.byu.uapi.spi.input.CollectionParamsProvider
import edu.byu.uapi.spi.input.PathParamDeserializer
import edu.byu.uapi.spi.input.QueryParamReader
import edu.byu.uapi.spi.scalars.ScalarType
import kotlin.reflect.KClass

interface TypeDictionary {
    fun <Type : Any> pathDeserializer(type: KClass<Type>): MaybeTypeFailure<PathParamDeserializer<Type>>
    fun <Type : Any> queryDeserializer(type: KClass<Type>): MaybeTypeFailure<QueryParamReader<Type>>

    fun <Type: Any> collectionParamsProvider(type: KClass<Type>): MaybeTypeFailure<CollectionParamsProvider<Type>>

    fun <Type: Any> scalarType(type: KClass<Type>): ScalarType<Type>?
    fun isScalarType(type: KClass<*>): Boolean
}
