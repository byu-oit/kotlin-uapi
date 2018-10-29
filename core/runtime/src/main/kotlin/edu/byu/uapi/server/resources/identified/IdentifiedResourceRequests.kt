package edu.byu.uapi.server.resources.identified

import edu.byu.uapi.spi.input.SubsetParams

sealed class ResourceRequest<AuthContext> {
    abstract val userContext: AuthContext
}

sealed class ReadResourceRequest<AuthContext>: ResourceRequest<AuthContext>()
sealed class MutatingResourceRequest<AuthContext>: ResourceRequest<AuthContext>()

data class ListResourceRequest<
    UserContext,
    Filters>(
    override val userContext: UserContext,
    val filters: Filters,
    val paging: SubsetParams? = null
) : ReadResourceRequest<UserContext>()

data class FetchResourceRequest<
    UserContext,
    Id>(
    override val userContext: UserContext,
    val id: Id
): ReadResourceRequest<UserContext>()

data class CreateResourceRequest<
    UserContext,
    Input>(
    override val userContext: UserContext,
    val input: Input
): MutatingResourceRequest<UserContext>()

data class UpdateResourceRequest<
    UserContext,
    Id,
    Input>(
    override val userContext: UserContext,
    val id: Id,
    val input: Input
): MutatingResourceRequest<UserContext>()

data class DeleteResourceRequest<
    UserContext,
    Id>(
    override val userContext: UserContext,
    val id: Id
): MutatingResourceRequest<UserContext>()
