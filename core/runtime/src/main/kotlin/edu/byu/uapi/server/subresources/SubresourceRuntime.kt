package edu.byu.uapi.server.subresources

import edu.byu.uapi.server.subresources.list.*
import edu.byu.uapi.server.subresources.singleton.*
import edu.byu.uapi.server.types.ModelHolder
import edu.byu.uapi.server.util.loggerFor
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.input.ListParams
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

        subresource.updateMutation?.also { ops.add(SingletonSubresourceUpdateHandler(this, it)) }
        subresource.deleteMutation?.also { ops.add(SingletonSubresourceDeleteHandler(this, it)) }

        Collections.unmodifiableSet(ops)
    }
}

class ListSubresourceRuntime<UserContext : Any, Parent : ModelHolder, Id : Any, Model : Any, Params : ListParams>(
    internal val subresource: ListSubresource<UserContext, Parent, Id, Model, Params>,
    val parent: SubresourceParent<UserContext, Parent>,
    val typeDictionary: TypeDictionary,
    val validationEngine: ValidationEngine
) : SubresourceRuntime<UserContext, Parent, Model>() {
    val pluralName = subresource.pluralName

    companion object {
        private val LOG = loggerFor<ListSubresourceRuntime<*, *, *, *, *>>()
    }

    val idReader = subresource.getIdReader(typeDictionary)

    val availableOperations: Set<ListSubresourceRequestHandler<UserContext, Parent, Id, Model, Params, *>> by lazy {
        val ops: MutableSet<ListSubresourceRequestHandler<UserContext, Parent, Id, Model, Params, *>> = mutableSetOf()

        ops.add(ListSubresourceFetchHandler(this))
        ops.add(ListSubresourceListHandler(this))

        subresource.createOperation?.also { ops.add(ListSubresourceCreateHandler(this, it)) }
        subresource.updateOperation?.also { ops.add(ListSubresourceUpdateHandler(this, it)) }
        subresource.deleteOperation?.also { ops.add(ListSubresourceDeleteHandler(this, it)) }

        Collections.unmodifiableSet(ops)
    }


}



