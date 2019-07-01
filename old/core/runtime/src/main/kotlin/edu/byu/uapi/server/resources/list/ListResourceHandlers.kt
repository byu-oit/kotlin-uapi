package edu.byu.uapi.server.resources.list

import edu.byu.uapi.server.resources.ResourceRequestContext
import edu.byu.uapi.server.subresources.RequestedFieldsetResponse
import edu.byu.uapi.server.subresources.SubresourceRequestContext
import edu.byu.uapi.server.types.*
import edu.byu.uapi.server.util.debug
import edu.byu.uapi.server.util.info
import edu.byu.uapi.server.util.loggerFor
import edu.byu.uapi.server.util.warn
import edu.byu.uapi.spi.SpecConstants
import edu.byu.uapi.spi.input.ListParamReader
import edu.byu.uapi.spi.input.ListParams
import edu.byu.uapi.spi.input.ListWithTotal
import edu.byu.uapi.spi.requests.*
import edu.byu.uapi.utility.takeIfType

sealed class ListResourceRequestHandler<UserContext : Any, Id : Any, Model : Any, Params : ListParams, Request : ListResourceRequest<UserContext>>(
    val runtime: ListResourceRuntime<UserContext, Id, Model, Params>
) {
    val resource = runtime.resource

    internal fun modelToBasic(
        userContext: UserContext,
        model: Model,
        validationResponse: ValidationResponse = ValidationResponse.OK,
        id: Id = resource.idFromModel(model)
    ): UAPIPropertiesResponse {
        return UAPIPropertiesResponse(
            metadata = UAPIResourceMeta(validationResponse = validationResponse),
            links = generateLinks(userContext, id, model),
            properties = modelToProperties(userContext, id, model)
        )
    }

    internal fun modelToProperties(
        userContext: UserContext,
        id: Id,
        model: Model
    ): Map<String, UAPIProperty> {
        return resource.responseFields.map { f ->
            f.name to f.toProp(userContext, model)
        }.toMap()
    }

    @Suppress("UNUSED_PARAMETER")
    internal fun generateLinks(
        userContext: UserContext,
        id: Id,
        model: Model
    ): UAPILinks {
        //TODO generate links
        return emptyMap()
    }

    internal fun buildFieldsetResponse(
        requestContext: RequestContext,
        resourceRequestContext: ResourceRequestContext,
        userContext: UserContext,
        id: Id,
        model: Model
    ): UAPIFieldsetsResponse {
        val loadedFieldsets = loadFieldsets(
            requestContext,
            resourceRequestContext,
            userContext,
            id,
            model,
            resourceRequestContext.requestedSubresources
        )

        return UAPIFieldsetsResponse(
            fieldsets = loadedFieldsets,
            metadata = FieldsetsMetadata(
                fieldSetsReturned = loadedFieldsets.keys,
                fieldSetsAvailable = runtime.availableFieldsets
            )
        )
    }

    internal fun loadFieldsets(
        requestContext: RequestContext,
        resourceRequestContext: ResourceRequestContext,
        userContext: UserContext,
        id: Id,
        model: Model,
        requestedFieldsets: Set<String>
    ): Map<String, UAPIResponse<*>> {
        val fieldsets = requestedFieldsets.ifEmpty { setOf(SpecConstants.FieldSets.VALUE_BASIC) }

        val subresourceRequestContext = SubresourceRequestContext.Simple(requestedFieldsets, resourceRequestContext.attributes)

        return fieldsets.associateWith { loadFieldset(requestContext, subresourceRequestContext, userContext, id, model, it) }
    }

    internal fun loadFieldset(
        requestContext: RequestContext,
        subresourceRequestContext: SubresourceRequestContext,
        userContext: UserContext,
        id: Id,
        model: Model,
        fieldsetName: String
    ): UAPIResponse<*> {
        if (fieldsetName == SpecConstants.FieldSets.VALUE_BASIC) {
            return modelToBasic(
                userContext = userContext, id = id, model = model
            )
        }
        val sub = runtime.subresources[fieldsetName]
            ?: return UAPIBadRequestError("Invalid fieldsets name")
        return sub.handleBasicFetch(requestContext, subresourceRequestContext, userContext, IdentifiedModel.Simple(id, model))
    }

    fun getId(idParams: IdParams): Id {
        return runtime.idReader.read(idParams)
    }

    fun handle(request: Request): UAPIResponse<*> {
        val requestedFieldsets = when (val result = runtime.getRequestedFieldsets(request.requestContext.fieldsets)) {
            is RequestedFieldsetResponse.Success -> result.fieldsets
            RequestedFieldsetResponse.InvalidFieldsets -> return UAPIBadRequestError("Requested one or more invalid fieldsets. Allowed fieldsets are: ${runtime.availableFieldsets}")
            RequestedFieldsetResponse.InvalidContexts -> return UAPIBadRequestError("Requested one or more invalid contexts. Allowed contexts are: ${runtime.availableContexts.keys}.")
        }
        val requestContext = ResourceRequestContext.Simple(requestedFieldsets)
        return handle(request, requestContext)
    }

    abstract fun handle(
        request: Request,
        requestContext: ResourceRequestContext
    ): UAPIResponse<*>
}

class ListResourceFetchHandler<UserContext : Any, Id : Any, Model : Any, Params : ListParams>(
    runtime: ListResourceRuntime<UserContext, Id, Model, Params>
) : ListResourceRequestHandler<UserContext, Id, Model, Params, FetchListResource<UserContext>>(runtime) {
    override fun handle(
        request: FetchListResource<UserContext>,
        requestContext: ResourceRequestContext
    ): UAPIResponse<*> {
        val id = getId(request.idParams)
        val userContext = request.userContext

        val model = resource.loadModel(requestContext, userContext, id) ?: return UAPINotFoundError
        if (!resource.canUserViewModel(requestContext, userContext, id, model)) {
            return UAPINotAuthorizedError
        }
        return buildFieldsetResponse(request.requestContext, requestContext, userContext, id, model)
    }
}

class ListResourceListHandler<UserContext : Any, Id : Any, Model : Any, Params : ListParams>(
    runtime: ListResourceRuntime<UserContext, Id, Model, Params>
) : ListResourceRequestHandler<UserContext, Id, Model, Params, ListListResource<UserContext>>(runtime) {
    private val paramReader: ListParamReader<Params> = resource.getListParamReader(runtime.typeDictionary)

    override fun handle(
        request: ListListResource<UserContext>,
        requestContext: ResourceRequestContext
    ): UAPIResponse<*> {
        val params = paramReader.read(request.queryParams)

        val result = resource.list(requestContext, request.userContext, params)

        val meta = buildCollectionMetadata(result, params)

        return UAPIFieldsetsCollectionResponse(
            result.map { buildFieldsetResponse(request.requestContext, requestContext, request.userContext, resource.idFromModel(it), it) },
            meta,
            emptyMap() //TODO: Links
        )
    }

    private fun buildCollectionMetadata(
        list: List<Model>,
        params: Params
    ): CollectionMetadata {
        val meta = paramReader.describe()

        val size = if (list is ListWithTotal<Model>) list.totalItems else list.size

        val (search, _, sort, subset) = meta
        val searchMeta = search?.let { SearchableCollectionMetadata(it.contextFields) }
        val sortMeta = sort?.let { SortableCollectionMetadata(it.properties, it.defaults, it.defaultSortOrder) }
        val subsetMeta = subset?.let {
            params as ListParams.WithSubset
            CollectionSubsetMetadata(
                list.size,
                params.subset.subsetStartOffset,
                it.defaultSize,
                it.maxSize
            )
        }

        return CollectionMetadata(
            collectionSize = size,
            searchMetadata = searchMeta,
            sortMetadata = sortMeta,
            subsetMetadata = subsetMeta
        )
    }
}

class ListResourceCreateHandler<UserContext : Any, Id : Any, Model : Any, Params : ListParams, Input : Any>(
    runtime: ListResourceRuntime<UserContext, Id, Model, Params>,
    private val operation: ListResource.Creatable<UserContext, Id, Model, Input>
) : ListResourceRequestHandler<UserContext, Id, Model, Params, CreateListResource<UserContext>>(runtime) {

    companion object {
        private val LOG = loggerFor<ListResourceCreateHandler<*, *, *, *, *>>()
    }

    private val inputType = operation.createInput

    override fun handle(
        request: CreateListResource<UserContext>,
        requestContext: ResourceRequestContext
    ): UAPIResponse<*> {
        LOG.debug { "Got request to create ${resource.singleName}" }
        val userContext = request.userContext

        val authorized = operation.canUserCreate(requestContext, userContext)
        if (!authorized) {
            LOG.warn { "NotAuthorized request to create ${resource.singleName}! User Context was $userContext" }
            return UAPINotAuthorizedError
        }
        val input = request.body.readAs(inputType)

        val validator = operation.getCreateValidator(runtime.validationEngine)
        val validationResponse = validator.validate(input)
        if (validationResponse.isNotEmpty()) {
            LOG.warn { "Invalid create ${resource.singleName} request body: ${validationResponse.map { "${it.field}: ${it.should}" }}" }
            return GenericUAPIErrorResponse(
                statusCode = 400,
                message = "Bad Request",
                validationInformation = validationResponse.map { "The value for ${it.field} is invalid: ${it.should}" }
            )
        }
        return when (val result = operation.handleCreate(requestContext, userContext, input)) {
            is CreateResult.Success -> {
                val model = result.model
                val id = resource.idFromModel(model)
                LOG.info { "Successfully created ${resource.singleName} $id" }
                super.modelToBasic(
                    userContext = userContext,
                    id = id,
                    model = model,
                    validationResponse = ValidationResponse(201, "Created")
                )
            }
            CreateResult.Unauthorized -> {
                LOG.warn { "NotAuthorized request to create ${resource.singleName} (caught in handleCreate)! User Context was $userContext" }
                UAPINotAuthorizedError
            }
            is CreateResult.InvalidInput -> {
                LOG.warn { "Invalid create ${resource.singleName} request body: ${result.errors.map { "${it.field}: ${it.description}" }}" }
                GenericUAPIErrorResponse(
                    400, "Bad Request", result.errors.map { "The value for ${it.field} is invalid: ${it.description}" }
                )
            }
            is CreateResult.Error -> {
                LOG.error("Error(s) creating ${resource.singleName}: ${result.errors.joinToString()}", result.cause)
                GenericUAPIErrorResponse(
                    result.code, "Error", result.errors
                )
            }
        }
    }
}

class ListResourceUpdateHandler<UserContext : Any, Id : Any, Model : Any, Params : ListParams, Input : Any>(
    runtime: ListResourceRuntime<UserContext, Id, Model, Params>,
    private val operation: ListResource.Updatable<UserContext, Id, Model, Input>
) : ListResourceRequestHandler<UserContext, Id, Model, Params, UpdateListResource<UserContext>>(runtime) {

    companion object {
        private val LOG = loggerFor<ListResourceCreateHandler<*, *, *, *, *>>()
    }

    private val inputType = operation.updateInput
    private val createWithId = operation.takeIfType<ListResource.CreatableWithId<UserContext, Id, Model, Input>>()
    private val validator = operation.getUpdateValidator(runtime.validationEngine)

    override fun handle(
        request: UpdateListResource<UserContext>,
        requestContext: ResourceRequestContext
    ): UAPIResponse<*> {
        LOG.debug { "Got request to update ${resource.singleName}" }
        val userContext = request.userContext

        val id = getId(request.idParams)
        val input = request.body.readAs(inputType)

        val model = resource.loadModel(requestContext, userContext, id)

        return when {
            model != null -> {
                val result = doUpdate(requestContext, userContext, id, model, input)
                handleUpdateResult(userContext, id, result)
            }
            createWithId != null -> {
                val result = doCreate(createWithId, requestContext, userContext, id, input)
                handleCreateResult(userContext, id, result)
            }
            else -> UAPINotFoundError
        }
    }

    private fun handleUpdateResult(
        userContext: UserContext,
        id: Id,
        result: UpdateResult<Model>
    ): UAPIResponse<*> {
        return when (result) {
            is UpdateResult.Success -> {
                val model = result.model
                LOG.info { "Successfully updated ${resource.singleName} $id" }
                super.modelToBasic(
                    userContext = userContext,
                    id = id,
                    model = model,
                    validationResponse = ValidationResponse(200, "OK")
                )
            }
            is UpdateResult.InvalidInput -> {
                LOG.warn { "Invalid update ${resource.singleName} $id request body: ${result.errors.map { "${it.field}: ${it.description}" }}" }
                GenericUAPIErrorResponse(
                    400, "Bad Request", result.errors.map { "The value for ${it.field} is invalid: ${it.description}" }
                )
            }
            UpdateResult.Unauthorized -> {
                LOG.warn { "NotAuthorized request to update ${resource.singleName} $id! User Context was $userContext" }
                UAPINotAuthorizedError
            }
            is UpdateResult.CannotBeUpdated -> {
                LOG.warn { "Got request to update ${resource.singleName} $id, but updates are not allowed: ${result.reason}" }
                GenericUAPIErrorResponse(
                    statusCode = 409,
                    message = "Conflict",
                    validationInformation = listOf(result.reason)
                )
            }
            is UpdateResult.Error -> {
                LOG.warn("Unknown error updating ${resource.singleName} $id: code ${result.code} ${result.errors.joinToString()}", result.cause)
                GenericUAPIErrorResponse(
                    statusCode = result.code,
                    message = "Error",
                    validationInformation = result.errors
                )
            }
        }
    }

    private fun doUpdate(
        requestContext: ResourceRequestContext,
        userContext: UserContext,
        id: Id,
        model: Model,
        input: Input
    ): UpdateResult<Model> {
        val authorized = operation.canUserUpdate(requestContext, userContext, id, model)
        if (!authorized) {
            return UpdateResult.Unauthorized
        }

        val canBeUpdated = operation.canBeUpdated(id, model)
        if (!canBeUpdated) {
            return UpdateResult.CannotBeUpdated("cannot be updated at this time.")
        }

        val validationResponse = validator.validate(input)
        if (validationResponse.isNotEmpty()) {
            return UpdateResult.InvalidInput(validationResponse.map { InputError(it.field, it.should) })
        }
        return operation.handleUpdate(requestContext, userContext, id, model, input)
    }

    private fun doCreate(
        operation: ListResource.CreatableWithId<UserContext, Id, Model, Input>,
        requestContext: ResourceRequestContext,
        userContext: UserContext,
        id: Id,
        input: Input
    ): CreateResult<Model> {
        val authorized = operation.canUserCreateWithId(requestContext, userContext, id)
        if (!authorized) {
            return CreateResult.Unauthorized
        }

        val validationResponse = validator.validate(input)
        if (validationResponse.isNotEmpty()) {
            return CreateResult.InvalidInput(validationResponse.map { InputError(it.field, it.should) })
        }
        return operation.handleCreateWithId(requestContext, userContext, id, input)
    }

    private fun handleCreateResult(
        userContext: UserContext,
        id: Id,
        result: CreateResult<Model>
    ): UAPIResponse<*> {
        return when (result) {
            is CreateResult.Success -> {
                LOG.info { "Successfully created ${resource.singleName} $id" }
                super.modelToBasic(
                    userContext = userContext,
                    id = id,
                    model = result.model,
                    validationResponse = ValidationResponse(201, "Created")
                )
            }
            CreateResult.Unauthorized -> {
                LOG.warn { "NotAuthorized request to create ${resource.singleName} $id! User Context was $userContext" }
                UAPINotAuthorizedError
            }
            is CreateResult.InvalidInput -> {
                LOG.warn { "Invalid create ${resource.singleName} $id request body: ${result.errors.map { "${it.field}: ${it.description}" }}" }
                UAPIBadRequestError(
                    result.errors.map { "The value for ${it.field} is invalid: ${it.description}" }
                )
            }
            is CreateResult.Error -> {
                LOG.error("Error(s) creating ${resource.singleName} $id: ${result.errors.joinToString()}", result.cause)
                GenericUAPIErrorResponse(
                    result.code, "Error", result.errors
                )
            }
        }
    }
}

class ListResourceDeleteHandler<UserContext : Any, Id : Any, Model : Any, Params : ListParams>(
    runtime: ListResourceRuntime<UserContext, Id, Model, Params>,
    val operation: ListResource.Deletable<UserContext, Id, Model>
) : ListResourceRequestHandler<UserContext, Id, Model, Params, DeleteListResource<UserContext>>(runtime) {
    companion object {
        private val LOG = loggerFor<ListResourceDeleteHandler<*, *, *, *>>()
    }

    override fun handle(
        request: DeleteListResource<UserContext>,
        requestContext: ResourceRequestContext
    ): UAPIResponse<*> {
        val id = getId(request.idParams)
        val userContext = request.userContext

        val model = resource.loadModel(requestContext, userContext, id)

        val result = doDelete(requestContext, userContext, id, model)

        return when (result) {
            DeleteResult.Success -> {
                LOG.info("Successfully deleted ${resource.singleName} $id")
                UAPIEmptyResponse
            }
            DeleteResult.AlreadyDeleted -> {
                LOG.info("${resource.singleName} $id has already been deleted; returning success")
                UAPIEmptyResponse
            }
            DeleteResult.Unauthorized -> {
                LOG.warn("NotAuthorized request to delete ${resource.singleName} $id! User contexts was $userContext")
                UAPINotAuthorizedError
            }
            is DeleteResult.CannotBeDeleted -> {
                LOG.warn("${resource.singleName} $id cannot be deleted: ${result.reason}")
                GenericUAPIErrorResponse(
                    statusCode = 409,
                    message = "Conflict",
                    validationInformation = listOf(result.reason)
                )
            }
            is DeleteResult.Error -> {
                LOG.error("Unexpected error deleting ${resource.singleName} $id: ${result.code} ${result.errors.joinToString()}", result.cause)
                GenericUAPIErrorResponse(
                    statusCode = result.code,
                    message = "Error",
                    validationInformation = result.errors
                )
            }
        }
    }

    private fun doDelete(
        requestContext: ResourceRequestContext,
        userContext: UserContext,
        id: Id,
        model: Model?
    ): DeleteResult {
        if (model == null) {
            return DeleteResult.AlreadyDeleted
        }
        if (!operation.canUserDelete(requestContext, userContext, id, model)) {
            return DeleteResult.Unauthorized
        }
        if (!operation.canBeDeleted(id, model)) {
            return DeleteResult.CannotBeDeleted("Cannot be deleted")
        }
        return operation.handleDelete(requestContext, userContext, id, model)
    }
}
