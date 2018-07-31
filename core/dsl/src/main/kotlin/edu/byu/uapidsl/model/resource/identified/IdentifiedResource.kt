package edu.byu.uapidsl.model.resource.identified

import edu.byu.uapidsl.model.ResponseModel
import edu.byu.uapidsl.model.resource.*
import edu.byu.uapidsl.model.resource.identified.ops.CreateOperation
import edu.byu.uapidsl.model.resource.identified.ops.DeleteOperation
import edu.byu.uapidsl.model.resource.identified.ops.ListOperation
import edu.byu.uapidsl.model.resource.identified.ops.UpdateOperation
import edu.byu.uapidsl.types.UAPIResponse
import kotlin.reflect.KClass

data class IdentifiedResource<
    AuthContext : Any,
    Id : Any,
    Model : Any>(
    override val type: KClass<Model>,
    override val responseModel: ResponseModel<Model>,
    val idModel: IdModel<Id>,
    val idExtractor: IdExtractor<Id, Model>,
    override val name: String,
    override val example: Model,
    override val operations: IdentifiedResourceOperations<AuthContext, Id, Model>
//    val output: OutputModel<AuthContext, Id, Model, *>,
//    val responseMapper: ModelResponseMapper<AuthContext, Id, Model>
//  val subresources: List<SubResourceModel<AuthContext, Id, Model, Any>>
) : Resource<
    AuthContext,
    Model,
    IdentifiedResourceModelContext<Id, Model>,
    IdentifiedResourceOptionalModelContext<Id, Model>,
    IdentifiedResourceOperations<AuthContext, Id, Model>
    > {

    init {

    }

    fun <Filters : Any> handleListRequest(
        request: ListResourceRequest<AuthContext, Filters>
    ): UAPIResponse<*> {
        @Suppress("UNCHECKED_CAST")
        val op = operations.list as ListOperation<AuthContext, Id, Model, Filters, *, *, *>?
            ?: throw UnsupportedOperationException("resource lists are not implemented")

        return op.handleRequest(request, this.operations.read)
    }

    fun handleFetchRequest(
        request: FetchResourceRequest<AuthContext, Id>
    ): UAPIResponse<*> = operations.read.handleRequest(request)

    fun <Input : Any> handleCreateRequest(
        request: CreateResourceRequest<AuthContext, Input>
    ): UAPIResponse<*> {
        @Suppress("UNCHECKED_CAST")
        val op: CreateOperation<AuthContext, Id, Input> = operations.create as CreateOperation<AuthContext, Id, Input>?
            ?: throw UnsupportedOperationException("create requests are not implemented")

        return op.handleRequest(request, this.operations.read)
    }

    fun <Input : Any> handleUpdateRequest(
        request: UpdateResourceRequest<AuthContext, Id, Input>
    ): UAPIResponse<*> {
        @Suppress("UNCHECKED_CAST")
        val op: UpdateOperation<AuthContext, Id, Model, Input, *> = operations.update as UpdateOperation<AuthContext, Id, Model, Input, *>?
            ?: throw UnsupportedOperationException("create requests are not implemented")

        return op.handleRequest(request, this.operations.read)
    }

    fun handleDeleteRequest(
        request: DeleteResourceRequest<AuthContext, Id>
    ): UAPIResponse<*> {
        @Suppress("UNCHECKED_CAST")
        val op: DeleteOperation<AuthContext, Id, Model> = operations.delete
            ?: throw UnsupportedOperationException("create requests are not implemented")

        return op.handleRequest(request, this.operations.read)
    }
}

data class IdentifiedResourceModelContext<Id : Any, Model : Any>(
    val id: Id,
    override val model: Model
) : ResourceModelContext<Model>

data class IdentifiedResourceOptionalModelContext<Id : Any, Model : Any>(
    val id: Id,
    override val model: Model?
) : ResourceOptionalModelContext<Model>

typealias IdentifiedReadContext<Auth, Id, Model> = ResourceReadContext<Auth, Model, IdentifiedResourceModelContext<Id, Model>>

interface DomainModelOps<
    AuthContext,
    IdType,
    ModelType> {
    fun idToModel(authContext: AuthContext, id: IdType): ModelType?

    fun modelToResult(authContext: AuthContext, model: ModelType): UAPIResponse<*>

    fun idToResult(authContext: AuthContext, id: IdType): UAPIResponse<*>
}

