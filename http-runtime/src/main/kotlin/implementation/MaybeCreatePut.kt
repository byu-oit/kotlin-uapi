package edu.byu.uapidsl.http.implementation

import com.fasterxml.jackson.databind.ObjectWriter
import edu.byu.uapidsl.UApiModel
import edu.byu.uapidsl.dsl.CreateOrUpdateContext
import edu.byu.uapidsl.http.NotAuthorizedToViewException
import edu.byu.uapidsl.http.PathParams
import edu.byu.uapidsl.http.PutHandler
import edu.byu.uapidsl.http.PutRequest
import edu.byu.uapidsl.model.resource.CreateOrUpdateOperation
import edu.byu.uapidsl.model.resource.ResourceModel
import edu.byu.uapidsl.types.*

class MaybeCreatePut<AuthContext : Any, IdType : Any, ModelType : Any, InputType : Any>(
    apiModel: UApiModel<AuthContext>,
    private val resource: ResourceModel<AuthContext, IdType, ModelType>,
    private val operation: CreateOrUpdateOperation<AuthContext, IdType, ModelType, InputType>,
    jsonWriter: ObjectWriter
) : BaseHttpHandler<PutRequest, AuthContext>(
    apiModel,
    jsonWriter
), PutHandler {

    private val authorizer = operation.authorization
    private val handler = operation.handle
    private val loader = resource.operations.read.handle
    private val loadAuthorizer = resource.operations.read.authorization

    override fun handleAuthenticated(request: PutRequest, authContext: AuthContext): UAPIResponse<*> {
        val body: InputType = request.body.readWith(operation.input.reader)

        val id = idFrom(request.path)

        val model: ModelType? = maybeLoadModel(id, authContext)

        val requestContext = CreateOrUpdateContextImpl(authContext, id, body, model)

        val authorized = requestContext.authorizer()
        if (!authorized) {
            throw NotAuthorizedToViewException()
        }

        requestContext.handler()

        val updated = maybeLoadModel(id, authContext)!! //TODO(Make this not have to re-authorize everything)

        val metadata = UAPIResourceMeta(
            validationResponse = ValidationResponse(200, "Updated")
        )

        return SimpleResourceResponse(
            mapOf("basic" to UAPISimpleResource(metadata, properties = updated)),
            metadata
        )
    }

    private fun idFrom(pathParams: PathParams): IdType {
        return this.resource.idModel.reader.read(pathParams)
    }

    private fun maybeLoadModel(id: IdType, authContext: AuthContext): ModelType? {
        val loadContext = ReadLoadContextImpl(authContext, id)

        val model = loadContext.loader()

        if (model != null) {
            val authorized = loadAuthorizer.invoke(ReadContextImpl(authContext, id, model))

            if (!authorized) {
                throw NotAuthorizedToViewException()
            }
        }

        return model
    }

}

data class CreateOrUpdateContextImpl<AuthContext, IdType, ModelType, InputType>(
    override val authContext: AuthContext,
    override val id: IdType,
    override val input: InputType,
    override val resource: ModelType?
): CreateOrUpdateContext<AuthContext, IdType, ModelType, InputType>
