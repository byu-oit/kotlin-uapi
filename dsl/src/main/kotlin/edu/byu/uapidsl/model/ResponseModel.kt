package edu.byu.uapidsl.model

import edu.byu.uapidsl.converters.ScalarSerializer
import edu.byu.uapidsl.converters.defaultScalars
import edu.byu.uapidsl.types.UAPIField
import edu.byu.uapidsl.types.UAPIScalar
import edu.byu.uapidsl.types.UAPIString
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType

fun<Type: Any> getResponseModel(type: Introspectable<Type>): ResponseModel<Type> {
    val fields: List<ResponseField<Type, *>> = type.uapiProps.map {
        val name = it.key
        val prop = it.value as KProperty1<Type, UAPIField<*>>

        ResponseField(
            prop.returnType,
            UAPIString::class,
            prop.name,
            name,
            defaultScalars[String::class]!!,
            prop::get
        )
    }

    return ResponseModel(type, fields)
}

data class ResponseModel<Type: Any>(
    val type: Introspectable<Type>,
    val fields: List<ResponseField<Type, *>>
)

data class ResponseField<ModelType: Any, FieldType: UAPIField<*>>(
    val type: KType,
    val uapiType: KClass<out UAPIScalar<*>>,
    val name: String,
    val uapiName: String,
    val serializer: ScalarSerializer<*, *>,
    val reader: (ModelType) -> FieldType
)



