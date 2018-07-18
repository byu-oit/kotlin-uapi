package edu.byu.uapidsl.http.implementation

import com.fasterxml.jackson.databind.ObjectWriter
import edu.byu.uapidsl.UApiModel
import edu.byu.uapidsl.dsl.ListContext
import edu.byu.uapidsl.dsl.PagingParams
import edu.byu.uapidsl.http.GetHandler
import edu.byu.uapidsl.http.GetRequest
import edu.byu.uapidsl.model.resource.ResourceModel
import edu.byu.uapidsl.model.resource.ops.SimpleListOperation
import implementation.BaseListGet

class SimpleListGet<AuthContext : Any, IdType : Any, ModelType : Any, Filters : Any>(
    apiModel: UApiModel<AuthContext>,
    resource: ResourceModel<AuthContext, IdType, ModelType>,
    override val list: SimpleListOperation<AuthContext, IdType, ModelType, Filters>,
    jsonMapper: ObjectWriter
) : BaseListGet<AuthContext, IdType, ModelType, Filters, ListContext<AuthContext, Filters>, Collection<IdType>, Collection<ModelType>>(
    apiModel, resource, jsonMapper
), GetHandler {
    override fun extractPagingParams(request: GetRequest): PagingParams? = null
}
