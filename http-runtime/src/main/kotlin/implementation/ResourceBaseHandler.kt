package edu.byu.uapidsl.http.implementation

import com.fasterxml.jackson.databind.ObjectMapper
import edu.byu.uapidsl.UApiModel
import edu.byu.uapidsl.dsl.ReadLoadContext
import edu.byu.uapidsl.http.*
import edu.byu.uapidsl.model.ResourceModel
import edu.byu.uapidsl.types.UAPIResponse

//class ResourceBaseHandler

abstract class ResourceBaseHandler<Request : HttpRequest, AuthContext : Any, IdType : Any, ModelType : Any, AuthzContext: Any>(
    apiModel: UApiModel<AuthContext>,
    jsonMapper: ObjectMapper,
    protected val resource: ResourceModel<AuthContext, IdType, ModelType>
) : BaseHttpHandler<Request, AuthContext>(apiModel, jsonMapper) {

    private val loader = resource.read.handle

    final override fun handleAuthenticated(request: Request, authContext: AuthContext): UAPIResponse<*> {
        val id = idFrom(request.path)

        val model = loadModel(id, authContext)

        val authzContext = getAuthzContext(request, authContext, id, model)

        val authorized = authzContext.authorizer()
        if (!authorized) {
            throw NotAuthorizedToViewException()
        }

        return handleResource(request, authContext, id, model)
    }

    abstract val authorizer: AuthzContext.() -> Boolean

    abstract fun getAuthzContext(request: Request, authContext: AuthContext, id: IdType, model: ModelType): AuthzContext

    abstract fun handleResource(request: Request, authContext: AuthContext, id: IdType, model: ModelType): UAPIResponse<*>

    private fun idFrom(pathParams: PathParams): IdType {
        if (this.resource.idModel.isCompound) {
            TODO("Compound path parameters aren't supported yet")
        }
        if (this.resource.idModel.type.type != String::class) {
            TODO("Non-string ID types are not yet supported")
        }
        val name = this.resource.idModel.names.first()
        println("ID Name: $name, params: $pathParams")
        val value = pathParams[name]
//        if (!this.resource.idModel.type.type.isInstance(value)) {
//            throw IllegalStateException("Id value is not the proper type")
//        }
        @Suppress("UNCHECKED_CAST")
        return pathParams[name] as IdType
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun loadModel(id: IdType, authContext: AuthContext): ModelType {
        //TODO: Add implementation classes for context objects
        val loadContext = object : ReadLoadContext<AuthContext, IdType> {
            override val authContext: AuthContext = authContext
            override val id: IdType = id
        }

        return loadContext.loader() ?: throw NotFoundException(resource.name, id)
    }


}
