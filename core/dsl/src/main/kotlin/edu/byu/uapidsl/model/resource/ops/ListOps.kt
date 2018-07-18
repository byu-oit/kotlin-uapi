package edu.byu.uapidsl.model.resource.ops

import edu.byu.uapidsl.dsl.*
import edu.byu.uapidsl.model.resource.*
import edu.byu.uapidsl.types.*
import either.Either
import either.fold

sealed class ListOperation<AuthContext, IdType, DomainType, Filters : Any, RequestContext : ListContext<AuthContext, Filters>, IdCollection : Collection<IdType>, DomainCollection : Collection<DomainType>> {
    abstract val filterType: QueryParamModel<Filters>
    abstract val handle: Either<
        RequestContext.() -> IdCollection,
        RequestContext.() -> DomainCollection
        >

    fun handleRequest(req: ListResourceRequest<AuthContext, Filters>, modelOps: DomainModelOps<AuthContext, IdType, DomainType>): UAPIResponse<*> {
        val ctx = createRequestContext(
            req.authContext, req.filters, req.paging
        )

        val results = handle.fold(
            { it -> idBasedLoader(it, modelOps) },
            { it }
        ).invoke(ctx)

        val resources: List<UAPIResponse<*>> = results.map { modelOps.modelToResult(req.authContext, it) }

        return UAPIListResponse(
            values = resources,
            metadata = responseMetadata(results, ctx)
        )
    }

    abstract fun createRequestContext(
        authContext: AuthContext,
        filters: Filters,
        paging: PagingParams?
    ): RequestContext


    private fun idBasedLoader(
        handler: (ctx: RequestContext) -> IdCollection,
        modelOps: DomainModelOps<AuthContext, IdType, DomainType>
    ): (ctx: RequestContext) -> DomainCollection {
        return { ctx: RequestContext ->
            val ids = handler.invoke(ctx)
            val models = ids.map {
                modelOps.idToModel(ctx.authContext, it) ?: throw IllegalStateException("Couldn't find model for known-good ID '$it'")
            }
            idToModelCollection(ids, models)
        }
    }

    protected abstract fun idToModelCollection(ids: IdCollection, models: List<DomainType>): DomainCollection

    protected abstract fun responseMetadata(list: DomainCollection, requestContext: RequestContext): CollectionMetadata

}

data class SimpleListOperation<AuthContext, IdType, DomainType, Filters : Any>(
    override val filterType: QueryParamModel<Filters>,
    override val handle: Either<
        ListHandler<AuthContext, Filters, IdType>,
        ListHandler<AuthContext, Filters, DomainType>
        >
) : ListOperation<
    AuthContext,
    IdType,
    DomainType,
    Filters,
    ListContext<AuthContext, Filters>,
    Collection<IdType>,
    Collection<DomainType>
    >() {

    override fun createRequestContext(authContext: AuthContext, filters: Filters, paging: PagingParams?): ListContext<AuthContext, Filters> {
        return ListContextImpl(authContext, filters)
    }

    override fun idToModelCollection(ids: Collection<IdType>, models: List<DomainType>): Collection<DomainType> {
        return models
    }

    override fun responseMetadata(list: Collection<DomainType>, requestContext: ListContext<AuthContext, Filters>): CollectionMetadata {
        return SimpleCollectionMetadata(
            collectionSize = list.size
        )
    }
}

data class ListContextImpl<AuthContext, Filters>(
    override val authContext: AuthContext,
    override val filters: Filters
) : ListContext<AuthContext, Filters>

data class PagedListOperation<AuthContext, IdType, DomainType, Filters : Any>(
    override val filterType: QueryParamModel<Filters>,
    val pageParamModel: QueryParamModel<PagingParams>,
    val defaultPageSize: Int,
    val maxPageSize: Int,
    override val handle: Either<
        PagedListHandler<AuthContext, Filters, IdType>,
        PagedListHandler<AuthContext, Filters, DomainType>>
) : ListOperation<
    AuthContext,
    IdType,
    DomainType,
    Filters,
    PagedListContext<AuthContext, Filters>,
    CollectionWithTotal<IdType>,
    CollectionWithTotal<DomainType>>() {

    override fun createRequestContext(authContext: AuthContext, filters: Filters, paging: PagingParams?): PagedListContext<AuthContext, Filters> {
        return PagedListContextImpl(
            authContext,
            filters,
            paging ?: PagingParams(0, this.defaultPageSize)
        )
    }

    override fun idToModelCollection(ids: CollectionWithTotal<IdType>, models: List<DomainType>): CollectionWithTotal<DomainType> {
        return CollectionWithTotal(ids.totalItems, models)
    }

    override fun responseMetadata(list: CollectionWithTotal<DomainType>, requestContext: PagedListContext<AuthContext, Filters>): CollectionMetadata {
        return PagedCollectionMetadata(
            collectionSize = list.totalItems,
            //TODO(the heuristics for pageSize and pageEnd are very likely wrong)
            pageSize = list.size,
            maxPageSize = maxPageSize,
            defaultPageSize = defaultPageSize,
            pageStart = requestContext.paging.pageStart,
            pageEnd = requestContext.paging.pageSize + requestContext.paging.pageStart
        )
    }
}

data class PagedListContextImpl<AuthContext, Filters>(
    override val authContext: AuthContext,
    override val filters: Filters,
    override val paging: PagingParams
) : PagedListContext<AuthContext, Filters>
