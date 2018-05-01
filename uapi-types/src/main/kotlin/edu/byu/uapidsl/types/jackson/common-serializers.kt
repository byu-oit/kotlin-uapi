package edu.byu.uapidsl.types.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import edu.byu.uapidsl.types.ApiEnum
import kotlin.reflect.KClass

internal inline infix fun <T> T?.maybe(block: (T) -> Unit) {
    if (this != null) {
        block(this)
    }
}

open class ApiEnumSerializer<E : ApiEnum>(type: KClass<E>) : StdSerializer<E>(type.java) {
    override fun serialize(value: E, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.serialized)
    }
}
