package edu.byu.uapidsl.types.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import edu.byu.uapidsl.types.ApiType
import edu.byu.uapidsl.types.UAPIField
import kotlin.reflect.KClass

internal val fieldSerializers = mapOf<KClass<*>, JsonSerializer<*>>(
  ApiType::class to ApiTypeSerializer,
  UAPIField::class to UAPIFieldSerializer
)

object ApiTypeSerializer : ApiEnumSerializer<ApiType>(ApiType::class)

object UAPIFieldSerializer : StdSerializer<UAPIField<*>>(UAPIField::class.java) {
  override fun serialize(uapiField: UAPIField<*>, gen: JsonGenerator, serializers: SerializerProvider) {
    gen.writeStartObject()
    uapiField.apply {
      serializers.defaultSerializeField("value", value, gen)
      serializers.defaultSerializeField("api_type", apiType, gen)
      if (key) {
        gen.writeBooleanField("key", true)
      }
      description maybe { gen.writeStringField("description", it)}
      longDescription maybe { gen.writeStringField("long_description", it)}
      displayLabel maybe { gen.writeStringField("display_label", it) }
      domain maybe { gen.writeStringField("domain", it.toASCIIString()) }
      relatedResource maybe { gen.writeStringField("related_resource", it.toASCIIString()) }
    }
    gen.writeEndObject()
  }

}

