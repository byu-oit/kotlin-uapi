package edu.byu.uapi.http.spark

import com.google.gson.JsonObject
import edu.byu.uapi.http.*
import edu.byu.uapi.http.json.*
import edu.byu.uapi.server.UAPIRuntime
import edu.byu.uapi.spi.dictionary.TypeDictionary
import edu.byu.uapi.spi.rendering.Renderer
import org.slf4j.LoggerFactory
import spark.*
import java.io.File
import java.io.InputStream
import java.io.Writer


data class SparkConfig(
    val port: Int = defaultPort,
    override val jsonEngine: JsonEngine<*, *> = defaultJsonEngine
) : HttpEngineConfig {
    companion object {
        val defaultPort = 4567
        val defaultJsonEngine: JsonEngine<*, *> = JacksonEngine
    }
}

class SparkHttpEngine(config: SparkConfig) : HttpEngineBase<Service, SparkConfig>(config) {
    private val LOG = LoggerFactory.getLogger(SparkHttpEngine::class.java)
    init {
        super.doInit()
    }

    override fun startServer(config: SparkConfig): Service {
        return Service.ignite().apply {
            port(config.port)

            before { request, response ->
                request.attribute("uapi.start", System.currentTimeMillis())
                println("context path: " + request.contextPath())
                println("host: " + request.host())
                println("path info: " + request.pathInfo())
                println("uri: " + request.uri())
                println("url: " + request.url())
                println("servlet path: " + request.servletPath())
                println("headers: " + request.headers().map { it to request.headers(it) }.joinToString(", "))
            }

            after { request, response ->
                val start = request.attribute<Long>("uapi.start")
                val end = System.currentTimeMillis()
                println("Finished in ${end - start} ms")
                response.header("Content-Encoding", "gzip")
            }

            LOG.info("UAPI-HTTP Spark server is listening on port {}", config.port)
        }
    }

    override fun stop(server: Service) {
        server.stop()
        server.awaitStop()
    }

    override fun registerRoutes(
        server: Service,
        config: SparkConfig,
        routes: List<HttpRoute>,
        rootPath: String,
        runtime: UAPIRuntime<*>
    ) {
        server.path(rootPath) {
            routes.forEach {
                server.addRoute(it.method.toSpark(), it.toSpark(config, runtime.typeDictionary))
            }
        }
    }
}

fun <UserContext : Any> UAPIRuntime<UserContext>.startSpark(
    config: SparkConfig
): SparkHttpEngine {
    return SparkHttpEngine(config).also { it.register(this) }
}

fun <UserContext : Any> UAPIRuntime<UserContext>.startSpark(
    port: Int = SparkConfig.defaultPort,
    jsonEngine: JsonEngine<*, *> = SparkConfig.defaultJsonEngine
): SparkHttpEngine {
    return this.startSpark(SparkConfig(port, jsonEngine))
}

private fun HttpRoute.toSpark(config: SparkConfig, typeDictionary: TypeDictionary): RouteImpl {
    val path = pathParts.stringify(PathParamDecorators.COLON)
    return RouteImpl.create(path, this.handler.toSpark(config, typeDictionary))
}

private fun HttpHandler.toSpark(config: SparkConfig, typeDictionary: TypeDictionary): SparkHttpRoute {
    return SparkHttpRoute(this, config, typeDictionary)
}

class SparkHttpRoute(val handler: HttpHandler, val config: SparkConfig, val typeDictionary: TypeDictionary) : Route {
    override fun handle(
        request: Request,
        response: Response
    ): Any {
        val resp = handler.handle(SparkRequest(request))
        response.type("application/json")
        return resp.body.renderResponseBody(config.jsonEngine, typeDictionary)
    }
}

fun ResponseBody.renderResponseBody(json: JsonEngine<*, *>, typeDictionary: TypeDictionary): Any {
    return when (json) {
        is GsonTreeEngine -> {
            val result: JsonObject = this.render(json.renderer(typeDictionary, null))
            result.toString()
        }
        is JavaxJsonTreeEngine -> {
            val obj = this.render(json.renderer(typeDictionary, null))
            obj.toString()
        }
        is JavaxJsonStreamEngine -> {
            json.renderWithFile(typeDictionary) {
                this.render(it)
            }
        }
        is JacksonEngine -> {
            json.renderWithFile(typeDictionary) {
                this.render(it)
            }
        }
    }
}

inline fun <Output: Any> JsonEngine<Output, Writer>.renderWithFile(typeDictionary: TypeDictionary, render: (Renderer<Output>) -> Unit): InputStream {
    val file = File.createTempFile("uapi-runtime-render-buffer", ".tmp.json")
    println(file)
    file.deleteOnExit()

    file.bufferedWriter().use {
        val renderer = this.renderer(typeDictionary, it)
        render(renderer)
        it.flush()
    }
    return file.inputStream().buffered()//.afterClose { file.delete() }
}

fun InputStream.afterClose(afterClose: () -> Unit): InputStream {
    return CloseActionInputStream(this, afterClose)
}

class CloseActionInputStream(
    val wrapped: InputStream,
    val afterClose: () -> Unit
) : InputStream() {

    override fun skip(n: Long): Long {
        return wrapped.skip(n)
    }

    override fun available(): Int {
        return wrapped.available()
    }

    override fun reset() {
        wrapped.reset()
    }

    override fun close() {
        wrapped.close()
        afterClose()
    }

    override fun mark(readlimit: Int) {
        wrapped.mark(readlimit)
    }

    override fun markSupported(): Boolean {
        return wrapped.markSupported()
    }

    override fun read(): Int {
        return wrapped.read()
    }

    override fun read(b: ByteArray?): Int {
        return wrapped.read(b)
    }

    override fun read(
        b: ByteArray?,
        off: Int,
        len: Int
    ): Int {
        return wrapped.read(b, off, len)
    }
}

private fun HttpMethod.toSpark(): spark.route.HttpMethod {
    return when (this) {
        HttpMethod.GET -> spark.route.HttpMethod.get
        HttpMethod.PUT -> spark.route.HttpMethod.put
        HttpMethod.PATCH -> spark.route.HttpMethod.patch
        HttpMethod.POST -> spark.route.HttpMethod.post
        HttpMethod.DELETE -> spark.route.HttpMethod.delete
    }
}
