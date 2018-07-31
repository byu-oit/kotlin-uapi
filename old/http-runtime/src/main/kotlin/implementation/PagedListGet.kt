package edu.byu.uapidsl.http.implementation

import com.fasterxml.jackson.databind.ObjectWriter
import edu.byu.uapidsl.UApiModel
import edu.byu.uapidsl.dsl.*
import edu.byu.uapidsl.http.*
import edu.byu.uapidsl.model.resource.identified.ops.PagedListOperation
import edu.byu.uapidsl.model.resource.identified.IdentifiedResource
import implementation.BaseListGet

class PagedListGet<AuthContext : Any, IdType : Any, ModelType : Any, Filters : Any>(
    apiModel: UApiModel<AuthContext>,
    resource: IdentifiedResource<AuthContext, IdType, ModelType>,
    override val list: PagedListOperation<AuthContext, IdType, ModelType, Filters>,
    jsonMapper: ObjectWriter
): BaseListGet<AuthContext, IdType, ModelType, Filters, PagedListContext<AuthContext, Filters>, CollectionWithTotal<IdType>, CollectionWithTotal<ModelType>>(
    apiModel, resource, jsonMapper
), GetHandler {

    override fun extractPagingParams(request: GetRequest): PagingParams? {
        return list.pageParamModel.reader.read(request.query)
    }
}
