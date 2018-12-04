package edu.byu.uapi.server.subresources.singleton

import edu.byu.uapi.spi.requests.IdParams
import edu.byu.uapi.spi.requests.RequestBody
import edu.byu.uapi.spi.requests.RequestContext

sealed class SingletonSubresourceRequest<UserContext : Any> {
    abstract val requestContext: RequestContext
    abstract val userContext: UserContext
    abstract val parentIdParams: IdParams

    data class Fetch<UserContext : Any>(
        override val requestContext: RequestContext,
        override val userContext: UserContext,
        override val parentIdParams: IdParams
    ) : SingletonSubresourceRequest<UserContext>()

    data class Update<UserContext : Any>(
        override val requestContext: RequestContext,
        override val userContext: UserContext,
        override val parentIdParams: IdParams,
        val body: RequestBody
    ) : SingletonSubresourceRequest<UserContext>()

    data class Delete<UserContext : Any>(
        override val requestContext: RequestContext,
        override val userContext: UserContext,
        override val parentIdParams: IdParams
    ) : SingletonSubresourceRequest<UserContext>()
}
