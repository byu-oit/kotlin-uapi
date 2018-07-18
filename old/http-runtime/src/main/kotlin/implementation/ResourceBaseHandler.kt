package edu.byu.uapidsl.http.implementation

import com.fasterxml.jackson.databind.ObjectWriter
import edu.byu.uapidsl.UApiModel
import edu.byu.uapidsl.dsl.ReadLoadContext
import edu.byu.uapidsl.http.HttpRequest
import edu.byu.uapidsl.http.NotAuthorizedToViewException
import edu.byu.uapidsl.http.NotFoundException
import edu.byu.uapidsl.http.PathParams
import edu.byu.uapidsl.model.resource.ResourceModel
import edu.byu.uapidsl.types.UAPIResponse

//class ResourceBaseHandler

abstract class ResourceBaseHandler<Request : HttpRequest, AuthContext : Any, IdType : Any, ModelType : Any, RequestContext : Any>(
    apiModel: UApiModel<AuthContext>,
    jsonWriter: ObjectWriter,
    protected val resource: ResourceModel<AuthContext, IdType, ModelType>
) : BaseHttpHandler<Request, AuthContext>(apiModel, jsonWriter) {

    private val loader = resource.operations.read.handle

    final override fun handleAuthenticated(request: Request, authContext: AuthContext): UAPIResponse<*> {
        val id = idFrom(request.path)

        val model = loadModel(id, authContext)

        val requestContext = createRequestContext(request, authContext, id, model)

        val authorized = requestContext.authorizer()
        if (!authorized) {
            throw NotAuthorizedToViewException()
        }

        return handleResource(request, authContext, id, model, requestContext)
    }

    abstract val authorizer: RequestContext.() -> Boolean

    abstract fun createRequestContext(request: Request, authContext: AuthContext, id: IdType, model: ModelType): RequestContext

    abstract fun handleResource(request: Request, authContext: AuthContext, id: IdType, model: ModelType, requestContext: RequestContext): UAPIResponse<*>

    private fun idFrom(pathParams: PathParams): IdType {
        return this.resource.idModel.reader.read(pathParams)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun loadModel(id: IdType, authContext: AuthContext): ModelType {
        //TODO: Add implementation classes for context objects
        val loadContext = ReadLoadContextImpl(authContext, id)

        return loadContext.loader() ?: throw NotFoundException(resource.name, id)
    }


}

data class ReadLoadContextImpl<AuthContext, IdType>(
    override val authContext: AuthContext,
    override val id: IdType
) : ReadLoadContext<AuthContext, IdType>
