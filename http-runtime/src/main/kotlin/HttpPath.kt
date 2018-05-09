package edu.byu.uapidsl.http

import edu.byu.uapidsl.UApiModel
import edu.byu.uapidsl.http.implementation.*
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
        return this.resources.flatMap(::pathsFor)
    }

private fun <AuthContext> pathsFor(resource: ResourceModel<AuthContext, *, *>): Iterable<HttpPath> {

    val basePath: List<PathPart> = listOf(StaticPathPart(resource.name))

    val resourcePath = basePath + SimplePathVariablePart(resource.name + "_id")

    val collection = collectionHandlers(resource)

    val single = singleHandlers(resource)

    val result = mutableListOf(
        HttpPath(resourcePath, single)
    )

    if (collection != null) {
        result += HttpPath(basePath, collection)
    }

    return result
}

private fun <AuthContext> collectionHandlers(resource: ResourceModel<AuthContext, *, *>): MethodHandlers? {
    if (resource.list == null && resource.create == null) {
        return null
    }
    val get = if (resource.list != null) PagedListGet() else null
    val post = if (resource.create != null) SimplePost() else null

    return MethodHandlers(
        options = AuthorizationAwareOptions(),
        get = get,
        post = post
    )
}

private fun <AuthContext> singleHandlers(resource: ResourceModel<AuthContext, *, *>): MethodHandlers {
    val get = ResourceGet()
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


