package edu.byu.uapidsl.typemodeling

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.module.jsonSchema.JsonSchema
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema
import com.fasterxml.jackson.module.kotlin.KotlinModule
import edu.byu.uapidsl.toSnakeCase
import edu.byu.uapidsl.types.ApiType
import edu.byu.uapidsl.types.jackson.JacksonUAPITypesModule
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

class DefaultTypeModeler(config: TypeModelerConfig = TypeModelerConfig()) : TypeModeler {

    private val objectMapper: ObjectMapper = ObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
        .registerModules(
            KotlinModule(),
            JacksonUAPITypesModule(),
            Jdk8Module()
        )
        .registerModules(config.jacksonModules)


    private val schemaGenerator = JsonSchemaGenerator(objectMapper)

    override fun getJsonReaderFor(type: KClass<*>): ObjectReader {
        return objectMapper.readerFor(type.java).with(DeserializationFeature.EAGER_DESERIALIZER_FETCH)
    }

    override fun getJsonWriterFor(type: KClass<*>): ObjectWriter {
        return objectMapper.writerFor(type.java).with(SerializationFeature.EAGER_SERIALIZER_FETCH)
    }

    @Throws(UnmappableTypeException::class)
    override fun validateForJsonOutput(type: KClass<*>) {
        TODO("This will check if we have all the Jackson serializers and such. Also validates that all properties are of the correct type")
    }

    @Throws(UnmappableTypeException::class)
    override fun validateForPathParamInput(type: KClass<*>) {
        TODO("Check that we know how to deserialize this type from path params")
    }

    @Throws(UnmappableTypeException::class)
    override fun validateForQueryParamInput(type: KClass<*>) {
        TODO("Check that we know how to deserialize this type from query params")
    }

    @Throws(UnmappableTypeException::class)
    override fun <Type : Any> queryParamReaderFor(type: KClass<Type>): QueryParamReader<Type> {
        return JacksonQueryParamReader(type, queryParamSchemaFor(type), getJsonReaderFor(type)) { objectMapper.createObjectNode() }
    }

    override fun queryParamSchemaFor(type: KClass<*>): QueryParamSchema {
        val schema = jsonSchemaFor(type)
        if (!schema.isObjectSchema) {
            //TODO: Pretty message here
            throw UnmappableTypeException("")
        }
        val objSchema = schema.asObjectSchema()
        val props = analyzeQueryParamObjectSchema(objSchema).toMap()
        return QueryParamSchema(type, objSchema, props)
    }

    @Throws(UnmappableTypeException::class)
    override fun <Type : Any> pathParamReaderFor(type: KClass<Type>): PathParamReader<Type> = JacksonPathParamReader(type, getJsonReaderFor(type)) { objectMapper.createObjectNode() }

    override fun pathParamSchemaFor(type: KClass<*>): PathParamSchema<*> {
        val schema = jsonSchemaFor(type)
        return if (schema.isObjectSchema) {
            val obj = schema.asObjectSchema()

            val props = obj.properties.map {
                val value: JsonSchema = it.value

                if (!value.isValueTypeSchema) {
                    throw UnmappableTypeException("Path params can only have scalar values, not nested objects or arrays")
                }
                PathParamField(it.key, value.asValueTypeSchema())
            }
            ComplexPathParamSchema(type, obj, props)
        } else {
            SimplePathParamSchema("id", type, schema.asValueTypeSchema())
        }
    }

    override fun outputSchemaFor(type: KClass<*>): OutputSchema {
        val schema: ObjectSchema = jsonSchemaFor(type).asObjectSchema()

        val props = (schema.properties as Map<String, JsonSchema>).map {
            val (name, prop) = it
            if (!prop.isValueTypeSchema) {
                //TODO(sometime when Joseph isn't hopped up on allergy meds, add a pretty error message)
                throw UnmappableTypeException("$type $name isn't a value type")
            }
            OutputField(
                name = name,
                valueSchema = prop.asValueTypeSchema(),
                allowedApiTypes = setOf(ApiType.MODIFIABLE)
            )
        }

        return OutputSchema(
            name = type.jvmName.toSnakeCase(),
            properties = props,
            jsonSchema = schema
        )
    }

    override fun jsonInputSchemaFor(type: KClass<*>): JsonInputSchema {
        val schema: ObjectSchema = jsonSchemaFor(type).asObjectSchema()

        val props = schema.properties.map { analyzeJsonInputSchema(it.key, it.value) }

        return JsonInputSchema(type.jvmName.toSnakeCase(), type, props, schema)
    }

    override fun jsonSchemaFor(type: KClass<*>): JsonSchema = schemaGenerator.generateSchema(type.java)

    private fun analyzeQueryParamObjectSchema(schema: ObjectSchema, prefix: String = ""): Iterable<Pair<String, QueryParamField<*>>> {
        return (schema.properties as Map<String, JsonSchema>).flatMap {
            val (name, prop) = it

            when {
                prop.isArraySchema -> listOf(name to QueryParamArrayField(prop.asArraySchema()))
                prop.isObjectSchema -> analyzeQueryParamObjectSchema(prop.asObjectSchema(), "$prefix.$name") + (name to QueryParamObjectField(prop.asObjectSchema()))
                else -> listOf(name to QueryParamValueField(prop.asValueTypeSchema()))
            }
        }
    }

    private fun analyzeJsonInputObjectSchema(schema: ObjectSchema): List<JsonInputField<*>> {
        return schema.properties.map { analyzeJsonInputSchema(it.key, it.value) }
    }

    private fun analyzeJsonInputSchema(name: String, schema: JsonSchema): JsonInputField<*> {
        return when {
            schema.isArraySchema -> {
                val array = schema.asArraySchema()
                CollectionJsonInputField(
                    name = name,
                    itemType = analyzeJsonInputSchema("", array.items.asSingleItems().schema),
                    jsonSchema = array
                )
            }
            schema.isObjectSchema -> {
                val obj = schema.asObjectSchema()
                val props = obj.properties.map { analyzeJsonInputSchema(it.key, it.value) }
                ObjectJsonInputField(
                    name, obj, props
                )
            }
            schema.isValueTypeSchema -> {
                SimpleJsonInputField(name, schema.asValueTypeSchema())
            }
            else -> throw UnmappableTypeException("$name $schema")
        }
    }

}
