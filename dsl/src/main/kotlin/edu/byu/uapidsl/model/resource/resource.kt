package edu.byu.uapidsl.model.resource

import edu.byu.uapidsl.dsl.IdExtractor
import edu.byu.uapidsl.model.ResponseModel
import edu.byu.uapidsl.model.resource.ops.ListOperation
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

    fun handleRequest(request: ResourceRequest<AuthContext>): UAPIResponse<*> =
        when (request) {
            is ListResourceRequest<AuthContext, *> -> TODO()
            is FetchResourceRequest<AuthContext, *> -> TODO()
            is CreateResourceRequest<AuthContext, *> -> TODO()
            is UpdateResourceRequest<AuthContext, *, *> -> TODO()
            is CreateOrUpdateResourceRequest<AuthContext, *, *> -> TODO()
            is DeleteResourceRequest<AuthContext, *> -> TODO()
        }

    fun <Filters : Any> handleListRequest(
        request: ListResourceRequest<AuthContext, Filters>
    ): UAPIResponse<*> {
        @Suppress("UNCHECKED_CAST")
        val op = operations.list as ListOperation<AuthContext, IdType, ResourceType, Filters, *, *, *>?
                ?: throw UnsupportedOperationException("resource lists are not implemented")

        return op.handleRequest(request, this.operations.read)
    }
}


interface DomainModelOps<
    AuthContext,
    IdType,
    ModelType> {
    fun idToModel(authContext: AuthContext, id: IdType): ModelType?

    fun modelToResult(authContext: AuthContext, model: ModelType): UAPIResponse<*>
}

