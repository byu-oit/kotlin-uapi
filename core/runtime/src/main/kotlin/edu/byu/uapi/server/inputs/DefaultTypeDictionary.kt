package edu.byu.uapi.server.inputs

import edu.byu.uapi.server.scalars.EnumScalarConverterHelper
import edu.byu.uapi.server.scalars.builtinScalarTypeMap
import edu.byu.uapi.server.scalars.builtinScalarTypes
import edu.byu.uapi.server.spi.UAPITypeError
import edu.byu.uapi.server.spi.reflective.ReflectiveCollectionParamsProvider
import edu.byu.uapi.spi.dictionary.MaybeTypeFailure
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.dictionary.TypeFailure
import edu.byu.uapi.spi.functional.Failure
import edu.byu.uapi.spi.functional.Success
import edu.byu.uapi.spi.functional.asSuccess
import edu.byu.uapi.spi.input.*
import edu.byu.uapi.spi.scalars.ScalarType
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

class DefaultTypeDictionary(
) : TypeDictionary {

    private val explicitScalarConverters = mapOf<KClass<*>, ScalarType<*>>() + builtinScalarTypeMap

    private val explicitPathDeserializers = mapOf<KClass<*>, PathParamDeserializer<*>>(

    ) + builtinScalarTypes.map { it.type to ScalarPathParamDeserializer(it) }

    override fun <Type : Any> pathDeserializer(type: KClass<Type>): MaybeTypeFailure<PathParamDeserializer<Type>> {
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

    override fun <Type : Any> queryDeserializer(type: KClass<Type>): MaybeTypeFailure<QueryParamReader<Type>> {
//        if (explicitPathDeserializers.containsKey(type)) {
//            return Success(explicitPathDeserializers[type])
//        }
        TODO("not implemented")
    }

    override fun <Type : Any> collectionParamsProvider(type: KClass<Type>): MaybeTypeFailure<CollectionParamsProvider<Type>> {
        if (type == Params.Empty::class) {
            @Suppress("UNCHECKED_CAST")
            return (EmptyCollectionParamsProvider as CollectionParamsProvider<Type>).asSuccess()
        }
        return try {
            // TODO: Add caching
            ReflectiveCollectionParamsProvider(type, this).asSuccess()
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
