package edu.byu.uapidsl.model.resource.ops

import edu.byu.uapidsl.dsl.*
import edu.byu.uapidsl.model.resource.DomainModelOps
import edu.byu.uapidsl.model.resource.InputModel
import edu.byu.uapidsl.model.resource.UpdateResourceRequest
import edu.byu.uapidsl.types.ErrorResponse
import edu.byu.uapidsl.types.UAPIResponse
import either.Either
import either.Left
import either.Right

sealed class UpdateOperation<AuthContext, IdType, DomainType, InputType : Any, RequestContext> {
    abstract val input: InputModel<InputType>
    abstract val authorization: RequestContext.() -> Boolean
    abstract val handle: RequestContext.() -> Unit

    fun handleRequest(
        request: UpdateResourceRequest<AuthContext, IdType, InputType>,
        modelOps: DomainModelOps<AuthContext, IdType, DomainType>
    ): UAPIResponse<*> {
        val auth = request.authContext
        val body = request.input
        val id = request.id
        val model = modelOps.idToModel(auth, id)

        val contextResult = createRequestContext(auth, id, model, body)

        val context: RequestContext = when(contextResult) {
            is Left -> contextResult.value
            is Right -> return@handleRequest contextResult.value
        }

        val authorized = authorization.invoke(context)
        if (!authorized) {
            return ErrorResponse.notAuthorized()
        }

        handle.invoke(context)

        //Load the created/updated resource again
        return modelOps.idToResult(auth, id)
    }

    abstract fun createRequestContext(
        authContext: AuthContext,
        id: IdType,
        model: DomainType?,
        body: InputType
    ): Either<RequestContext, UAPIResponse<*>>
}

data class SimpleUpdateOperation<AuthContext, IdType, DomainType, InputType : Any>(
    override val input: InputModel<InputType>,
    override val authorization: UpdateAuthorizer<AuthContext, IdType, DomainType, InputType>,
    override val handle: UpdateHandler<AuthContext, IdType, DomainType, InputType>
) : UpdateOperation<AuthContext, IdType, DomainType, InputType,
    UpdateContext<AuthContext, IdType, DomainType, InputType>>() {
    override fun createRequestContext(
        authContext: AuthContext,
        id: IdType,
        model: DomainType?,
        body: InputType
    ): Either<UpdateContext<AuthContext, IdType, DomainType, InputType>, UAPIResponse<*>> {
        if (model == null) {
            return Right(ErrorResponse.notFound())
        }
        return Left(UpdateContextImpl(authContext, id, body, model))
    }
}

internal data class CreateOrUpdateContextImpl<AuthContext, IdType, ModelType, InputType>(
    override val authContext: AuthContext,
    override val id: IdType,
    override val input: InputType,
    override val resource: ModelType?
): CreateOrUpdateContext<AuthContext, IdType, ModelType, InputType>

data class CreateOrUpdateOperation<AuthContext, IdType, DomainType, InputType : Any>(
    override val input: InputModel<InputType>,
    override val authorization: CreateOrUpdateAuthorizer<AuthContext, IdType, DomainType, InputType>,
    override val handle: CreateOrUpdateHandler<AuthContext, IdType, DomainType, InputType>
) : UpdateOperation<AuthContext, IdType, DomainType, InputType,
    CreateOrUpdateContext<AuthContext, IdType, DomainType, InputType>>() {

    override fun createRequestContext(
        authContext: AuthContext,
        id: IdType,
        model: DomainType?,
        body: InputType
    ): Either<CreateOrUpdateContext<AuthContext, IdType, DomainType, InputType>, UAPIResponse<*>> {
        return Left(CreateOrUpdateContextImpl(authContext, id, body, model))
    }
}

internal data class UpdateContextImpl<AuthContext, IdType, ModelType, InputType>(
    override val authContext: AuthContext,
    override val id: IdType,
    override val input: InputType,
    override val resource: ModelType
): UpdateContext<AuthContext, IdType, ModelType, InputType>

