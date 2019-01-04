package edu.byu.uapi.http

import edu.byu.uapi.http.json.JsonEngine
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

        LOG.info("Finished registering routes for root path '/$rootPath'")
    }

    abstract fun stop(server: Server)

    abstract fun registerRoutes(
        server: Server,
        config: Config,
        routes: List<HttpRoute>,
        rootPath: String,
        runtime: UAPIRuntime<*>
    )
}

interface HttpEngineConfig {
    val jsonEngine: JsonEngine<*, *>
}
