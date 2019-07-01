package edu.byu.uapi.spi.requests

sealed class ListResourceRequest<UserContext : Any> {
    abstract val requestContext: RequestContext
    abstract val userContext: UserContext
}

sealed class ListResourceWithIdRequest<UserContext: Any>: ListResourceRequest<UserContext>() {
    abstract val idParams: IdParams
}

data class FetchListResource<UserContext : Any>(
    override val requestContext: RequestContext,
    override val userContext: UserContext,
    override val idParams: IdParams,
    val queryParams: QueryParams
) : ListResourceWithIdRequest<UserContext>()

data class ListListResource<UserContext : Any>(
    override val requestContext: RequestContext,
    override val userContext: UserContext,
    val queryParams: QueryParams
) : ListResourceRequest<UserContext>()

data class CreateListResource<UserContext : Any>(
    override val requestContext: RequestContext,
    override val userContext: UserContext,
    val body: RequestBody
) : ListResourceRequest<UserContext>()

data class UpdateListResource<UserContext : Any>(
    override val requestContext: RequestContext,
    override val userContext: UserContext,
    override val idParams: IdParams,
    val body: RequestBody
) : ListResourceWithIdRequest<UserContext>()

data class DeleteListResource<UserContext : Any>(
    override val requestContext: RequestContext,
    override val userContext: UserContext,
    override val idParams: IdParams
) : ListResourceWithIdRequest<UserContext>()

