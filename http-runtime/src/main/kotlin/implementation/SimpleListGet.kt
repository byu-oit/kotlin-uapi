package edu.byu.uapidsl.http.implementation

import com.fasterxml.jackson.databind.ObjectWriter
import edu.byu.uapidsl.UApiModel
import edu.byu.uapidsl.dsl.ListContext
import edu.byu.uapidsl.dsl.ListHandler
import edu.byu.uapidsl.http.GetHandler
import edu.byu.uapidsl.http.GetRequest
import edu.byu.uapidsl.model.ResourceModel
import edu.byu.uapidsl.model.SimpleListOperation
import edu.byu.uapidsl.types.UAPIResource
import edu.byu.uapidsl.types.UAPIResponse
import either.fold

class SimpleListGet<AuthContext: Any, IdType: Any, ModelType: Any, Filters: Any>(
    apiModel: UApiModel<AuthContext>,
    private val resource: ResourceModel<AuthContext, IdType, ModelType>,
    private val list: SimpleListOperation<AuthContext, IdType, ModelType, Filters>,
    jsonMapper: ObjectWriter
): BaseHttpHandler<GetRequest, AuthContext>(
    apiModel, jsonMapper
), GetHandler {

    private val itemAuthorizer = resource.operations.read.authorization
    private val itemLoader = resource.operations.read.handle
    private val handler = list.handle

    private val loader: ListLoader<AuthContext, ModelType, Filters>

    init {
        loader = handler.fold(
            {idBasedLoader(it)},
            {it}
        )
    }

    override fun handleAuthenticated(request: GetRequest, authContext: AuthContext): UAPIResponse<*> {
        val filters = list.filterType.reader.read(request.query)

        val context = ListContextImpl(authContext, filters)

        val results = this.loader.invoke(context)

        val resources: List<UAPIResource> = results.map {
            if (!itemAuthorizer.invoke(ReadContextImpl(authContext, )))
        }
    }

    private fun idBasedLoader(handler: ListHandler<AuthContext, Filters, IdType>): ListLoader<AuthContext, ModelType, Filters> {
        return { ctx ->
            handler.invoke(ctx)
                .map { itemLoader.invoke(ReadLoadContextImpl(ctx.authContext, it))!! }
        }
    }

}

//internal interface ListLoader<AuthContext, Filters, ModelType> {
//
//}

internal typealias ListLoader<AuthContext, ModelType, Filters> =
    (ListContext<AuthContext, Filters>) -> Collection<ModelType>

data class ListContextImpl<AuthContext, Filters>(
    override val authContext: AuthContext,
    override val filters: Filters
): ListContext<AuthContext, Filters>
