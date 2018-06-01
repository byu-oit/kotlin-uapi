package edu.byu.uapidsl.types.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper
import com.fasterxml.jackson.databind.ser.std.EnumSerializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.databind.util.EnumValues
import edu.byu.uapidsl.types.ApiEnum
import kotlin.reflect.KClass

internal inline infix fun <T> T?.maybe(block: (T) -> Unit) {
    if (this != null) {
        block(this)
    }
}

open class ApiEnumSerializer<E : ApiEnum>(private val type: KClass<E>) : StdSerializer<E>(type.java) {
    override fun serialize(value: E, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.serialized)
    }

    override fun acceptJsonFormatVisitor(visitor: JsonFormatVisitorWrapper, typeHint: JavaType) {
        visitor.expectStringFormat(typeHint).enumTypes(type.java.enumConstants.map { it.serialized }.toSet())
    }
}

