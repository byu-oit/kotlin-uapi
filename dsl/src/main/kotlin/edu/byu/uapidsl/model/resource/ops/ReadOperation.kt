package edu.byu.uapidsl.model.resource.ops

import edu.byu.uapidsl.dsl.*
import edu.byu.uapidsl.model.resource.DomainModelOps
import edu.byu.uapidsl.model.resource.FetchResourceRequest
import edu.byu.uapidsl.types.*

data class ReadOperation<AuthContext, IdType, DomainType: Any>(
    val authorization: ReadAuthorizer<AuthContext, IdType, DomainType>,
    val handle: ReadHandler<AuthContext, IdType, DomainType>,
    val idExtractor: IdExtractor<IdType, DomainType>
): DomainModelOps<AuthContext, IdType, DomainType> {

    fun handleRequest(req: FetchResourceRequest<AuthContext, IdType>): UAPIResponse<*> {
        val auth = req.authContext
        return idToResult(auth, req.id)
    }

    override fun modelToResult(authContext: AuthContext, model: DomainType): UAPIResponse<*> {
        val id = idExtractor(model)
        val authorized = authorization.invoke(ReadContextImpl(authContext, id, model))
        if (!authorized) {
            return ErrorResponse.notAuthorized()
        }
        return UAPISimpleResource(
            UAPIResourceMeta(ValidationResponse.OK),
            emptyMap(),//TODO: Implement Links
            model
        )
    }

    override fun idToModel(authContext: AuthContext, id: IdType): DomainType? =
        handle.invoke(ReadLoadContextImpl(authContext, id))

    override fun idToResult(authContext: AuthContext, id: IdType): UAPIResponse<*> {
        val model = idToModel(authContext, id) ?: return ErrorResponse.notFound()
        return modelToResult(authContext, model)
    }
}

data class ReadLoadContextImpl<AuthContext, IdType>(
    override val authContext: AuthContext,
    override val id: IdType
) : ReadLoadContext<AuthContext, IdType>

data class ReadContextImpl<AuthContext, IdType, ModelType>(
    override val authContext: AuthContext,
    override val id: IdType,
    override val resource: ModelType
): ReadContext<AuthContext, IdType, ModelType>
