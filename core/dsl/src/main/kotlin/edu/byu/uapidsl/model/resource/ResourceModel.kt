package edu.byu.uapidsl.model.resource

import edu.byu.uapidsl.model.ResponseModel
import edu.byu.uapidsl.model.resource.ops.CreateOperation
import edu.byu.uapidsl.model.resource.ops.DeleteOperation
import edu.byu.uapidsl.model.resource.ops.ListOperation
import edu.byu.uapidsl.model.resource.ops.UpdateOperation
import edu.byu.uapidsl.types.UAPIResponse
import kotlin.reflect.KClass

data class ResourceModel<AuthContext : Any, IdType : Any, ResourceType : Any>(
    val type: KClass<ResourceType>,
    val responseModel: ResponseModel<ResourceType>,
    val idModel: IdModel<IdType>,
    val idExtractor: IdExtractor<IdType, ResourceType>,
    val name: String,
    val example: ResourceType,
    val operations: OperationModel<AuthContext, IdType, ResourceType>
//    val output: OutputModel<AuthContext, IdType, ResourceType, *>,
//    val responseMapper: ModelResponseMapper<AuthContext, IdType, ResourceType>
//  val subresources: List<SubResourceModel<AuthContext, IdType, ResourceType, Any>>
) {
    init {

    }

    fun <Filters : Any> handleListRequest(
        request: ListResourceRequest<AuthContext, Filters>
    ): UAPIResponse<*> {
        @Suppress("UNCHECKED_CAST")
        val op = operations.list as ListOperation<AuthContext, IdType, ResourceType, Filters, *, *, *>?
                ?: throw UnsupportedOperationException("resource lists are not implemented")

        return op.handleRequest(request, this.operations.read)
    }

    fun handleFetchRequest(
        request: FetchResourceRequest<AuthContext, IdType>
    ): UAPIResponse<*> = operations.read.handleRequest(request)

    fun <Input: Any> handleCreateRequest(
        request: CreateResourceRequest<AuthContext, Input>
    ): UAPIResponse<*> {
        @Suppress("UNCHECKED_CAST")
        val op: CreateOperation<AuthContext, IdType, Input> = operations.create as CreateOperation<AuthContext, IdType, Input>?
            ?: throw UnsupportedOperationException("create requests are not implemented")

        return op.handleRequest(request, this.operations.read)
    }

    fun <Input: Any> handleUpdateRequest(
        request: UpdateResourceRequest<AuthContext, IdType, Input>
    ): UAPIResponse<*> {
        @Suppress("UNCHECKED_CAST")
        val op: UpdateOperation<AuthContext, IdType, ResourceType, Input, *> = operations.update as UpdateOperation<AuthContext, IdType, ResourceType, Input, *>?
            ?: throw UnsupportedOperationException("create requests are not implemented")

        return op.handleRequest(request, this.operations.read)
    }

    fun handleDeleteRequest(
        request: DeleteResourceRequest<AuthContext, IdType>
    ): UAPIResponse<*> {
        @Suppress("UNCHECKED_CAST")
        val op: DeleteOperation<AuthContext, IdType, ResourceType> = operations.delete
            ?: throw UnsupportedOperationException("create requests are not implemented")

        return op.handleRequest(request, this.operations.read)
    }
}


interface DomainModelOps<
    AuthContext,
    IdType,
    ModelType> {
    fun idToModel(authContext: AuthContext, id: IdType): ModelType?

    fun modelToResult(authContext: AuthContext, model: ModelType): UAPIResponse<*>

    fun idToResult(authContext: AuthContext, id: IdType): UAPIResponse<*>
}

