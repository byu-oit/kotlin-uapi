package edu.byu.uapidsl.adapters.openapi3

import com.fasterxml.jackson.databind.ObjectMapper
import edu.byu.uapidsl.UApiModel
import edu.byu.uapidsl.adapters.openapi3.converter.convert
import edu.byu.uapidsl.types.jackson.JacksonUAPITypesModule
import io.swagger.v3.core.jackson.SwaggerModule
import io.swagger.v3.oas.models.OpenAPI

fun UApiModel<*>.toOpenApi3Model(): OpenAPI = convert(this)

fun UApiModel<*>.toOpenApi3Json(): String {
    val mapper = ObjectMapper()
    mapper.registerModule(JacksonUAPITypesModule())
    mapper.registerModule(SwaggerModule())
    return mapper.writeValueAsString(this.toOpenApi3Model())
}
