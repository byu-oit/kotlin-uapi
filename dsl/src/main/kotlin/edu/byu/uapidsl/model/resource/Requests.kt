package edu.byu.uapidsl.model.resource

import edu.byu.uapidsl.dsl.PagingParams

sealed class ResourceRequest<AuthContext> {
    abstract val authContext: AuthContext
}

sealed class ReadResourceRequest<AuthContext>: ResourceRequest<AuthContext>()
sealed class MutatingResourceRequest<AuthContext>: ResourceRequest<AuthContext>()

data class ListResourceRequest<
    AuthContext,
    Filters>(
    override val authContext: AuthContext,
    val filters: Filters,
    val paging: PagingParams? = null
) : ReadResourceRequest<AuthContext>()

data class FetchResourceRequest<
    AuthContext,
    IdType>(
    override val authContext: AuthContext,
    val id: IdType
): ReadResourceRequest<AuthContext>()

data class CreateResourceRequest<
    AuthContext,
    Input>(
    override val authContext: AuthContext,
    val input: Input
): MutatingResourceRequest<AuthContext>()

data class UpdateResourceRequest<
    AuthContext,
    IdType,
    Input>(
    override val authContext: AuthContext,
    val id: IdType,
    val input: Input
): MutatingResourceRequest<AuthContext>()

data class DeleteResourceRequest<
    AuthContext,
    IdType>(
    override val authContext: AuthContext,
    val id: IdType
): MutatingResourceRequest<AuthContext>()
