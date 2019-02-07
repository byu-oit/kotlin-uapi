package edu.byu.uapi.http

import edu.byu.uapi.http.docs.DialectDocSource
import edu.byu.uapi.http.docs.DocSource
import edu.byu.uapi.http.docs.SwaggerViewerDocs
import edu.byu.uapi.http.json.JsonEngine
import edu.byu.uapi.model.UAPIModel
import edu.byu.uapi.model.dialect.UAPIDialect
import edu.byu.uapi.model.serialization.UAPISerializationFormat
import edu.byu.uapi.server.UAPIRuntime
import edu.byu.uapi.server.resources.list.ListResourceRuntime
import org.slf4j.LoggerFactory

abstract class HttpEngineBase<Server : Any, Config : HttpEngineConfig>(
    val config: Config
) {
    @Suppress("PrivatePropertyName")
    private val LOG = LoggerFactory.getLogger(this.javaClass)

    abstract fun startServer(config: Config): Server

    private lateinit var _server: Server

    val server: Server
        get() = this._server

    protected fun doInit() {
        _server = startServer(config)
    }

    private fun checkInitialized() {
        if (!this::_server.isInitialized) {
            throw IllegalStateException(this::class.qualifiedName + " must call super.doInit() in its constructor.")
        }
    }

    fun <UserContext : Any> register(
        runtime: UAPIRuntime<UserContext>,
        rootPath: String = ""
    ) {
        checkInitialized()
        val resources = runtime.resources().map {
            when (it) {
                is ListResourceRuntime<UserContext, *, *, *> -> HttpListResource(runtime, config, it)
            }
        }
        val routes = resources.flatMap { it.routes }

        registerRoutes(_server, config, routes, rootPath, runtime)

        registerDocRoutes(_server, config, docRoutes(runtime.model), rootPath, runtime)

        LOG.info("Finished registering routes for root path '/$rootPath'")
    }

    private fun docRoutes(model: UAPIModel): List<DocRoute> {
        val rootPath: List<PathPart> = listOf(StaticPathPart("\$docs"))
        return UAPIDialect.findAllDialects()
            .flatMap { d -> UAPISerializationFormat.values().map { d to it } }
            .map { DialectDocSource(model, it.first, it.second) }
            .map { DocRoute(it, rootPath) } + DocRoute(SwaggerViewerDocs(), rootPath)
    }

    abstract fun stop(server: Server)

    abstract fun registerRoutes(
        server: Server,
        config: Config,
        routes: List<HttpRoute>,
        rootPath: String,
        runtime: UAPIRuntime<*>
    )

    abstract fun registerDocRoutes(
        server: Server,
        config: Config,
        docRoutes: List<DocRoute>,
        rootPath: String,
        runtime: UAPIRuntime<*>
    )
}

data class DocRoute (
    val path: List<PathPart>,
    val source: DocSource
) {
    constructor(source: DocSource, basePath: List<PathPart>): this(
        basePath + StaticPathPart(source.name),
        source
    )
}

interface HttpEngineConfig {
    val jsonEngine: JsonEngine<*, *>
}
