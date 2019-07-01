package edu.byu.uapi.server.resources.list

import edu.byu.uapi.model.UAPIListResourceModel
import edu.byu.uapi.model.UAPIResourceModel
import edu.byu.uapi.server.claims.ClaimModelLoader
import edu.byu.uapi.server.claims.ClaimsRuntime
import edu.byu.uapi.server.resources.Resource
import edu.byu.uapi.server.resources.ResourceRequestContext
import edu.byu.uapi.server.subresources.*
import edu.byu.uapi.server.types.IdentifiedModel
import edu.byu.uapi.server.types.ModelHolder
import edu.byu.uapi.server.util.loggerFor
import edu.byu.uapi.spi.SpecConstants.FieldSets.DEFAULT_FIELDSETS
import edu.byu.uapi.spi.SpecConstants.FieldSets.VALUE_BASIC
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.input.IdParamReader
import edu.byu.uapi.spi.input.ListParams
import edu.byu.uapi.spi.introspection.Introspectable
import edu.byu.uapi.spi.introspection.IntrospectionContext
import edu.byu.uapi.spi.introspection.IntrospectionLocation
import edu.byu.uapi.spi.requests.FieldsetRequest
import edu.byu.uapi.spi.requests.IdParams
import edu.byu.uapi.spi.validation.ValidationEngine
import edu.byu.uapi.utility.collections.ifNotEmpty
import java.util.*
import kotlin.reflect.KClass

sealed class ResourceRuntime<
    UserContext : Any,
    ModelStyle : ModelHolder,
    ResourceModelType : UAPIResourceModel
    > : SubresourceParent<UserContext, ModelStyle>, Introspectable<Pair<String, ResourceModelType>> {

    abstract val name: String

}

class ListResourceRuntime<UserContext : Any, Id : Any, Model : Any, Params : ListParams>(
    internal val resource: ListResource<UserContext, Id, Model, Params>,
    val typeDictionary: TypeDictionary,
    val validationEngine: ValidationEngine,
    subresourceList: List<Pair<Subresource<UserContext, IdentifiedModel<Id, Model>, *>, SubresourceConfig>> = emptyList()
) : ResourceRuntime<UserContext, IdentifiedModel<Id, Model>, UAPIListResourceModel>() {

    val pluralName = resource.pluralName
    val singleName = resource.singleName

    override val name: String = pluralName

    companion object {
        private val LOG = loggerFor<ListResourceRuntime<*, *, *, *>>()
    }

    val idReader: IdParamReader<Id> = resource.getIdReader(typeDictionary)

    init {
        LOG.debug("Initializing $pluralName runtime")
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

    val subresources: Map<String, SubresourceRuntime<UserContext, IdentifiedModel<Id, Model>, *>> =
        subresourceList.map { it.first.createRuntime(this, typeDictionary, validationEngine, it.second) }.associateBy { it.fieldsetName }

    val defaultFieldsets: Set<String> = setOf(VALUE_BASIC) + subresources.asSequence()
        .filter { it.value.config.returnByDefault }
        .map { it.key }
    val availableFieldsets = DEFAULT_FIELDSETS + subresources.keys
    val availableContexts = emptyMap<String, Set<String>>()

    val claimsRuntime: ClaimsRuntime<UserContext, Id, Model>? = resource.claims?.toRuntime(this)

    override fun getRequestedFieldsets(fieldsetRequest: FieldsetRequest?): RequestedFieldsetResponse {
        if (fieldsetRequest == null) {
            return RequestedFieldsetResponse.of(defaultFieldsets)
        }
        val requestedFieldsets = fieldsetRequest.requestedFieldsets
        val requestedContexts = fieldsetRequest.requestedContexts

        // Validate incoming fieldsets and contexts
        (requestedFieldsets - availableFieldsets).ifNotEmpty { return RequestedFieldsetResponse.InvalidFieldsets }
        (requestedContexts - availableContexts.keys).ifNotEmpty { return RequestedFieldsetResponse.InvalidContexts }

        val fromContexts =
            fieldsetRequest.requestedContexts.flatMap { availableContexts.getValue(it) /* we already validated contexts */ }
        val set = fieldsetRequest.requestedFieldsets.ifEmpty { defaultFieldsets } + fromContexts
        return RequestedFieldsetResponse.of(set)
    }

    override fun getParentModel(
        requestContext: SubresourceRequestContext,
        user: UserContext,
        idParams: IdParams
    ): ParentResult<IdentifiedModel<Id, Model>> {
        val context = ResourceRequestContext.Simple(requestContext.allRequestedSubresources, requestContext.attributes)
        val id = idReader.read(idParams)
        val model = resource.loadModel(context, user, id) ?: return ParentResult.DoesNotExist
        val authorized = resource.canUserViewModel(context, user, id, model)
        if (!authorized) return ParentResult.NotAuthorized
        return ParentResult.Success(IdentifiedModel.Simple(id, model))
    }

    override fun introspect(context: IntrospectionContext): Pair<String, UAPIListResourceModel> {
        return this.pluralName to context.withLocation(resource.asIntrospectionLocation()) {
            introspect(this@ListResourceRuntime, this)
        }
    }
}

fun KClass<*>.asIntrospectionLocation(): IntrospectionLocation = IntrospectionLocation.of(this)

fun Any.asIntrospectionLocation(): IntrospectionLocation {
    return IntrospectionLocation.of(this::class)
}

private fun <UserContext : Any, SubjectId : Any, Model : Any> Resource.HasClaims<UserContext, SubjectId, Model, *>.toRuntime(
    resource: ListResourceRuntime<UserContext, SubjectId, Model, *>
): ClaimsRuntime<UserContext, SubjectId, Model> {
    return ClaimsRuntime(
        SimpleClaimModelLoader(resource.resource, this),
        resource.typeDictionary,
        this.getClaimIdScalar(resource.typeDictionary),
        this.claimConcepts
    )
}

private class SimpleClaimModelLoader<UserContext : Any, SubjectId : Any, Model : Any>(
    private val listResource: ListResource<UserContext, SubjectId, Model, *>,
    private val claims: Resource.HasClaims<UserContext, SubjectId, Model, *>
) : ClaimModelLoader<UserContext, SubjectId, Model> {
    override fun loadClaimModel(request: ResourceRequestContext, userContext: UserContext, id: SubjectId): Model? {
        return listResource.loadModel(request, userContext, id)
    }

    override fun canUserMakeAnyClaims(
        requestContext: ResourceRequestContext,
        userContext: UserContext,
        id: SubjectId,
        model: Model
    ): Boolean {
        return claims.canUserMakeAnyClaims(requestContext, userContext, id, model)
    }
}
