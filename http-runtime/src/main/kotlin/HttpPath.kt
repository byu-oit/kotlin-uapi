package edu.byu.uapidsl.http

import edu.byu.uapidsl.UApiModel
import edu.byu.uapidsl.http.implementation.*
import edu.byu.uapidsl.http.implementation.serialization.jacksonJsonMapper
import edu.byu.uapidsl.http.path.CompoundPathVariablePart
import edu.byu.uapidsl.http.path.PathPart
import edu.byu.uapidsl.http.path.SimplePathVariablePart
import edu.byu.uapidsl.http.path.StaticPathPart
import edu.byu.uapidsl.model.CreateOrUpdateOperation
import edu.byu.uapidsl.model.ResourceModel
import edu.byu.uapidsl.model.SimpleUpdateOperation

data class HttpPath(
    val pathParts: List<PathPart>,
    val handlers: MethodHandlers
)


data class MethodHandlers(
    val options: OptionsHandler,
    val get: GetHandler? = null,
    val post: PostHandler? = null,
    val put: PutHandler? = null,
    val patch: PatchHandler? = null,
    val delete: DeleteHandler? = null
)

val <AuthContext: Any> UApiModel<AuthContext>.httpPaths: List<HttpPath>
    get() {
        return this.resources.flatMap { pathsFor(this, it) }
    }

private fun <AuthContext: Any> pathsFor(apiModel: UApiModel<AuthContext>, resource: ResourceModel<AuthContext, *, *>): Iterable<HttpPath> {

    val basePath: List<PathPart> = listOf(StaticPathPart(resource.name))

    // TODO("Actually, you know, handle other types of IDs")
    val idParamName = resource.idModel.names.first()

    val resourcePath = basePath + SimplePathVariablePart(idParamName)

    val collection = collectionHandlers(apiModel, resource)

    val single = singleHandlers(apiModel, resource)

    val result = mutableListOf(
        HttpPath(resourcePath, single)
    )

    if (collection != null) {
        result += HttpPath(basePath, collection)
    }

    return result
}

private fun <AuthContext: Any, IdType: Any, ModelType: Any> collectionHandlers(uapiModel: UApiModel<AuthContext>, resource: ResourceModel<AuthContext, IdType, ModelType>): MethodHandlers? {
    if (resource.list == null && resource.create == null) {
        return null
    }

    val create = resource.create

    val get = if (resource.list != null) PagedListGet() else null
    val post = if (create != null) {
        SimplePost(uapiModel, resource, create, jacksonJsonMapper)
    } else null

    return MethodHandlers(
        options = AuthorizationAwareOptions(),
        get = get,
        post = post
    )
}

private fun <AuthContext: Any> singleHandlers(uapiModel: UApiModel<AuthContext>, resource: ResourceModel<AuthContext, *, *>): MethodHandlers {
    val get = ResourceGet(uapiModel, resource, jacksonJsonMapper)
    val put = when(resource.update) {
        is SimpleUpdateOperation<AuthContext, *, *, *> -> SimplePut()
        is CreateOrUpdateOperation<AuthContext, *, *, *> -> MaybeCreatePut()
        null -> null
    }
    val delete = if (resource.delete != null) SimpleDelete() else null
    val options = AuthorizationAwareOptions()

    return MethodHandlers(
        options = options,
        get = get,
        put = put,
        delete = delete
    )
}

typealias PathParamDecorator = (part: String) -> String

object PathParamDecorators {
    val COLON: PathParamDecorator = { ":$it" }
    val CURLY_BRACE: PathParamDecorator = {"{$it}"}
    val NONE: PathParamDecorator = {it}
}

fun stringifyPaths(pathParts: List<PathPart>, decorator: PathParamDecorator = PathParamDecorators.NONE): String {
    return pathParts.joinToString(separator = "/", prefix = "/") { part ->
        when (part) {
            is StaticPathPart -> part.part
            is SimplePathVariablePart -> decorator(part.name)
            is CompoundPathVariablePart -> part.names.joinToString(separator = ",", transform = decorator)
        }
    }
}



