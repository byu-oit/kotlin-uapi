package edu.byu.uapi.server.subresources

import edu.byu.uapi.server.subresources.list.ListSubresource
import edu.byu.uapi.server.subresources.singleton.SingletonSubresource
import edu.byu.uapi.server.types.ModelHolder
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.requests.FieldsetRequest
import edu.byu.uapi.spi.requests.IdParams
import edu.byu.uapi.spi.validation.ValidationEngine

interface Subresource<UserContext : Any, Parent : ModelHolder, Model : Any> {
    val pretendUnauthorizedDoesntExist: Boolean
        get() = false
}

fun <UserContext : Any, Parent : ModelHolder, Model : Any> Subresource<UserContext, Parent, Model>.createRuntime(
    parent: SubresourceParent<UserContext, Parent>,
    typeDictionary: TypeDictionary,
    validationEngine: ValidationEngine
): SubresourceRuntime<UserContext, Parent, Model> {
    return when {
        this is SingletonSubresource -> SingletonSubresourceRuntime(this, parent, typeDictionary, validationEngine)
        this is ListSubresource<UserContext, Parent, *, Model, *> -> ListSubresourceRuntime(this, parent, typeDictionary, validationEngine)
        else -> throw IllegalStateException("Unknown subresource type: ${this::class.qualifiedName}")
    }
}

interface SubresourceParent<UserContext : Any, Model : ModelHolder> {
    fun getParentModel(
        requestContext: SubresourceRequestContext,
        user: UserContext,
        idParams: IdParams
    ): ParentResult<Model>

    fun getRequestedFieldsets(fieldsetRequest: FieldsetRequest?): RequestedFieldsetResponse
}

sealed class ParentResult<out Model : ModelHolder> {
    data class Success<Model : ModelHolder>(
        val value: Model
    ) : ParentResult<Model>()

    object DoesNotExist : ParentResult<Nothing>()
    object NotAuthorized : ParentResult<Nothing>()
}

sealed class RequestedFieldsetResponse {
    data class Success(val fieldsets: Set<String>) : RequestedFieldsetResponse()
    object InvalidFieldsets : RequestedFieldsetResponse()
    object InvalidContexts : RequestedFieldsetResponse()

    companion object {
        fun of(values: Set<String>) = Success(values)
    }
}
