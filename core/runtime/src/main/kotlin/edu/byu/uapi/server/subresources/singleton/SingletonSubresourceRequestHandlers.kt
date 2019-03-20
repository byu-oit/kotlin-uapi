package edu.byu.uapi.server.subresources.singleton

import edu.byu.uapi.server.subresources.ParentResult
import edu.byu.uapi.server.subresources.RequestedFieldsetResponse
import edu.byu.uapi.server.subresources.SingletonSubresourceRuntime
import edu.byu.uapi.server.subresources.SubresourceRequestContext
import edu.byu.uapi.server.types.*
import edu.byu.uapi.server.util.debug
import edu.byu.uapi.server.util.info
import edu.byu.uapi.server.util.loggerFor
import edu.byu.uapi.server.util.warn
import edu.byu.uapi.utility.takeIfType

sealed class SingletonSubresourceRequestHandler<UserContext : Any, Parent : ModelHolder, Model : Any, Request : SingletonSubresourceRequest<UserContext>>(
) {
    abstract val runtime: SingletonSubresourceRuntime<UserContext, Parent, Model>
    val subresource: SingletonSubresource<UserContext, Parent, Model>
        get() = runtime.subresource

    fun handle(request: Request): UAPIResponse<*> {
        val fieldsets = when (val r = runtime.parent.getRequestedFieldsets(request.requestContext.fieldsets)) {
            is RequestedFieldsetResponse.Success -> r.fieldsets
            RequestedFieldsetResponse.InvalidFieldsets -> return UAPIBadRequestError("Requested one or more invalid fieldsets.")
            RequestedFieldsetResponse.InvalidContexts -> return UAPIBadRequestError("Requested one or more invalid contexts.")
        }
        val context = SubresourceRequestContext.Simple(fieldsets + runtime.fieldsetName)
        return when (val parent = runtime.parent.getParentModel(context, request.userContext, request.parentIdParams)) {
            is ParentResult.Success -> handle(request, context, parent.value)
            ParentResult.DoesNotExist -> UAPINotFoundError
            ParentResult.NotAuthorized -> UAPINotAuthorizedError
        }
    }

    abstract fun handle(
        request: Request,
        requestContext: SubresourceRequestContext,
        parent: Parent
    ): UAPIResponse<*>

    internal fun buildResponse(
        user: UserContext,
        parent: Parent,
        model: Model,
        validationResponse: ValidationResponse = ValidationResponse.OK
    ): UAPIPropertiesResponse {
        return runtime.buildResponse(user, parent, model, validationResponse)
    }
}

class SingletonSubresourceFetchHandler<UserContext : Any, Parent : ModelHolder, Model : Any>(
    override val runtime: SingletonSubresourceRuntime<UserContext, Parent, Model>
) : SingletonSubresourceRequestHandler<UserContext, Parent, Model, SingletonSubresourceRequest.Fetch<UserContext>>() {
    override fun handle(
        request: SingletonSubresourceRequest.Fetch<UserContext>,
        requestContext: SubresourceRequestContext,
        parent: Parent
    ): UAPIResponse<*> {
        return runtime.handleBasicFetch(request.requestContext, requestContext, request.userContext, parent)
    }
}

class SingletonSubresourceDeleteHandler<UserContext : Any, Parent : ModelHolder, Model : Any>(
    override val runtime: SingletonSubresourceRuntime<UserContext, Parent, Model>,
    val mutation: SingletonSubresource.Deletable<UserContext, Parent, Model>
) : SingletonSubresourceRequestHandler<UserContext, Parent, Model, SingletonSubresourceRequest.Delete<UserContext>>() {
    companion object {
        private val LOG = loggerFor<SingletonSubresourceDeleteHandler<*, *, *>>()
    }

    override fun handle(
        request: SingletonSubresourceRequest.Delete<UserContext>,
        requestContext: SubresourceRequestContext,
        parent: Parent
    ): UAPIResponse<*> {
        val user = request.userContext
        val model = subresource.loadModel(requestContext, user, parent)

        return when (val result = doDelete(requestContext, user, parent, model)) {
            DeleteResult.Success -> {
                LOG.info("Successfully deleted ${subresource.name}")
                UAPIEmptyResponse
            }
            DeleteResult.AlreadyDeleted -> {
                LOG.info("${subresource.name} has already been deleted; returning success")
                UAPIEmptyResponse
            }
            DeleteResult.Unauthorized -> {
                LOG.warn("NotAuthorized request to delete ${subresource.name}! User contexts was $user")
                UAPINotAuthorizedError
            }
            is DeleteResult.CannotBeDeleted -> {
                LOG.warn("${subresource.name} cannot be deleted: ${result.reason}")
                GenericUAPIErrorResponse(
                    statusCode = 409,
                    message = "Conflict",
                    validationInformation = listOf(result.reason)

                )
            }
            is DeleteResult.Error -> {
                LOG.error("Unexpected error deleting ${subresource.name}: ${result.code} ${result.errors.joinToString()}", result.cause)
                GenericUAPIErrorResponse(
                    statusCode = result.code,
                    message = "Error",
                    validationInformation = result.errors
                )
            }
        }
    }

    private fun doDelete(
        requestContext: SubresourceRequestContext,
        user: UserContext,
        parent: Parent,
        model: Model?
    ): DeleteResult {
        if (model == null) {
            return DeleteResult.AlreadyDeleted
        }
        if (!mutation.canUserDelete(requestContext, user, parent, model)) {
            return DeleteResult.Unauthorized
        }
        if (!mutation.canBeDeleted(parent, model)) {
            return DeleteResult.CannotBeDeleted("Cannot be deleted")
        }
        return mutation.handleDelete(requestContext, user, parent, model)
    }
}

class SingletonSubresourceUpdateHandler<UserContext : Any, Parent: ModelHolder, Model : Any, Input : Any>(
    override val runtime: SingletonSubresourceRuntime<UserContext, Parent, Model>,
    private val operation: SingletonSubresource.Updatable<UserContext, Parent, Model, Input>
) : SingletonSubresourceRequestHandler<UserContext, Parent, Model, SingletonSubresourceRequest.Update<UserContext>>() {

    companion object {
        private val LOG = loggerFor<SingletonSubresourceUpdateHandler<*, *, *, *>>()
    }

    private val inputType = operation.updateInput
    private val createWithId = operation.takeIfType<SingletonSubresource.Creatable<UserContext, Parent, Model, Input>>()
    private val validator = operation.getUpdateValidator(runtime.validationEngine)

    override fun handle(
        request: SingletonSubresourceRequest.Update<UserContext>,
        requestContext: SubresourceRequestContext,
        parent: Parent
    ): UAPIResponse<*> {
        LOG.debug { "Got request to update ${subresource.name}" }
        val userContext = request.userContext

        val input = request.body.readAs(inputType)
        val model = subresource.loadModel(requestContext, userContext, parent)

        return when {
            model != null -> {
                val result = doUpdate(requestContext, userContext, parent, model, input)
                handleUpdateResult(requestContext, userContext, parent, result)
            }
            createWithId != null -> {
                val result = doCreate(createWithId, requestContext, userContext, parent, input)
                handleCreateResult(requestContext, userContext, parent, result)
            }
            else -> UAPINotFoundError
        }
    }

    private fun handleUpdateResult(
        requestContext: SubresourceRequestContext,
        userContext: UserContext,
        parent: Parent,
        result: UpdateResult<Model>
    ): UAPIResponse<*> {
        return when (result) {
            is UpdateResult.Success -> {
                LOG.info { "Successfully updated ${subresource.name}" }
                buildResponse(userContext, parent, result.model)
            }
            is UpdateResult.InvalidInput -> {
                LOG.warn { "Invalid update ${subresource.name} request body: ${result.errors.map { "${it.field}: ${it.description}" }}" }
                GenericUAPIErrorResponse(
                    400, "Bad Request", result.errors.map { "The value for ${it.field} is invalid: ${it.description}" }
                )
            }
            UpdateResult.Unauthorized -> {
                LOG.warn { "NotAuthorized request to update ${subresource.name}! User Context was $userContext" }
                UAPINotAuthorizedError
            }
            is UpdateResult.CannotBeUpdated -> {
                LOG.warn { "Got request to update ${subresource.name}, but updates are not allowed: ${result.reason}" }
                GenericUAPIErrorResponse(
                    statusCode = 409,
                    message = "Conflict",
                    validationInformation = listOf(result.reason)
                )
            }
            is UpdateResult.Error -> {
                LOG.warn("Unknown error udpating ${subresource.name}: code ${result.code} ${result.errors.joinToString()}", result.cause)
                GenericUAPIErrorResponse(
                    statusCode = result.code,
                    message = "Error",
                    validationInformation = result.errors
                )
            }
        }
    }

    private fun doUpdate(
        requestContext: SubresourceRequestContext,
        userContext: UserContext,
        parent: Parent,
        model: Model,
        input: Input
    ): UpdateResult<Model> {
        val authorized = operation.canUserUpdate(requestContext, userContext, parent, model)
        if (!authorized) {
            return UpdateResult.Unauthorized
        }

        val canBeUpdated = operation.canBeUpdated(parent, model)
        if (!canBeUpdated) {
            return UpdateResult.CannotBeUpdated("cannot be updated at this time.")
        }

        val validationResponse = validator.validate(input)
        if (validationResponse.isNotEmpty()) {
            return UpdateResult.InvalidInput(validationResponse.map { InputError(it.field, it.should) })
        }
        return operation.handleUpdate(requestContext, userContext, parent, model, input)
    }

    private fun doCreate(
        operation: SingletonSubresource.Creatable<UserContext, Parent, Model, Input>,
        requestContext: SubresourceRequestContext,
        userContext: UserContext,
        parent: Parent,
        input: Input
    ): CreateResult<Model> {
        val authorized = operation.canUserCreate(requestContext, userContext, parent)
        if (!authorized) {
            return CreateResult.Unauthorized
        }

        val validationResponse = validator.validate(input)
        if (validationResponse.isNotEmpty()) {
            return CreateResult.InvalidInput(validationResponse.map { InputError(it.field, it.should) })
        }
        return operation.handleCreate(requestContext, userContext, parent, input)
    }

    private fun handleCreateResult(
        requestContext: SubresourceRequestContext,
        userContext: UserContext,
        parent: Parent,
        result: CreateResult<Model>
    ): UAPIResponse<*> {
        return when (result) {
            is CreateResult.Success -> {
                LOG.info { "Successfully created ${subresource.name}" }
                buildResponse(userContext, parent, result.model)
            }
            CreateResult.Unauthorized -> {
                LOG.warn { "NotAuthorized request to create ${subresource.name}! User Context was $userContext" }
                UAPINotAuthorizedError
            }
            is CreateResult.InvalidInput -> {
                LOG.warn { "Invalid create ${subresource.name} request body: ${result.errors.map { "${it.field}: ${it.description}" }}" }
                UAPIBadRequestError(
                    result.errors.map { "The value for ${it.field} is invalid: ${it.description}" }
                )
            }
            is CreateResult.Error -> {
                LOG.error("Error(s) creating ${subresource.name}: ${result.errors.joinToString()}", result.cause)
                GenericUAPIErrorResponse(
                    result.code, "Error", result.errors
                )
            }
        }
    }
}
