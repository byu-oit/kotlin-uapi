package edu.byu.uapidsl.adapters.openapi3.converter

import edu.byu.uapidsl.UApiModel
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info

fun convert(model: UApiModel<*>): OpenAPI {
    return OpenAPI().apply {
        addExtension("x-generated-by", "uapi-openapi3-adapter")

        info = Info()
            .title(model.info.name)
            .version(model.info.version)
            .description(model.info.description)


    }
}
