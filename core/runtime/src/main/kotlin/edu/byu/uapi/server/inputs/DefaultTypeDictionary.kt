package edu.byu.uapi.server.inputs

import edu.byu.uapi.server.scalars.EnumScalarConverterHelper
import edu.byu.uapi.server.scalars.builtinScalarTypeMap
import edu.byu.uapi.server.scalars.builtinScalarTypes
import edu.byu.uapi.server.spi.UAPITypeError
import edu.byu.uapi.server.spi.reflective.ReflectiveListParamReader
import edu.byu.uapi.spi.dictionary.MaybeTypeFailure
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.dictionary.TypeFailure
import edu.byu.uapi.spi.functional.Failure
import edu.byu.uapi.spi.functional.Success
import edu.byu.uapi.spi.functional.asSuccess
import edu.byu.uapi.spi.input.EmptyListParamReader
import edu.byu.uapi.spi.input.ListParamReader
import edu.byu.uapi.spi.input.ListParams
import edu.byu.uapi.spi.input.PathParamReader
import edu.byu.uapi.spi.scalars.ScalarType
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

class DefaultTypeDictionary: TypeDictionary {

    private val explicitScalarConverters = mapOf<KClass<*>, ScalarType<*>>() + builtinScalarTypeMap

    private val explicitPathDeserializers = mapOf<KClass<*>, PathParamReader<*>>(

    ) + builtinScalarTypes.map { it.type to ScalarPathParamReader(it) }

    override fun <Type : Any> pathDeserializer(type: KClass<Type>): MaybeTypeFailure<PathParamReader<Type>> {
        if (explicitPathDeserializers.containsKey(type)) {
            @Suppress("UNCHECKED_CAST")
            return Success(explicitPathDeserializers[type] as PathParamReader<Type>)
        } else if (type.isEnum()) {
            return Success(ScalarPathParamReader(
                EnumScalarConverterHelper.getEnumScalarConverter(
                    type
                )))
        }
        TODO("Add new deserializer types - generated, reflective, etc.")
    }

    override fun <Type : Any> listParamReader(type: KClass<Type>): MaybeTypeFailure<ListParamReader<Type>> {
        if (type == ListParams.Empty::class) {
            @Suppress("UNCHECKED_CAST")
            return (EmptyListParamReader as ListParamReader<Type>).asSuccess()
        }
        return try {
            // TODO: Add caching
            ReflectiveListParamReader(type, this).asSuccess()
        } catch (err: UAPITypeError) {
            Failure(TypeFailure(
                type.createType(),
                err.message!!,
                err
            ))
        }
    }

    override fun <Type : Any> scalarType(type: KClass<Type>): ScalarType<Type>? {
        if (explicitScalarConverters.containsKey(type)) {
            @Suppress("UNCHECKED_CAST")
            return explicitScalarConverters[type] as ScalarType<Type>
        }
        if (type.isEnum()) {
            return EnumScalarConverterHelper.getEnumScalarConverter(type)
        }
        return null
    }

    override fun isScalarType(type: KClass<*>): Boolean {
        return explicitPathDeserializers.containsKey(type) || type.isEnum()
    }
}

private fun KClass<*>.isEnum() = this.java.isEnum
