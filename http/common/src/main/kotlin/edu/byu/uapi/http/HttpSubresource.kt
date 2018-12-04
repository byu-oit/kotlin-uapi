package edu.byu.uapi.http

import edu.byu.uapi.server.UAPIRuntime
import edu.byu.uapi.server.subresources.ListSubresourceRuntime
import edu.byu.uapi.server.subresources.SingletonSubresourceRuntime
import edu.byu.uapi.server.subresources.SubresourceRuntime
import edu.byu.uapi.server.subresources.list.*
import edu.byu.uapi.server.subresources.singleton.SingletonSubresourceDeleteHandler
import edu.byu.uapi.server.subresources.singleton.SingletonSubresourceFetchHandler
import edu.byu.uapi.server.subresources.singleton.SingletonSubresourceRequestHandler
import edu.byu.uapi.server.subresources.singleton.SingletonSubresourceUpdateHandler
import edu.byu.uapi.server.types.ModelHolder

sealed class HttpSubresource {
    abstract fun getRoutes(rootPath: List<PathPart>): List<HttpRoute>
}

fun <UserContext : Any, Parent : ModelHolder, Model : Any> SubresourceRuntime<UserContext, Parent, Model>.toHttp(
    runtime: UAPIRuntime<UserContext>,
    config: HttpEngineConfig
): HttpSubresource {
    return when (this) {
        is SingletonSubresourceRuntime -> HttpSingletonSubresource(runtime, config, this)
        is ListSubresourceRuntime<UserContext, Parent, *, Model, *> -> HttpListSubresource(runtime, config, this)
    }
}

class HttpSingletonSubresource<UserContext : Any, Parent : ModelHolder, Model : Any>(
    val runtime: UAPIRuntime<UserContext>,
    val config: HttpEngineConfig,
    val subresource: SingletonSubresourceRuntime<UserContext, Parent, Model>
) : HttpSubresource() {

    override fun getRoutes(rootPath: List<PathPart>): List<HttpRoute> {
        val path = rootPath + StaticPathPart(subresource.name)

        return subresource.availableOperations.map { handlerFor(it, path) }
    }

    private fun handlerFor(
        op: SingletonSubresourceRequestHandler<UserContext, Parent, Model, *>,
        path: List<PathPart>
    ): HttpRoute {
        return when (op) {
            is SingletonSubresourceFetchHandler ->
                HttpRoute(path, HttpMethod.GET, SingletonSubresourceFetchHttpHandler(runtime, op))
            is SingletonSubresourceDeleteHandler ->
                HttpRoute(path, HttpMethod.DELETE, SingletonSubresourceDeleteHttpHandler(runtime, op))
            is SingletonSubresourceUpdateHandler<UserContext, Parent, Model, *> ->
                HttpRoute(path, HttpMethod.PUT, SingletonSubresourceUpdateHttpHandler(runtime, op, config.jsonEngine))
        }
    }
}

class HttpListSubresource<UserContext: Any, Parent: ModelHolder, Id: Any, Model: Any>(
    val runtime: UAPIRuntime<UserContext>,
    val config: HttpEngineConfig,
    val subresource: ListSubresourceRuntime<UserContext, Parent, Id, Model, *>
) : HttpSubresource() {

    override fun getRoutes(rootPath: List<PathPart>): List<HttpRoute> {
        val basePath = rootPath + StaticPathPart(subresource.pluralName)
        val idPath = basePath + subresource.idReader.describe().toPathPart()

        return subresource.availableOperations.map { handlerFor(it, basePath, idPath) }
    }

    private fun handlerFor(
        op: ListSubresourceRequestHandler<UserContext, Parent, Id, Model, *, *>,
        rootPath: List<PathPart>,
        idPath: List<PathPart>
    ): HttpRoute {
        return when (op) {
            is ListSubresourceFetchHandler -> HttpRoute(
                idPath, HttpMethod.GET, ListSubresourceHttpHandler.Fetch(runtime, op)
            )
            is ListSubresourceListHandler<UserContext, Parent, Id, Model, *> -> HttpRoute(
                rootPath, HttpMethod.GET, ListSubresourceHttpHandler.List(runtime, op)
            )
            is ListSubresourceCreateHandler<UserContext, Parent, Id, Model, *, *> -> HttpRoute(
                rootPath, HttpMethod.POST, ListSubresourceHttpHandler.Create(runtime, op, config.jsonEngine)
            )
            is ListSubresourceUpdateHandler<UserContext, Parent, Id, Model, *, *> -> HttpRoute(
                idPath, HttpMethod.PUT, ListSubresourceHttpHandler.Update(runtime, op, config.jsonEngine)
            )
            is ListSubresourceDeleteHandler<UserContext, Parent, Id, Model, *> -> HttpRoute(
                idPath, HttpMethod.DELETE, ListSubresourceHttpHandler.Delete(runtime, op)
            )
        }
    }
}
