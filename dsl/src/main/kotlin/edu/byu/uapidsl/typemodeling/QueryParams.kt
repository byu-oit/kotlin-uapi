package edu.byu.uapidsl.typemodeling

import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.jsonSchema.JsonSchema
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema
import com.fasterxml.jackson.module.jsonSchema.types.ValueTypeSchema
import kotlin.reflect.KClass

interface QueryParamReader<Type : Any> {
    fun read(params: Map<String, Set<String>>): Type
}

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

enum class QueryParamType {
    ARRAY, SCALAR, OBJECT
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
