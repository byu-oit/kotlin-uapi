package edu.byu.uapi.server.inputs

import edu.byu.uapi.server.scalars.EnumScalarConverterHelper
import edu.byu.uapi.spi.scalars.ScalarType
import edu.byu.uapi.server.scalars.builtinScalarTypeMap
import edu.byu.uapi.server.scalars.builtinScalarTypes
import edu.byu.uapi.spi.dictionary.DeserializationFailure
import edu.byu.uapi.spi.functional.Success
import edu.byu.uapi.spi.functional.SuccessOrFailure
import edu.byu.uapi.spi.input.PathParamDeserializer
import edu.byu.uapi.spi.input.QueryParamReader
import edu.byu.uapi.spi.dictionary.TypeDictionary
import kotlin.reflect.KClass

class DefaultTypeDictionary : TypeDictionary {

    private val explicitScalarConverters = mapOf<KClass<*>, ScalarType<*>>() + builtinScalarTypeMap

    private val explicitPathDeserializers = mapOf<KClass<*>, PathParamDeserializer<*>>(

    ) + builtinScalarTypes.map { it.type to ScalarPathParamDeserializer(it) }

    override fun <Type : Any> pathDeserializer(type: KClass<Type>): SuccessOrFailure<PathParamDeserializer<Type>, DeserializationFailure<*>> {
        if (explicitPathDeserializers.containsKey(type)) {
            @Suppress("UNCHECKED_CAST")
            return Success(explicitPathDeserializers[type] as PathParamDeserializer<Type>)
        } else if (type.isEnum()) {
            return Success(ScalarPathParamDeserializer(
                EnumScalarConverterHelper.getEnumScalarConverter(
                    type
                )))
        }
        TODO("Add new deserializer types - generated, reflective, etc.")
    }

    override fun <Type : Any> queryDeserializer(type: KClass<Type>): SuccessOrFailure<QueryParamReader<Type>, DeserializationFailure<*>> {
//        if (explicitPathDeserializers.containsKey(type)) {
//            return Success(explicitPathDeserializers[type])
//        }
        TODO("not implemented")
    }

    override fun <Type : Any> scalarConverter(type: KClass<Type>): SuccessOrFailure<ScalarType<Type>, DeserializationFailure<*>> {
        if (explicitScalarConverters.containsKey(type)) {
            @Suppress("UNCHECKED_CAST")
            return Success(explicitScalarConverters[type] as ScalarType<Type>)
        }
        if (type.isEnum()) {
            return Success(EnumScalarConverterHelper.getEnumScalarConverter(type))
        }
        return fail(type, "No scalar converter has been registered for this type.")
    }
}

private fun KClass<*>.isEnum() = this.java.isEnum

