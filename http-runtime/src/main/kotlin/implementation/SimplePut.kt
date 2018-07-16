package edu.byu.uapidsl.http.implementation

import com.fasterxml.jackson.databind.ObjectWriter
import edu.byu.uapidsl.UApiModel
import edu.byu.uapidsl.dsl.UpdateContext
import edu.byu.uapidsl.http.PutHandler
import edu.byu.uapidsl.http.PutRequest
import edu.byu.uapidsl.model.resource.ResourceModel
import edu.byu.uapidsl.model.resource.SimpleUpdateOperation
import edu.byu.uapidsl.types.*

class SimplePut<AuthContext: Any, IdType: Any, ModelType: Any, InputType: Any>(
    apiModel: UApiModel<AuthContext>,
    resource: ResourceModel<AuthContext, IdType, ModelType>,
    private val update: SimpleUpdateOperation<AuthContext, IdType, ModelType, InputType>,
    jsonWriter: ObjectWriter
): ResourceBaseHandler<PutRequest, AuthContext, IdType, ModelType, UpdateContext<AuthContext, IdType, ModelType, InputType>>(
    apiModel, jsonWriter, resource
), PutHandler {

    override val authorizer = update.authorization

    private val handler = update.handle

    override fun createRequestContext(
        request: PutRequest,
        authContext: AuthContext,
        id: IdType,
        model: ModelType
    ): UpdateContext<AuthContext, IdType, ModelType, InputType> {
        return UpdateContextImpl(authContext, id, model, request.body.readWith(update.input.reader))
    }

    override fun handleResource(
        request: PutRequest,
        authContext: AuthContext,
        id: IdType,
        model: ModelType,
        requestContext: UpdateContext<AuthContext, IdType, ModelType, InputType>
    ): UAPIResponse<*> {
        requestContext.handler()

        val updatedModel = loadModel(id, authContext)

        val metadata = UAPIResourceMeta(
            validationResponse = ValidationResponse(200, "Updated")
        )

        return SimpleResourceResponse(
            mapOf("basic" to UAPISimpleResource(metadata, properties = updatedModel)),
            metadata
        )
    }
}


data class UpdateContextImpl<AuthContext, IdType, ResourceModel, InputType> (
    override val authContext: AuthContext,
    override val id: IdType,
    override val resource: ResourceModel,
    override val input: InputType
): UpdateContext<AuthContext, IdType, ResourceModel, InputType>
