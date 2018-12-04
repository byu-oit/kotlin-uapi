package edu.byu.uapi.server.subresources.singleton

import edu.byu.uapi.spi.requests.IdParams
import edu.byu.uapi.spi.requests.RequestBody
import edu.byu.uapi.spi.requests.RequestContext

sealed class SingletonSubresourceRequest<UserContext : Any> {
    abstract val requestContext: RequestContext
    abstract val userContext: UserContext
    abstract val parentParams: IdParams
}

data class FetchSingletonSubresource<UserContext : Any>(
    override val requestContext: RequestContext,
    override val userContext: UserContext,
    override val parentParams: IdParams
) : SingletonSubresourceRequest<UserContext>()

data class UpdateSingletonSubresource<UserContext : Any>(
    override val requestContext: RequestContext,
    override val userContext: UserContext,
    override val parentParams: IdParams,
    val body: RequestBody
) : SingletonSubresourceRequest<UserContext>()

data class DeleteSingletonSubresource<UserContext : Any>(
    override val requestContext: RequestContext,
    override val userContext: UserContext,
    override val parentParams: IdParams
) : SingletonSubresourceRequest<UserContext>()
