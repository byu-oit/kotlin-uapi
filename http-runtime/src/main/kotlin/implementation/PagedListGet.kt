package edu.byu.uapidsl.http.implementation

import com.fasterxml.jackson.databind.ObjectWriter
import edu.byu.uapidsl.UApiModel
import edu.byu.uapidsl.dsl.*
import edu.byu.uapidsl.http.*
import edu.byu.uapidsl.model.PagedListOperation
import edu.byu.uapidsl.model.ResourceModel
import edu.byu.uapidsl.types.*
import either.fold

class PagedListGet<AuthContext : Any, IdType : Any, ModelType : Any, Filters : Any>(
    apiModel: UApiModel<AuthContext>,
    private val resource: ResourceModel<AuthContext, IdType, ModelType>,
    private val list: PagedListOperation<AuthContext, IdType, ModelType, Filters>,
    jsonMapper: ObjectWriter
): BaseHttpHandler<GetRequest, AuthContext>(
    apiModel, jsonMapper
), GetHandler {

    private val itemAuthorizer = resource.operations.read.authorization
    private val itemLoader = resource.operations.read.handle
    private val handler = list.handle
    private val idExtractor = resource.idExtractor

    private val loader: PagedListLoader<AuthContext, ModelType, Filters>

    init {
        loader = handler.fold(
            { idBasedLoader(it) },
            { it }
        )
    }

    override fun handleAuthenticated(request: GetRequest, authContext: AuthContext): UAPIResponse<*> {
        val filters = list.filterType.reader.read(request.query)

        val pagingQueryParams = mapOf(
            "page_size" to setOf(list.defaultPageSize.toString()),
        "page_start" to setOf("0")
        ).plus(request.query)

        val paging = list.pageParamModel.reader.read(pagingQueryParams)

        val context = PagedListContextImpl(authContext, filters, paging)

        val results = this.loader.invoke(context)

        val resources: List<UAPIResponse<*>> = results.map {
            val id = idExtractor.invoke(it)
            if (!itemAuthorizer.invoke(ReadContextImpl(authContext, id, it))) {
                ErrorResponse(UAPIErrorMetadata(ValidationResponse(403, "Unauthorized"), listOf("Unauthorized")))
            } else {
                val meta = UAPIResourceMeta()
                SimpleResourceResponse(
                    mapOf("basic" to UAPISimpleResource(
                        metadata = meta,
                        properties = it
                    )),
                    meta
                )
            }
        }
        return UAPIListResponse(
            values = resources,
            metadata = SimpleCollectionMetadata(
                collectionSize = results.totalItems,
                validationResponse = ValidationResponse.OK
            )
        )
    }

    private fun idBasedLoader(handler: PagedListHandler<AuthContext, Filters, IdType>): PagedListLoader<AuthContext, ModelType, Filters> {
        return { ctx ->
            val result = handler.invoke(ctx)
            CollectionWithTotal(
                values = result.map { itemLoader.invoke(ReadLoadContextImpl(ctx.authContext, it))!! },
                totalItems = result.totalItems
            )
        }
    }
}

internal typealias PagedListLoader<AuthContext, ModelType, Filters> =
    (PagedListContext<AuthContext, Filters>) -> CollectionWithTotal<ModelType>

data class PagedListContextImpl<AuthContext, Filters>(
    override val authContext: AuthContext,
    override val filters: Filters,
    override val paging: PagingParams
) : PagedListContext<AuthContext, Filters>
