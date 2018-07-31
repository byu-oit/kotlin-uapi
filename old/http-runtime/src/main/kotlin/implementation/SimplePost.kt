package edu.byu.uapidsl.http.implementation

import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.databind.ObjectWriter
import edu.byu.uapidsl.UApiModel
import edu.byu.uapidsl.http.*
import edu.byu.uapidsl.model.resource.identified.CreateResourceRequest
import edu.byu.uapidsl.model.resource.identified.ops.CreateOperation
import edu.byu.uapidsl.model.resource.identified.IdentifiedResource
import edu.byu.uapidsl.types.*

class SimplePost<AuthContext: Any, IdType: Any, ModelType: Any, InputType: Any>(
    apiModel: UApiModel<AuthContext>,
    private val resource: IdentifiedResource<AuthContext, IdType, ModelType>,
    private val create: CreateOperation<AuthContext, IdType, InputType>,
    jsonMapper: ObjectWriter
): BaseHttpHandler<PostRequest, AuthContext>(
    apiModel, jsonMapper
), PostHandler {

    override fun handleAuthenticated(request: PostRequest, authContext: AuthContext): UAPIResponse<*> {
        val body: InputType = request.body.readWith(create.input.reader)

        val response = resource.handleCreateRequest(
            CreateResourceRequest(authContext, body)
        )

        if (!response.metadata.validationResponse.successful) {
            return response
        }

        val metadata = UAPIResourceMeta(
            validationResponse = ValidationResponse(201, "Created")
        )

        return SimpleResourceResponse(
            mapOf("basic" to response),
            metadata
        )
    }
}

fun <Type: Any> RequestBody.readWith(reader: ObjectReader): Type {
    return when(this) {
        is StringRequestBody -> reader.readValue(this.body)
        else -> throw IllegalStateException("Unable to deserialize body")
    }
}

