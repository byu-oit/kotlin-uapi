package edu.byu.uapidsl.adapters.openapi3.converter

import edu.byu.uapidsl.model.ResourceModel
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse

data class OpenAPIResourceInfo(
    val schemas: Map<String, Schema<*>>,
    val apiResponses: Map<String, ApiResponse>,
    val examples: Map<String, Example>,
    val requestBodies: Map<String, RequestBody>,
    val paths: Map<String, PathItem>
)

fun resourceInfo(model: ResourceModel<*, *, *>): OpenAPIResourceInfo {
    val schemas: MutableMap<String, Schema<*>> = mutableMapOf()
    val apiResponses: MutableMap<String, ApiResponse> = mutableMapOf()
    val examples: MutableMap<String, Example> = mutableMapOf()
    val requestBodies: MutableMap<String, RequestBody> = mutableMapOf()
    val paths: MutableMap<String, PathItem> = mutableMapOf()

//    model.apply {
//        val prefix = "${name}__"
//
////        schemas[prefix + "model"] = transform.type
//    }

    TODO()
}

