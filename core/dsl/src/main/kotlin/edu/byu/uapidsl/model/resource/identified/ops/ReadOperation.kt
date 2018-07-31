package edu.byu.uapidsl.model.resource.identified.ops

import edu.byu.uapidsl.model.resource.*
import edu.byu.uapidsl.model.resource.identified.*
import edu.byu.uapidsl.types.*

data class ReadOperation<Auth : Any, Id : Any, Model : Any>(
    override val authorized: Authorizer<IdentifiedReadContext<Auth, Id, Model>>,
    override val handle: Loader<IdentifiedLoadContext<Auth, Id>, Model>,
    val idExtractor: IdExtractor<Id, Model>
) : ResourceReadOperation<
    Auth,
    Model,
    IdentifiedResourceModelContext<Id, Model>,
    IdentifiedLoadContext<Auth, Id>
    >,
    DomainModelOps<Auth, Id, Model> {

    fun handleRequest(req: FetchResourceRequest<Auth, Id>): UAPIResponse<*> {
        val auth = req.authContext
        return idToResult(auth, req.id)
    }

    override fun modelToResult(authContext: Auth, model: Model): UAPIResponse<*> {
        val id = idExtractor(model)
        val authorized = authorized.invoke(
            ResourceReadContext.Default(authContext, IdentifiedResourceModelContext(id, model))
        )
        if (!authorized) {
            return ErrorResponse.notAuthorized()
        }
        return UAPISimpleResource(
            UAPIResourceMeta(ValidationResponse.OK),
            emptyMap(),//TODO: Implement Links
            model
        )
    }

    override fun idToModel(authContext: Auth, id: Id): Model? =
        handle.invoke(IdentifiedLoadContext(authContext, id))

    override fun idToResult(authContext: Auth, id: Id): UAPIResponse<*> {
        val model = idToModel(authContext, id) ?: return ErrorResponse.notFound()
        return modelToResult(authContext, model)
    }
}


data class IdentifiedLoadContext<AuthContext : Any, Id>(
    override val authContext: AuthContext,
    val id: Id
) : ResourceLoadContext<AuthContext>

data class ReadLoadContextImpl<AuthContext, IdType>(
    override val authContext: AuthContext,
    override val id: IdType
) : ReadLoadContext<AuthContext, IdType>

data class ReadContextImpl<AuthContext, IdType, ModelType>(
    override val authContext: AuthContext,
    override val id: IdType,
    override val resource: ModelType
) : ReadContext<AuthContext, IdType, ModelType>
