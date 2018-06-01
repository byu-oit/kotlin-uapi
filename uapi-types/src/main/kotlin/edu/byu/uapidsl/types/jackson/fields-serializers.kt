package edu.byu.uapidsl.types.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import edu.byu.uapidsl.types.ApiType
import edu.byu.uapidsl.types.UAPIField
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.KClass

internal val fieldSerializers = mapOf<KClass<*>, JsonSerializer<*>>(
    ApiType::class to ApiTypeSerializer
//    UAPIField::class to UAPIFieldSerializer
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
            description maybe { gen.writeStringField("description", it) }
            longDescription maybe { gen.writeStringField("long_description", it) }
            displayLabel maybe { gen.writeStringField("display_label", it) }
            domain maybe { gen.writeStringField("domain", it.toASCIIString()) }
            relatedResource maybe { gen.writeStringField("related_resource", it.toASCIIString()) }
        }
        gen.writeEndObject()
    }

    private val apiTypeSchema by lazy {
        val node = createSchemaNode("string")
        val enumValues = node.putArray("enum")

        ApiType.values().forEach { enumValues.add(it.serialized) }

        node
    }

    private val keySchema by lazy {
        createSchemaNode("boolean", true)
    }

    private val descriptionSchema by lazy {
        createSchemaNode("string", true)
    }

    private val longDescriptionSchema by lazy {
        createSchemaNode("string", true)
    }

    private val displayLabelSchema by lazy {
        createSchemaNode("string", true)
    }

    private val domainSchema by lazy {
        createSchemaNode("string", true)
    }

    private val relatedSchema by lazy {
        createSchemaNode("string", true)
    }

    override fun getSchema(provider: SerializerProvider, typeHint: Type): JsonNode {
        val schema = createSchemaNode("object")

        val parameterized = typeHint as ParameterizedType

        val valueClass = parameterized.actualTypeArguments.first() as Class<*>

        val ser = provider.findValueSerializer(valueClass) as StdSerializer<*>

        val valueSchema = ser.getSchema(provider, valueClass)

        schema.putObject("properties").apply {
            set("value", valueSchema)
            set("api_type", apiTypeSchema)
            set("key", keySchema)
            set("description", descriptionSchema)
            set("long_description", longDescriptionSchema)
            set("display_label", displayLabelSchema)
            set("domain", domainSchema)
            set("related_resource", relatedSchema)
        }

        return schema
    }

//    override fun acceptJsonFormatVisitor(visitor: JsonFormatVisitorWrapper?, typeHint: JavaType) {
//        if (visitor == null) {
//            return
//        }
//        val objectVisitor = visitor.expectObjectFormat(typeHint) ?: return
//
//        objectVisitor.property()
//
//        val provider = visitor.provider
//
//        if (_propertyFilterId != null) {
//            val filter = findPropertyFilter(visitor.provider,
//                _propertyFilterId, null)
//            var i = 0
//            val end = _props.size
//            while (i < end) {
//                filter.depositSchemaProperty(_props[i], objectVisitor, provider)
//                ++i
//            }
//        } else {
//            val view = if (_filteredProps == null || provider == null)
//                null
//            else
//                provider.activeView
//            val props: Array<BeanPropertyWriter>
//            if (view != null) {
//                props = _filteredProps
//            } else {
//                props = _props
//            }
//
//            var i = 0
//            val end = props.size
//            while (i < end) {
//                val prop = props[i]
//                prop?.depositSchemaProperty(objectVisitor, provider)
//                ++i
//            }
//        }
//    }


}

