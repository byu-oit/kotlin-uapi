package edu.byu.uapidsl.http.implementation

import com.fasterxml.jackson.databind.ObjectMapper
import edu.byu.uapidsl.UApiModel
import edu.byu.uapidsl.dsl.ReadContext
import edu.byu.uapidsl.http.GetHandler
import edu.byu.uapidsl.http.GetRequest
import edu.byu.uapidsl.model.ResourceModel
import edu.byu.uapidsl.types.*

class ResourceGet<AuthContext : Any, IdType : Any, ModelType : Any>(
    apiModel: UApiModel<AuthContext>,
    resource: ResourceModel<AuthContext, IdType, ModelType>,
    jsonMapper: ObjectMapper
) : ResourceBaseHandler<GetRequest, AuthContext, IdType, ModelType, ReadContext<AuthContext, IdType, ModelType>>(
    apiModel, jsonMapper, resource
), GetHandler {

    override val authorizer = resource.read.authorization

    override fun getAuthzContext(
        request: GetRequest,
        authContext: AuthContext,
        id: IdType,
        model: ModelType
    ): ReadContext<AuthContext, IdType, ModelType> = ReadContextImpl(authContext, id, model)

    override fun handleResource(
        request: GetRequest,
        authContext: AuthContext,
        id: IdType,
        model: ModelType
    ): UAPIResponse<*> {
        val props = resource.responseMapper.mapResponse(authContext, id, model)

        val metadata = UAPIResourceMeta(
            ValidationResponse.OK
        )

        return BasicResourceResponse(
            mapOf("basic" to UAPIMapResource(metadata, properties = props)),
            metadata
        )
    }
}



data class ReadContextImpl<AuthContext, IdType, ModelType>(
    override val authContext: AuthContext,
    override val id: IdType,
    override val resource: ModelType
) : ReadContext<AuthContext, IdType, ModelType>
