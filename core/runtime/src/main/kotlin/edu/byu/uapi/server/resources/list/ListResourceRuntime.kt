package edu.byu.uapi.server.resources.list

import edu.byu.uapi.server.subresources.Subresource
import edu.byu.uapi.server.subresources.createRuntime
import edu.byu.uapi.server.types.IdentifiedModel
import edu.byu.uapi.server.util.loggerFor
import edu.byu.uapi.spi.SpecConstants
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.input.IdParamReader
import edu.byu.uapi.spi.input.ListParams
import edu.byu.uapi.spi.validation.ValidationEngine
import java.util.*

class ListResourceRuntime<UserContext : Any, Id : Any, Model : Any, Params: ListParams>(
    internal val resource: ListResource<UserContext, Id, Model, Params>,
    val typeDictionary: TypeDictionary,
    val validationEngine: ValidationEngine,
    subresourceList: List<Subresource<UserContext, IdentifiedModel<Id, Model>, Model>> = emptyList()
) {

    val pluralName = resource.pluralName
    val singleName = resource.singleName

    // TODO fun validateResource(validation: Validating)

    companion object {
        private val LOG = loggerFor<ListResourceRuntime<*, *, *, *>>()
    }

    val idReader: IdParamReader<Id> = resource.getIdReader(typeDictionary)

    init {
        LOG.debug("Initializing $pluralName runtime")
    }

    val model: ListResourceModel by lazy {
        TODO()
    }

    val availableOperations: Set<ListResourceRequestHandler<UserContext, Id, Model, Params, *>> by lazy {
        val ops: MutableSet<ListResourceRequestHandler<UserContext, Id, Model, Params, *>> = mutableSetOf()

        ops.add(ListResourceFetchHandler(this))
        ops.add(ListResourceListHandler(this))

        resource.createOperation?.also { ops.add(ListResourceCreateHandler(this, it)) }
        resource.updateOperation?.also { ops.add(ListResourceUpdateHandler(this, it)) }
        resource.deleteOperation?.also { ops.add(ListResourceDeleteHandler(this, it)) }

        Collections.unmodifiableSet(ops)
    }

    val availableFieldsets = setOf(SpecConstants.FieldSets.VALUE_BASIC)
    val availableContexts = emptyMap<String, Set<String>>()

    val subresources = subresourceList.map { it.createRuntime(typeDictionary, validationEngine) }
}
