package edu.byu.uapidsl.typemodeling

import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema
import com.fasterxml.jackson.module.jsonSchema.types.ValueTypeSchema
import edu.byu.uapidsl.types.ApiType

data class OutputSchema(
    val name: String,
    val properties: List<OutputField>,
    val jsonSchema: ObjectSchema
)

data class OutputField(
    val name: String,
    val valueSchema: ValueTypeSchema,
    val allowedApiTypes: Set<ApiType>,
    val key: Boolean = false,
    val displayName: String? = null,
    val description: String? = null
)
