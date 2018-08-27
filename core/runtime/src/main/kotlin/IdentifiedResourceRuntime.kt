package edu.byu.uapi.server

import edu.byu.uapi.server.types.*

class IdentifiedResourceRuntime<UserContext : Any, Id : Any, Model : Any>(
    private val resource: IdentifiedResource<UserContext, Id, Model>
) {

//    @Suppress("UNCHECKED_CAST")
//    fun <Filters : Any> handleListRequest(
//        request: ListResourceRequest<UserContext, Filters>
//    ): UAPIResponse<*> {
//        if (listOp != null) {
//            val op = listOp as Listable<UserContext, Id, Model, Filters>
//            op.list(
//                request.userContext,
//                request.filters
//            )
//        } else if (pagedListOp != null) {
//            val op = pagedListOp as PagedListable<UserContext, Id, Model, Filters>
//            op.list(
//                request.userContext,
//                request.filters,
//                request.paging ?: PagingParams(0, op.defaultPageSize)
//            )
//        } else {
//            return ErrorResponse.badRoute()
//        }
//    }

    fun handleFetchRequest(
        request: FetchResourceRequest<UserContext, Id>
    ): UAPIResponse<*> {
        val user = request.userContext
        val id = request.id
        val model = resource.loadModel(user, id) ?: return UAPINotFoundError
        if (!resource.canUserViewModel(user, id, model)) {
            return UAPINotAuthorizedError
        }
        return toResponse(user, id, model)
    }

    fun toResponse(userContext: UserContext, id: Id, model: Model): UAPIPropertiesResponse<Model> {
        return UAPIPropertiesResponse(
            metadata = UAPIResourceMeta(),
            links = generateLinks(userContext, id, model),
            properties = model
        )
    }

    fun generateLinks(userContext: UserContext, id: Id, model: Model): UAPILinks {
        //TODO
        return emptyMap()
    }

//    fun <Input : Any> handleCreateRequest(
//        request: CreateResourceRequest<UserContext, Input>
//    ): UAPIResponse<*> {
//        @Suppress("UNCHECKED_CAST")
//        val op: CreateOperation<UserContext, Id, Input> = operations.create as CreateOperation<UserContext, Id, Input>?
//            ?: throw UnsupportedOperationException("create requests are not implemented")
//
//        return op.handleRequest(request, this.operations.read)
//    }
//
//    fun <Input : Any> handleUpdateRequest(
//        request: UpdateResourceRequest<UserContext, Id, Input>
//    ): UAPIResponse<*> {
//        @Suppress("UNCHECKED_CAST")
//        val op: UpdateOperation<UserContext, Id, Model, Input, *> = operations.update as UpdateOperation<UserContext, Id, Model, Input, *>?
//            ?: throw UnsupportedOperationException("create requests are not implemented")
//
//        return op.handleRequest(request, this.operations.read)
//    }
//
//    fun handleDeleteRequest(
//        request: DeleteResourceRequest<UserContext, Id>
//    ): UAPIResponse<*> {
//        @Suppress("UNCHECKED_CAST")
//        val op: DeleteOperation<UserContext, Id, Model> = operations.delete
//            ?: throw UnsupportedOperationException("create requests are not implemented")
//
//        return op.handleRequest(request, this.operations.read)
//    }


}

//class IdentifiedResourceListHandler<UserContext : Any, Id : Any, Model : Any, Filters: Any>(
//    private val listable: Listable<UserContext, Id, Model, Filters>
//) {
//    fun handle(request: ListResourceRequest<UserContext, Filters>): UAPIResponse<*> {
//        val list = listable.list(request.userContext, request.filters)
//
//    }
//}

inline fun <reified T> Any.takeIfType(): T? {
    return if (this is T) {
        this
    } else {
        null
    }
}
