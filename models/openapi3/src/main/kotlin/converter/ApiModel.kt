package edu.byu.uapidsl.adapters.openapi3.converter

import edu.byu.uapidsl.UApiModel
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import edu.byu.uapidsl.http.*
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import edu.byu.uapidsl.http.stringifyPaths

fun convert(model: UApiModel<*>): OpenAPI {
    return OpenAPI().apply {
        addExtension("x-generated-by", "uapi-openapi3-adapter")

        info = Info()
            .title(model.info.name)
            .version(model.info.version)
            .description(model.info.description)


        val modelPaths = model.httpPaths

        for (modelPath in modelPaths) {
            val pathString = stringifyPaths(modelPath.pathParts)
            val idString = pathString.removePrefix("/").replace("/", "_").replace(":","")

            val (options, get, post, put, patch, delete) = modelPath.handlers

            val pathItem = PathItem()

            // pathItem.options is never null
            pathItem.options = Operation()
            pathItem.options.operationId(idString.plus("_options"))
            println("Swagger - OPTIONS - $pathString")

            if (get != null) {
                pathItem.get = Operation()
                pathItem.get.operationId(idString.plus("_get"))
                println("Swagger GET - $pathString")
            }
            if (post != null) {
                pathItem.post = Operation()
                pathItem.post.operationId(idString.plus("_post"))
                println("Swagger - POST - $pathString")
            }
            if (put != null) {
                pathItem.put = Operation()
                pathItem.put.operationId(idString.plus("_put"))
                println("Swagger - PUT - $pathString")
            }
            if (patch != null) {
                pathItem.patch = Operation()
                pathItem.patch.operationId(idString.plus("_patch"))
                println("Swagger - PATCH - $pathString")
            }
            if (delete != null) {
                pathItem.delete = Operation()
                pathItem.delete.operationId(idString.plus("_delete"))
                println("Swagger - DELETE - $pathString")
            }

            if (paths == null) {
                paths = Paths()
            }
            paths.addPathItem(pathString, pathItem)

        }
    }
}
