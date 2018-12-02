package edu.byu.uapi.server.subresources.singleton

import edu.byu.uapi.server.types.ModelHolder
import edu.byu.uapi.spi.requests.RequestBody
import edu.byu.uapi.spi.requests.RequestContext

sealed class SingletonSubresourceRequest<UserContext : Any, Parent: ModelHolder> {
    abstract val requestContext: RequestContext
    abstract val userContext: UserContext
    abstract val parent: Parent
}

data class FetchSingletonSubresource<UserContext : Any, Parent: ModelHolder>(
    override val requestContext: RequestContext,
    override val userContext: UserContext,
    override val parent: Parent
) : SingletonSubresourceRequest<UserContext, Parent>()

data class UpdateSingletonSubresource<UserContext : Any, Parent: ModelHolder>(
    override val requestContext: RequestContext,
    override val userContext: UserContext,
    override val parent: Parent,
    val body: RequestBody
) : SingletonSubresourceRequest<UserContext, Parent>()

data class DeleteSingletonSubresource<UserContext : Any, Parent: ModelHolder>(
    override val requestContext: RequestContext,
    override val userContext: UserContext,
    override val parent: Parent
) : SingletonSubresourceRequest<UserContext, Parent>()
