package edu.byu.uapi.server.subresources

import edu.byu.uapi.server.subresources.list.*
import edu.byu.uapi.server.subresources.singleton.*
import edu.byu.uapi.server.types.*
import edu.byu.uapi.server.util.loggerFor
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.input.ListParams
import edu.byu.uapi.spi.requests.RequestContext
import edu.byu.uapi.spi.validation.ValidationEngine
import java.util.*

sealed class SubresourceRuntime<UserContext : Any, Parent : ModelHolder, Model : Any> {
    abstract fun handleBasicFetch(
        requestContext: RequestContext,
        subresourceRequestContext: SubresourceRequestContext,
        userContext: UserContext,
        parent: Parent
    ): UAPIResponse<*>

    abstract val fieldsetName: String
}

class SingletonSubresourceRuntime<UserContext : Any, Parent : ModelHolder, Model : Any>(
    internal val subresource: SingletonSubresource<UserContext, Parent, Model>,
    val parent: SubresourceParent<UserContext, Parent>,
    val typeDictionary: TypeDictionary,
    val validationEngine: ValidationEngine
) : SubresourceRuntime<UserContext, Parent, Model>() {

    val name = subresource.name
    override val fieldsetName: String = name

    companion object {
        private val LOG = loggerFor<SingletonSubresourceRuntime<*, *, *>>()
    }

    init {
        LOG.debug("Initializing $name runtime")
    }

    val availableOperations: Set<SingletonSubresourceRequestHandler<UserContext, Parent, Model, *>> by lazy {
        val ops: MutableSet<SingletonSubresourceRequestHandler<UserContext, Parent, Model, *>> = mutableSetOf()

        ops.add(SingletonSubresourceFetchHandler(this))

        subresource.updateMutation?.also { ops.add(SingletonSubresourceUpdateHandler(this, it)) }
        subresource.deleteMutation?.also { ops.add(SingletonSubresourceDeleteHandler(this, it)) }

        Collections.unmodifiableSet(ops)
    }

    override fun handleBasicFetch(
        requestContext: RequestContext,
        subresourceRequestContext: SubresourceRequestContext,
        userContext: UserContext,
        parent: Parent
    ): UAPIResponse<*> {
        val model = subresource.loadModel(subresourceRequestContext, userContext, parent) ?: return UAPINotFoundError

        if (!subresource.canUserViewModel(subresourceRequestContext, userContext, parent, model)) {
            return UAPINotAuthorizedError
        }
        return buildResponse(userContext, parent, model)
    }

    internal fun buildResponse(
        user: UserContext,
        parent: Parent,
        model: Model,
        validationResponse: ValidationResponse = ValidationResponse.OK
    ): UAPIPropertiesResponse {
        return UAPIPropertiesResponse(
            metadata = UAPIResourceMeta(validationResponse),
            links = generateLinks(user, parent, model),
            properties = modelToProperties(user, parent, model)
        )
    }

    private fun modelToProperties(
        user: UserContext,
        parent: Parent,
        model: Model
    ): Map<String, UAPIProperty> {
        return subresource.responseFields.map { f ->
            f.name to f.toProp(user, model)
        }.toMap()
    }

    @Suppress("UNUSED_PARAMETER")
    internal fun generateLinks(
        userContext: UserContext,
        parent: Parent,
        model: Model
    ): UAPILinks {
        //TODO generate links
        return emptyMap()
    }
}

class ListSubresourceRuntime<UserContext : Any, Parent : ModelHolder, Id : Any, Model : Any, Params : ListParams>(
    internal val subresource: ListSubresource<UserContext, Parent, Id, Model, Params>,
    val parent: SubresourceParent<UserContext, Parent>,
    val typeDictionary: TypeDictionary,
    val validationEngine: ValidationEngine
) : SubresourceRuntime<UserContext, Parent, Model>() {
    val pluralName = subresource.pluralName
    override val fieldsetName: String = pluralName

    companion object {
        private val LOG = loggerFor<ListSubresourceRuntime<*, *, *, *, *>>()
    }

    val idReader = subresource.getIdReader(typeDictionary)

    private val fetchHandler = ListSubresourceFetchHandler(this)
    private val listHandler = ListSubresourceListHandler(this)

    val availableOperations: Set<ListSubresourceRequestHandler<UserContext, Parent, Id, Model, Params, *>> by lazy {
        val ops: MutableSet<ListSubresourceRequestHandler<UserContext, Parent, Id, Model, Params, *>> = mutableSetOf()

        ops.add(fetchHandler)
        ops.add(listHandler)

        subresource.createOperation?.also { ops.add(ListSubresourceCreateHandler(this, it)) }
        subresource.updateOperation?.also { ops.add(ListSubresourceUpdateHandler(this, it)) }
        subresource.deleteOperation?.also { ops.add(ListSubresourceDeleteHandler(this, it)) }

        Collections.unmodifiableSet(ops)
    }

    override fun handleBasicFetch(
        requestContext: RequestContext,
        subresourceRequestContext: SubresourceRequestContext,
        userContext: UserContext,
        parent: Parent
    ): UAPIResponse<*> {
        return listHandler.handle(subresourceRequestContext, userContext, emptyMap(), parent)
    }
}



