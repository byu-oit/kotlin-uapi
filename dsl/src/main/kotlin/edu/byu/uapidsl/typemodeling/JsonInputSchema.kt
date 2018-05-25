package edu.byu.uapidsl.typemodeling

import com.fasterxml.jackson.module.jsonSchema.JsonSchema
import com.fasterxml.jackson.module.jsonSchema.types.ArraySchema
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema
import com.fasterxml.jackson.module.jsonSchema.types.ValueTypeSchema
import kotlin.reflect.KClass

data class JsonInputSchema(
    val name: String,
    val type: KClass<*>,
    val fields: List<JsonInputField<*>>,
    val jsonSchema: ObjectSchema
)

sealed class JsonInputField<SchemaType : JsonSchema> {
    abstract val name: String
    abstract val jsonSchema: SchemaType
    val required: Boolean
        get() = jsonSchema.required

    val description: String?
        get() = jsonSchema.description
}

data class SimpleJsonInputField(
    override val name: String,
    override val jsonSchema: ValueTypeSchema
) : JsonInputField<ValueTypeSchema>()

data class ObjectJsonInputField(
    override val name: String,
    override val jsonSchema: ObjectSchema,
    val fields: List<JsonInputField<*>>
) : JsonInputField<ObjectSchema>()

data class CollectionJsonInputField(
    override val name: String,
    override val jsonSchema: ArraySchema,
    val itemType: JsonInputField<*>
) : JsonInputField<ArraySchema>()
