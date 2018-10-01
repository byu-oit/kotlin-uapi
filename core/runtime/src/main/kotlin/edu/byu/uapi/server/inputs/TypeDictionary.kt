package edu.byu.uapi.server.inputs

import edu.byu.uapi.server.scalars.EnumScalarConverterHelper
import edu.byu.uapi.server.scalars.ScalarConverter
import edu.byu.uapi.server.scalars.defaultScalarConverters
import edu.byu.uapi.server.types.Success
import edu.byu.uapi.server.types.SuccessOrFailure
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier

interface TypeDictionary {
    fun <Type : Any> pathDeserializer(type: KClass<Type>): SuccessOrFailure<PathParamDeserializer<Type>, DeserializationFailure<*>>
    fun <Type : Any> queryDeserializer(type: KClass<Type>): SuccessOrFailure<QueryParamDeserializer<Type>, DeserializationFailure<*>>

    fun <Type: Any> scalarConverter(type: KClass<Type>): SuccessOrFailure<ScalarConverter<Type>, DeserializationFailure<*>>
}

class DefaultTypeDictionary : TypeDictionary {

    private val explicitScalarConverters = mapOf<KClass<*>, ScalarConverter<*>>() + defaultScalarConverters

    private val explicitPathDeserializers = mapOf<KClass<*>, PathParamDeserializer<*>>(

    ) + defaultScalarConverters.mapValues { ScalarPathParamDeserializer(it.value) }

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

    override fun <Type : Any> queryDeserializer(type: KClass<Type>): SuccessOrFailure<QueryParamDeserializer<Type>, DeserializationFailure<*>> {
//        if (explicitPathDeserializers.containsKey(type)) {
//            return Success(explicitPathDeserializers[type])
//        }
        TODO("not implemented")
    }

    override fun <Type : Any> scalarConverter(type: KClass<Type>): SuccessOrFailure<ScalarConverter<Type>, DeserializationFailure<*>> {
        if (explicitScalarConverters.containsKey(type)) {
            @Suppress("UNCHECKED_CAST")
            return Success(explicitScalarConverters[type] as ScalarConverter<Type>)
        }
        if (type.isEnum()) {
            return Success(EnumScalarConverterHelper.getEnumScalarConverter(type))
        }
        return fail(type, "No scalar converter has been registered for this type.")
    }
}

private fun KClass<*>.isEnum() = this.java.isEnum

data class DeserializationFailure<T>(
    val type: KClassifier,
    val message: String,
    val cause: Throwable? = null
) {
    fun asError(): DeserializationError = DeserializationError(type, message, cause)
}

class DeserializationError(
    val type: KClassifier,
    message: String,
    cause: Throwable? = null
) : Exception("Error deserializing type $type: $message", cause)
