package edu.byu.uapidsl.adapters.openapi3.converter

import edu.byu.uapidsl.model.Introspectable
import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import kotlin.reflect.KClass
import kotlin.reflect.KType

fun <Type : Any> Introspectable<Type>.toSchema(): Schema<*> {
    val s: ObjectSchema = ObjectSchema()

//    s.properties(
//        this.uapiProps.mapValues {
//            it.value.returnType
//        }
//    )

    return s
}

data class TypeMapping(
    val type: KClass<*>,
    val schema: Schema<*>
)

//val typeMappings = listOf(
//    TypeMapping(String::class, )
//)



