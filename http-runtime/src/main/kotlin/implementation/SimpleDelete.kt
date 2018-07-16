package edu.byu.uapidsl.http.implementation

import com.fasterxml.jackson.databind.ObjectWriter
import edu.byu.uapidsl.UApiModel
import edu.byu.uapidsl.dsl.DeleteContext
import edu.byu.uapidsl.http.DeleteHandler
import edu.byu.uapidsl.http.DeleteRequest
import edu.byu.uapidsl.model.resource.DeleteOperation
import edu.byu.uapidsl.model.resource.ResourceModel
import edu.byu.uapidsl.types.UAPIEmptyResponse
import edu.byu.uapidsl.types.UAPIResponse

class SimpleDelete<AuthContext: Any, IdType: Any, ModelType: Any>(
    apiModel: UApiModel<AuthContext>,
    resource: ResourceModel<AuthContext, IdType, ModelType>,
    private val operation: DeleteOperation<AuthContext, IdType, ModelType>,
    jsonWriter: ObjectWriter
): ResourceBaseHandler<DeleteRequest, AuthContext, IdType, ModelType, DeleteContext<AuthContext, IdType, ModelType>>(
    apiModel, jsonWriter, resource
), DeleteHandler {

    override val authorizer = operation.authorization
    private val handler = operation.handle

    override fun createRequestContext(
        request: DeleteRequest,
        authContext: AuthContext,
        id: IdType,
        model: ModelType
    ): DeleteContext<AuthContext, IdType, ModelType> {
        return DeleteContextImpl(authContext, id, model)
    }

    override fun handleResource(
        request: DeleteRequest,
        authContext: AuthContext,
        id: IdType,
        model: ModelType,
        requestContext: DeleteContext<AuthContext, IdType, ModelType>
    ): UAPIResponse<*> {
        requestContext.handler()

        return UAPIEmptyResponse
    }
}

data class DeleteContextImpl<AuthContext, IdType, ResourceModel> (
    override val authContext: AuthContext,
    override val id: IdType,
    override val resource: ResourceModel
): DeleteContext<AuthContext, IdType, ResourceModel>
