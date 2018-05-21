package edu.byu.uapidsl.converters

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.module.jsonSchema.JsonSchema
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema
import com.fasterxml.jackson.module.jsonSchema.types.ValueTypeSchema
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import edu.byu.uapidsl.types.ApiType
import edu.byu.uapidsl.types.JsonSchemaType
import edu.byu.uapidsl.types.jackson.JacksonUAPITypesModule
import kotlin.reflect.KClass

data class TypeModelerConfig(
    val jacksonModules: List<Module> = emptyList()
)

class TypeModeler(config: TypeModelerConfig = TypeModelerConfig()) {

    private val jsonReaderCache: LoadingCache<KClass<*>, ObjectReader> = Caffeine.newBuilder()
        .softValues()
        .build(this::buildJsonReaderFor)

    private val jsonWriterCache: LoadingCache<KClass<*>, ObjectWriter> = Caffeine.newBuilder()
        .softValues()
        .build(this::buildJsonWriterFor)

    private val schemaCache: LoadingCache<KClass<*>, JsonSchema> = Caffeine.newBuilder()
        .softValues()
        .build { schemaGenerator.generateSchema(it.java) }

    private val queryParamSchemaCache = Caffeine.newBuilder()
        .softValues()
        .build(this::buildQueryParamSchemaFor)

    private val pathParamSchemaCache = Caffeine.newBuilder()
        .softValues()
        .build(this::buildPathParamSchemaFor)

    private val objectMapper: ObjectMapper = ObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
        .registerModules(
            KotlinModule(),
            JacksonUAPITypesModule(),
            Jdk8Module()
        )
        .registerModules(config.jacksonModules)


    private val schemaGenerator = JsonSchemaGenerator(objectMapper)

    private fun buildJsonReaderFor(type: KClass<*>): ObjectReader {
        return objectMapper.readerFor(type.java).with(DeserializationFeature.EAGER_DESERIALIZER_FETCH)
    }

    fun getJsonReaderFor(type: KClass<*>): ObjectReader {
        return jsonReaderCache[type]!!
    }

    private fun buildJsonWriterFor(type: KClass<*>): ObjectWriter {
        return objectMapper.writerFor(type.java).with(SerializationFeature.EAGER_SERIALIZER_FETCH)
    }

    fun getJsonWriterFor(type: KClass<*>): ObjectWriter {
        return jsonWriterCache[type]!!
    }

    @Throws(UnmappableTypeException::class)
    fun validateForJsonOutput(type: KClass<*>) {
        TODO("This will check if we have all the Jackson serializers and such. Also validates that all fields are of the correct type")
    }

    @Throws(UnmappableTypeException::class)
    fun validateForPathParamInput(type: KClass<*>) {
        TODO("Check that we know how to deserialize this type from path params")
    }

    @Throws(UnmappableTypeException::class)
    fun validateForQueryParamInput(type: KClass<*>) {
        TODO("Check that we know how to deserialize this type from query params")
    }

    @Throws(UnmappableTypeException::class)
    fun <Type : Any> queryParamReaderFor(type: KClass<Type>): QueryParamReader<Type> {
        return JacksonQueryParamReader(type, queryParamSchemaFor(type), getJsonReaderFor(type)) { objectMapper.createObjectNode() }
    }

    fun queryParamSchemaFor(type: KClass<*>): QueryParamSchema = queryParamSchemaCache[type]!!

    private fun buildQueryParamSchemaFor(type: KClass<*>): QueryParamSchema {
        val schema = schemaFor(type)
        if (!schema.isObjectSchema) {
            //TODO: Pretty message here
            throw UnmappableTypeException("")
        }
        val objSchema = schema.asObjectSchema()
        val props = analyzeQueryParamObjectSchema(objSchema).toMap()
        return QueryParamSchema(type, objSchema, props)
    }

    @Throws(UnmappableTypeException::class)
    fun <Type : Any> pathParamReaderFor(type: KClass<Type>): PathParamReader<Type> = JacksonPathParamReader(type, getJsonReaderFor(type)) { objectMapper.createObjectNode() }

    fun pathParamSchemaFor(type: KClass<*>): PathParamSchema<*> = pathParamSchemaCache[type]!!

    private fun buildPathParamSchemaFor(type: KClass<*>): PathParamSchema<*> {
        val schema = schemaFor(type)
        return if (schema.isObjectSchema) {
            val obj = schema.asObjectSchema()

            val props = obj.properties.mapValues {
                val value: JsonSchema = it.value

                if (!value.isValueTypeSchema) {
                    throw UnmappableTypeException("Path params can only have scalar values, not nested objects or arrays")
                }
                PathParamField(value.asValueTypeSchema())
            }
            ComplexPathParamSchema(type, obj, props)
        } else {
            SimplePathParamSchema("id", type, schema.asValueTypeSchema())
        }
    }

    fun schemaFor(type: KClass<*>): JsonSchema = schemaCache[type]!!

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
}

interface QueryParamReader<Type : Any> {
    fun read(params: Map<String, Set<String>>): Type
}

abstract class PathParamSchema<SchemaType : JsonSchema>(
    val paramType: PathParamType
) {
    abstract val type: KClass<*>
    abstract val jsonSchema: SchemaType
    abstract val properties: Map<String, PathParamField>
}

data class SimplePathParamSchema(
    val name: String,
    override val type: KClass<*>,
    override val jsonSchema: ValueTypeSchema
) : PathParamSchema<ValueTypeSchema>(PathParamType.SIMPLE) {
    override val properties: Map<String, PathParamField> = mapOf(name to PathParamField(jsonSchema))
}

data class ComplexPathParamSchema(
    override val type: KClass<*>,
    override val jsonSchema: ObjectSchema,
    override val properties: Map<String, PathParamField>
) : PathParamSchema<ObjectSchema>(PathParamType.COMPLEX)

enum class PathParamType {
    SIMPLE, COMPLEX
}

data class PathParamField(
    val jsonSchema: ValueTypeSchema
)

abstract class QueryParamField<SchemaType : JsonSchema>(
    val type: QueryParamType
) {
    abstract val jsonSchema: SchemaType
}

data class QueryParamValueField(
    override val jsonSchema: ValueTypeSchema
) : QueryParamField<ValueTypeSchema>(QueryParamType.SCALAR)

data class QueryParamObjectField(
    override val jsonSchema: ObjectSchema
) : QueryParamField<ObjectSchema>(QueryParamType.OBJECT)

data class QueryParamArrayField(
    override val jsonSchema: ArraySchema
) : QueryParamField<ArraySchema>(QueryParamType.ARRAY)

typealias ObjectNodeCreator = () -> ObjectNode

enum class QueryParamType {
    ARRAY, SCALAR, OBJECT
}

fun ObjectNode.ensureObjectAtPath(path: List<String>): ObjectNode {
    return path.fold(this) { acc, part ->
        val found = acc.get(part)
        if (found != null) {
            //TODO(should probably, you know, check the type of this and throw a pretty exception)
            found as ObjectNode
        } else {
            acc.putObject(part)
        }
    }
}

fun ObjectNode.putArray(name: String, values: Iterable<String>) {
    val array = this.putArray(name)
    values.forEach { array.add(it) }
}

class JacksonQueryParamReader<Type : Any>(
    val type: KClass<Type>,
    private val schema: QueryParamSchema,
    private val jsonReader: ObjectReader,
    private val nodeCreator: ObjectNodeCreator
) : QueryParamReader<Type> {

    override fun read(params: Map<String, Set<String>>): Type {
        // TODO(Validate inputs against jsonSchema and throw a pretty error)
        val root: ObjectNode = nodeCreator()

        params.forEach { key, values ->
            val pathParts = key.split('.')
            val pathPrefix = pathParts.dropLast(1)
            val name = pathParts.last()
            val obj = root.ensureObjectAtPath(pathPrefix)

            when (schema.properties[key]?.type) {
                QueryParamType.SCALAR -> obj.put(name, values.first())
                QueryParamType.ARRAY -> obj.putArray(name, values.flatMap { it.split(',') })
                QueryParamType.OBJECT -> throw UnmappableTypeException("this ought to be a better exception")
            }
        }

        return jsonReader.readValue<Type>(root)
    }
}

data class QueryParamSchema(
    val type: KClass<*>,
    val jsonSchema: ObjectSchema,
    val properties: Map<String, QueryParamField<*>>
)

class JacksonPathParamReader<Type : Any>(
    val type: KClass<Type>,
    private val jsonReader: ObjectReader,
    private val nodeCreator: ObjectNodeCreator
) : PathParamReader<Type> {
    override fun read(params: Map<String, String>): Type {
        // TODO(Validate inputs against jsonSchema and throw a pretty error)
        val root: ObjectNode = nodeCreator()
        params.forEach { key, value -> root.put(key, value) }

        return jsonReader.readValue(root as JsonNode)
    }
}

interface PathParamReader<Type : Any> {
    fun read(params: Map<String, String>): Type
}

data class OutputMetadata(
    val name: String,
    val fields: List<OutputField>
)

data class OutputField(
    val name: String,
    val type: KClass<*>,
    val schemaType: JsonSchemaType,
    val allowedApiTypes: Set<ApiType>,
    val key: Boolean = false,
    val displayName: String? = null,
    val description: String? = null
)

data class InputMetadata(
    val name: String,
    val fields: Map<String, InputField>
)

sealed class InputField {
    abstract val nullable: Boolean
    abstract val required: Boolean
    abstract val description: String?
}

data class SimpleInputField(
    val type: KClass<*>,
    val schemaType: JsonSchemaType,
    val pattern: String? = null,
    override val nullable: Boolean,
    override val required: Boolean = !nullable,
    override val description: String? = null
) : InputField()

data class ObjectInputField(
    val type: KClass<*>,
    val fields: Map<String, InputField>,
    override val nullable: Boolean,
    override val required: Boolean = !nullable,
    override val description: String? = null
) : InputField()

data class CollectionInputField(
    val type: KClass<Collection<*>>,
    val itemType: InputField,
    override val nullable: Boolean,
    override val required: Boolean = !nullable,
    override val description: String? = null
) : InputField()

class UnmappableTypeException(
    message: String
) : RuntimeException(message)

