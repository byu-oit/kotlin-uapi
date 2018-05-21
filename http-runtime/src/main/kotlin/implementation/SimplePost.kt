package edu.byu.uapidsl.http.implementation

import com.fasterxml.jackson.databind.ObjectMapper
import edu.byu.uapidsl.UApiModel
import edu.byu.uapidsl.dsl.CreateContext
import edu.byu.uapidsl.http.*
import edu.byu.uapidsl.model.CreateOperation
import edu.byu.uapidsl.model.ResourceModel
import edu.byu.uapidsl.types.*
import kotlin.reflect.KClass

class SimplePost<AuthContext: Any, IdType: Any, ModelType: Any, InputType: Any>(
    apiModel: UApiModel<AuthContext>,
    private val resource: ResourceModel<AuthContext, IdType, ModelType>,
    private val create: CreateOperation<AuthContext, IdType, InputType>,
    jsonMapper: ObjectMapper
): BaseHttpHandler<PostRequest, AuthContext>(
    apiModel, jsonMapper
), PostHandler {

    private val authorizer = create.authorization
    private val handler = create.handle
    private val loader = resource.read.handle

    override fun handleAuthenticated(request: PostRequest, authContext: AuthContext): UAPIResponse<*> {
        val body: InputType = request.body.readAs(create.input.type, jsonMapper)
        val context = CreateContextImpl(authContext, body)

        val authorized = context.authorizer()
        if (!authorized) {
            throw NotAuthorizedToViewException()
        }

        val createdId = context.handler()

        val model = ReadLoadContextImpl(authContext, createdId).loader()!!

        val props = resource.responseMapper.mapResponse(authContext, createdId, model)

        val metadata = UAPIResourceMeta(
            validationResponse = ValidationResponse(201, "Created")
        )

        return BasicResourceResponse(
            mapOf("basic" to UAPIMapResource(metadata, properties = props)),
            metadata
        )
    }

}

fun <Type: Any> RequestBody.readAs(type: KClass<Type>, jsonMapper: ObjectMapper): Type {
    return when(this) {
        is StringRequestBody -> jsonMapper.readValue(this.body, type.java)
        else -> throw IllegalStateException("Unable to deserialize body")
    }
}

data class CreateContextImpl<AuthContext, InputType> (
    override val authContext: AuthContext,
    override val input: InputType
): CreateContext<AuthContext, InputType>

