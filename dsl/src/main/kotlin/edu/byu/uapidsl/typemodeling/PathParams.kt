package edu.byu.uapidsl.typemodeling

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.databind.node.ValueNode
import com.fasterxml.jackson.module.jsonSchema.JsonSchema
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema
import com.fasterxml.jackson.module.jsonSchema.types.ValueTypeSchema
import kotlin.reflect.KClass

sealed class PathParamSchema<SchemaType : JsonSchema>(
    val paramType: PathParamType
) {
    abstract val type: KClass<*>
    abstract val jsonSchema: SchemaType
    abstract val properties: List<PathParamField>
}

data class SimplePathParamSchema(
    val name: String,
    override val type: KClass<*>,
    override val jsonSchema: ValueTypeSchema
) : PathParamSchema<ValueTypeSchema>(PathParamType.SIMPLE) {
    override val properties: List<PathParamField> = listOf(PathParamField(name, jsonSchema))
}

data class ComplexPathParamSchema(
    override val type: KClass<*>,
    override val jsonSchema: ObjectSchema,
    override val properties: List<PathParamField>
) : PathParamSchema<ObjectSchema>(PathParamType.COMPLEX)

enum class PathParamType {
    SIMPLE, COMPLEX
}

data class PathParamField(
    val name: String,
    val jsonSchema: ValueTypeSchema
)

class JacksonSimplePathParamReader<Type : Any>(
    val type: KClass<Type>,
    private val jsonReader: ObjectReader
) : PathParamReader<Type> {
    override fun read(params: Map<String, String>): Type {
        val value = params.values.single()
        return jsonReader.readValue(TextNode.valueOf(value))
    }
}


class JacksonComplexPathParamReader<Type : Any>(
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
