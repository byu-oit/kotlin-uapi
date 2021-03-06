package edu.byu.uapi.server.spi

import edu.byu.uapi.server.inputs.create
import edu.byu.uapi.spi.UAPITypeError
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.scalars.ScalarType
import kotlin.reflect.KClass

fun <Type: Any> TypeDictionary.requireScalarType(type: KClass<Type>): ScalarType<Type> {
    return this.scalarType(type) ?: throw UAPITypeError.create(type, "Unable to find a scalar type mapping")
}
