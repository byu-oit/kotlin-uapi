package implementation

import com.fasterxml.jackson.databind.ObjectWriter
import edu.byu.uapidsl.UApiModel
import edu.byu.uapidsl.dsl.ListContext
import edu.byu.uapidsl.http.GetHandler
import edu.byu.uapidsl.http.GetRequest
import edu.byu.uapidsl.http.implementation.BaseHttpHandler
import edu.byu.uapidsl.http.implementation.ReadContextImpl
import edu.byu.uapidsl.http.implementation.ReadLoadContextImpl
import edu.byu.uapidsl.model.ListOperation
import edu.byu.uapidsl.model.ResourceModel
import edu.byu.uapidsl.types.*
import either.fold

abstract class BaseListGet<AuthContext : Any, IdType : Any, ModelType : Any, Filters : Any, RequestContext: ListContext<AuthContext, Filters>, IdCollection: Collection<IdType>, ModelCollection: Collection<ModelType>>(
    apiModel: UApiModel<AuthContext>,
    protected val resource: ResourceModel<AuthContext, IdType, ModelType>,
    jsonMapper: ObjectWriter
) : BaseHttpHandler<GetRequest, AuthContext>(
    apiModel, jsonMapper
), GetHandler {

    protected abstract val list: ListOperation<AuthContext, IdType, ModelType, Filters, RequestContext, IdCollection, ModelCollection>

    private val itemAuthorizer = resource.operations.read.authorization
    private val itemLoader = resource.operations.read.handle
    private val idExtractor = resource.idExtractor

    private val loader: RequestContext.() -> ModelCollection by lazy {
         this.list.handle.fold(
            { idBasedLoader(it) },
            { it }
        )
    }

    protected abstract fun getRequestContext(request: GetRequest, authContext: AuthContext, filters: Filters): RequestContext

    override fun handleAuthenticated(request: GetRequest, authContext: AuthContext): UAPIResponse<*> {
        val filters = list.filterType.reader.read(request.query)

        val context = this.getRequestContext(request, authContext, filters)

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
                collectionSize = getTotalSize(results),
                validationResponse = ValidationResponse.OK
            )
        )
    }

    protected abstract fun idToModelCollection(ids: IdCollection, models: List<ModelType>): ModelCollection
    protected abstract fun getTotalSize(list: ModelCollection): Int

    private fun idBasedLoader(handler: (ctx: RequestContext) -> IdCollection): (ctx: RequestContext) -> ModelCollection {
        return { ctx: RequestContext ->
            val ids = handler.invoke(ctx)
           val models = ids.map { itemLoader.invoke(ReadLoadContextImpl(ctx.authContext, it))!! }
            idToModelCollection(ids, models)
        }
    }

}
