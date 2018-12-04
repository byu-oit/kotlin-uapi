package edu.byu.uapi.spi.requests

sealed class ListSubresourceRequest<UserContext : Any> {
    abstract val requestContext: RequestContext
    abstract val userContext: UserContext
    abstract val parentIdParams: IdParams

    data class Fetch<UserContext : Any>(
        override val requestContext: RequestContext,
        override val userContext: UserContext,
        override val parentIdParams: IdParams,
        val idParams: IdParams,
        val queryParams: QueryParams
    ) : ListSubresourceRequest<UserContext>()

    data class List<UserContext : Any>(
        override val requestContext: RequestContext,
        override val userContext: UserContext,
        override val parentIdParams: IdParams,
        val queryParams: QueryParams
    ) : ListSubresourceRequest<UserContext>()

    data class Create<UserContext : Any>(
        override val requestContext: RequestContext,
        override val userContext: UserContext,
        override val parentIdParams: IdParams,
        val body: RequestBody
    ) : ListSubresourceRequest<UserContext>()

    data class Update<UserContext : Any>(
        override val requestContext: RequestContext,
        override val userContext: UserContext,
        override val parentIdParams: IdParams,
        val idParams: IdParams,
        val body: RequestBody
    ) : ListSubresourceRequest<UserContext>()

    data class Delete<UserContext : Any>(
        override val requestContext: RequestContext,
        override val userContext: UserContext,
        override val parentIdParams: IdParams,
        val idParams: IdParams
    ) : ListSubresourceRequest<UserContext>()

}
