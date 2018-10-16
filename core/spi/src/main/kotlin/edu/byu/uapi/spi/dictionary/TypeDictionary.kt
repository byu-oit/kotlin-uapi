package edu.byu.uapi.spi.dictionary

import edu.byu.uapi.spi.functional.SuccessOrFailure
import edu.byu.uapi.spi.input.PathParamDeserializer
import edu.byu.uapi.spi.input.QueryParamDeserializer
import edu.byu.uapi.spi.scalars.ScalarType
import kotlin.reflect.KClass

interface TypeDictionary {
    fun <Type : Any> pathDeserializer(type: KClass<Type>): SuccessOrFailure<PathParamDeserializer<Type>, DeserializationFailure<*>>
    fun <Type : Any> queryDeserializer(type: KClass<Type>): SuccessOrFailure<QueryParamDeserializer<Type>, DeserializationFailure<*>>

    fun <Type: Any> scalarConverter(type: KClass<Type>): SuccessOrFailure<ScalarType<Type>, DeserializationFailure<*>>
}
