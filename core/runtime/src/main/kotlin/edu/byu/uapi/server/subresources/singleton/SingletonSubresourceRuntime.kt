package edu.byu.uapi.server.subresources.singleton

import edu.byu.uapi.server.subresources.ParentResult
import edu.byu.uapi.server.subresources.SubresourceParent
import edu.byu.uapi.server.types.*
import edu.byu.uapi.server.util.loggerFor
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.validation.ValidationEngine
import java.util.*

sealed class SubresourceRuntime<UserContext : Any, Parent : ModelHolder, Model : Any>

class SingletonSubresourceRuntime<UserContext : Any, Parent : ModelHolder, Model : Any>(
    internal val subresource: SingletonSubresource<UserContext, Parent, Model>,
    val parent: SubresourceParent<UserContext, Parent>,
    val typeDictionary: TypeDictionary,
    val validationEngine: ValidationEngine
) : SubresourceRuntime<UserContext, Parent, Model>() {
    val name = subresource.name

    companion object {
        private val LOG = loggerFor<SingletonSubresourceRuntime<*, *, *>>()
    }

    init {
        LOG.debug("Initializing $name runtime")
    }

    val availableOperations: Set<SingletonSubresourceRequestHandler<UserContext, Parent, Model, *>> by lazy {
        val ops: MutableSet<SingletonSubresourceRequestHandler<UserContext, Parent, Model, *>> = mutableSetOf()

        ops.add(SingletonSubresourceFetchHandler(this))

        Collections.unmodifiableSet(ops)
    }

}


sealed class SingletonSubresourceRequestHandler<UserContext : Any, Parent : ModelHolder, Model : Any, Request : SingletonSubresourceRequest<UserContext>>(
) {
    abstract val runtime: SingletonSubresourceRuntime<UserContext, Parent, Model>
    val subresource: SingletonSubresource<UserContext, Parent, Model>
        get() = runtime.subresource

    fun handle(request: Request): UAPIResponse<*> {
        return when (val parent = runtime.parent.getParentModel(request.userContext, request.parentParams)) {
            is ParentResult.Success -> handle(request, parent.value)
            ParentResult.DoesNotExist -> UAPINotFoundError
            ParentResult.NotAuthorized -> UAPINotAuthorizedError
        }
    }

    abstract fun handle(
        request: Request,
        parent: Parent
    ): UAPIResponse<*>

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

class SingletonSubresourceFetchHandler<UserContext : Any, Parent : ModelHolder, Model : Any>(
    override val runtime: SingletonSubresourceRuntime<UserContext, Parent, Model>
) : SingletonSubresourceRequestHandler<UserContext, Parent, Model, FetchSingletonSubresource<UserContext>>() {
    override fun handle(
        request: FetchSingletonSubresource<UserContext>,
        parent: Parent
    ): UAPIResponse<*> {
        val user = request.userContext

        val model = subresource.loadModel(user, parent) ?: return UAPINotFoundError

        if (!subresource.canUserViewModel(user, parent, model)) {
            return UAPINotAuthorizedError
        }
        return buildResponse(user, parent, model)
    }
}

