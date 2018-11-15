package edu.byu.uapi.http.spark

import edu.byu.uapi.http.*
import edu.byu.uapi.server.UAPIRuntime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spark.*
import java.io.InputStream
import java.nio.file.Files

private val LOG: Logger = LoggerFactory.getLogger("edu.byu.uapi.http.spark.StartSpark")

fun <UserContext: Any> UAPIRuntime<UserContext>.startSpark(
    port: Int = 4567
): UAPISparkServer<UserContext> {
    val resources = this.resources().map {
        HttpIdentifiedResource(this, it.value)
    }
    val routes = resources.flatMap { it -> it.routes }

    val spark = Service.ignite()

    spark.port(port)

    spark.before { request, response ->
        println("context path: " + request.contextPath())
        println("host: " + request.host())
        println("path info: " + request.pathInfo())
        println("uri: " + request.uri())
        println("url: " + request.url())
        println("servlet path: " + request.servletPath())
        println("headers: " + request.headers().map { it to request.headers(it) }.joinToString(", "))
    }

    routes.forEach {
        spark.addRoute(it.method.toSpark(), it.toSpark())
    }

    LOG.info("UAPI-Spark is listening on port {}", port)

    return UAPISparkServer(port, resources, spark)
}

private fun HttpRoute.toSpark(): RouteImpl {
    val path = pathParts.stringify(PathParamDecorators.COLON)
    return RouteImpl.create(path, this.handler.toSpark())
}

private fun HttpHandler.toSpark() = SparkHttpRoute(this)

class SparkHttpRoute(val handler: HttpHandler): Route {
    override fun handle(
        request: Request,
        response: Response
    ): InputStream {
        val resp = handler.handle(SparkRequest(request))
        response.type("application/json")
        val temp = Files.createTempFile("uapi-runtime-response-buffer", ".json")
        val file = temp.toFile()
        file.writer().buffered().use { resp.body.toWriter(it) }

        return file.inputStream().buffered().afterClose { temp.toFile().delete() }
    }
}

fun InputStream.afterClose(afterClose: () -> Unit): InputStream {
    return CloseActionInputStream(this, afterClose)
}

class CloseActionInputStream(val wrapped: InputStream, val afterClose: () -> Unit): InputStream() {

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

data class UAPISparkServer<UserContext: Any>(
    val port: Int,
    val resources: List<HttpIdentifiedResource<UserContext, *, *>>,
    val server: Service
)
