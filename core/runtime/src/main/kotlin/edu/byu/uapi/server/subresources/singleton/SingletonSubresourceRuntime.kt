package edu.byu.uapi.server.subresources.singleton

import edu.byu.uapi.server.types.ModelHolder
import edu.byu.uapi.server.util.loggerFor
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.validation.ValidationEngine
import java.util.*

sealed class SubresourceRuntime<UserContext: Any, Parent: ModelHolder, Model: Any>

class SingletonSubresourceRuntime<UserContext: Any, Parent: ModelHolder, Model: Any>(
    internal val subresource: SingletonSubresource<UserContext, Parent, Model>,
    val typeDictionary: TypeDictionary,
    val validationEngine: ValidationEngine
): SubresourceRuntime<UserContext, Parent, Model>() {
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



sealed class SingletonSubresourceRequestHandler<UserContext: Any, Parent: ModelHolder, Model: Any, Request: SingletonSubresourceRequest<UserContext, Parent>>(
) {
    abstract val runtime: SingletonSubresourceRuntime<UserContext, Parent, Model>
    val subresource: SingletonSubresource<UserContext, Parent, Model>
        get() = runtime.subresource

    abstract fun handle(request: Request)
}

class SingletonSubresourceFetchHandler<UserContext: Any, Parent: ModelHolder, Model: Any>(
    override val runtime: SingletonSubresourceRuntime<UserContext, Parent, Model>
): SingletonSubresourceRequestHandler<UserContext, Parent, Model, FetchSingletonSubresource<UserContext, Parent>>() {
    override fun handle(request: FetchSingletonSubresource<UserContext, Parent>) {
        val user= request.userContext
//        val parent = request.parentInfo.invoke()
        TODO("not implemented")
    }
}

