package edu.byu.uapidsl.http.implementation

import com.fasterxml.jackson.databind.ObjectWriter
import edu.byu.uapidsl.UApiModel
import edu.byu.uapidsl.dsl.*
import edu.byu.uapidsl.http.*
import edu.byu.uapidsl.model.PagedListOperation
import edu.byu.uapidsl.model.ResourceModel
import implementation.BaseListGet

class PagedListGet<AuthContext : Any, IdType : Any, ModelType : Any, Filters : Any>(
    apiModel: UApiModel<AuthContext>,
    resource: ResourceModel<AuthContext, IdType, ModelType>,
    override val list: PagedListOperation<AuthContext, IdType, ModelType, Filters>,
    jsonMapper: ObjectWriter
): BaseListGet<AuthContext, IdType, ModelType, Filters, PagedListContext<AuthContext, Filters>, CollectionWithTotal<IdType>, CollectionWithTotal<ModelType>>(
    apiModel, resource, jsonMapper
), GetHandler {

    override fun getRequestContext(request: GetRequest, authContext: AuthContext, filters: Filters): PagedListContext<AuthContext, Filters> {
        val pagingQueryParams = mapOf(
            "page_size" to setOf(list.defaultPageSize.toString()),
            "page_start" to setOf("0")
        ).plus(request.query)

        val paging = list.pageParamModel.reader.read(pagingQueryParams)

        return PagedListContextImpl(
            authContext, filters, paging
        )
    }

    override fun idToModelCollection(ids: CollectionWithTotal<IdType>, models: List<ModelType>): CollectionWithTotal<ModelType> {
        return CollectionWithTotal(ids.totalItems, models)
    }

    override fun getTotalSize(list: CollectionWithTotal<ModelType>) = list.totalItems

}

data class PagedListContextImpl<AuthContext, Filters>(
    override val authContext: AuthContext,
    override val filters: Filters,
    override val paging: PagingParams
) : PagedListContext<AuthContext, Filters>
