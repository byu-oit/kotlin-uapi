package edu.byu.uapi.server.spi

import edu.byu.uapi.server.inputs.typeFailure
import edu.byu.uapi.spi.dictionary.MaybeTypeFailure
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.functional.asSuccess
import edu.byu.uapi.spi.scalars.ScalarType
import kotlin.reflect.KClass

fun <Type: Any> TypeDictionary.requireScalarType(type: KClass<Type>): ScalarType<Type> {
    return this.scalarType(type) ?: throw UAPITypeError(type, "Unable to find a scalar type mapping")
}

fun <Type: Any> TypeDictionary.scalarTypeOrFailure(type: KClass<Type>): MaybeTypeFailure<ScalarType<Type>> {
    return this.scalarType(type)?.asSuccess() ?: return typeFailure(type, "Unable to find a scalar type mapping")
}
