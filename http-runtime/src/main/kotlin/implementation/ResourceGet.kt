package edu.byu.uapidsl.http.implementation

import com.fasterxml.jackson.databind.ObjectWriter
import edu.byu.uapidsl.UApiModel
import edu.byu.uapidsl.dsl.ReadContext
import edu.byu.uapidsl.http.GetHandler
import edu.byu.uapidsl.http.GetRequest
import edu.byu.uapidsl.model.ResourceModel
import edu.byu.uapidsl.types.*

class ResourceGet<AuthContext : Any, IdType : Any, ModelType : Any>(
    apiModel: UApiModel<AuthContext>,
    resource: ResourceModel<AuthContext, IdType, ModelType>,
    jsonWriter: ObjectWriter
) : ResourceBaseHandler<GetRequest, AuthContext, IdType, ModelType, ReadContext<AuthContext, IdType, ModelType>>(
    apiModel, jsonWriter, resource
), GetHandler {

    override val authorizer = resource.operations.read.authorization

    override fun createRequestContext(
        request: GetRequest,
        authContext: AuthContext,
        id: IdType,
        model: ModelType
    ): ReadContext<AuthContext, IdType, ModelType> = ReadContextImpl(authContext, id, model)

    override fun handleResource(
        request: GetRequest,
        authContext: AuthContext,
        id: IdType,
        model: ModelType,
        requestContext: ReadContext<AuthContext, IdType, ModelType>
    ): UAPIResponse<*> {
        val metadata = UAPIResourceMeta(
            ValidationResponse.OK
        )

        return SimpleResourceResponse(
            mapOf("basic" to UAPISimpleResource(metadata, properties = model)),
            // TODO(Add other fieldsets and contexts)
            metadata
        )
    }
}

data class ReadContextImpl<AuthContext, IdType, ModelType>(
    override val authContext: AuthContext,
    override val id: IdType,
    override val resource: ModelType
) : ReadContext<AuthContext, IdType, ModelType>
