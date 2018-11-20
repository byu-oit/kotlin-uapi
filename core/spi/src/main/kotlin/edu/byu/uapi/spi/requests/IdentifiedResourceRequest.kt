package edu.byu.uapi.spi.requests

sealed class IdentifiedResourceRequest<UserContext : Any> {
    abstract val requestContext: RequestContext
    abstract val userContext: UserContext
}

sealed class IdentifiedResourceWithIdRequest<UserContext: Any>: IdentifiedResourceRequest<UserContext>() {
    abstract val idParams: IdParams
}

data class FetchIdentifiedResource<UserContext : Any>(
    override val requestContext: RequestContext,
    override val userContext: UserContext,
    override val idParams: IdParams,
    val queryParams: QueryParams
) : IdentifiedResourceWithIdRequest<UserContext>()

data class ListIdentifiedResource<UserContext : Any>(
    override val requestContext: RequestContext,
    override val userContext: UserContext,
    val queryParams: QueryParams
) : IdentifiedResourceRequest<UserContext>()

data class CreateIdentifiedResource<UserContext : Any>(
    override val requestContext: RequestContext,
    override val userContext: UserContext,
    val body: RequestBody
) : IdentifiedResourceRequest<UserContext>()

data class UpdateIdentifiedResource<UserContext : Any>(
    override val requestContext: RequestContext,
    override val userContext: UserContext,
    override val idParams: IdParams,
    val body: RequestBody
) : IdentifiedResourceWithIdRequest<UserContext>()

data class DeleteIdentifiedResource<UserContext : Any>(
    override val requestContext: RequestContext,
    override val userContext: UserContext,
    override val idParams: IdParams
) : IdentifiedResourceWithIdRequest<UserContext>()

