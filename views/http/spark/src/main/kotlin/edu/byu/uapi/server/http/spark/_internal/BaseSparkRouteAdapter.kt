package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.HttpHandler
import edu.byu.uapi.server.http.path.RoutePath
import kotlinx.coroutines.runBlocking
import spark.Request
import spark.Response
import spark.Route
import java.io.ByteArrayOutputStream
import kotlin.coroutines.CoroutineContext

internal abstract class BaseSparkRouteAdapter(
    private val routePath: RoutePath,
    private val context: CoroutineContext
) : Route {
    protected abstract fun getHandlerFor(req: Request): HttpHandler

    override fun handle(sparkReq: Request, sparkResp: Response): Any? {
        val uapiReq = SparkRequestAdapter(sparkReq, routePath)

        val handler = getHandlerFor(sparkReq)

        val uapiResp = runBlocking(context = context) {
            handler.handle(uapiReq)
        }

        sparkResp.status(uapiResp.status)
        uapiResp.headers.forEach { (k, v) -> sparkResp.header(k, v) }
        val body = uapiResp.body
        if (body != null) {
            sparkResp.type(body.contentType)
            // Always be buffering (because there's a bug in Spark's stream handling)
            val baos = ByteArrayOutputStream()
            body.writeTo(baos)
            return baos.toByteArray()
        }
        // Spark is buuuuuggy and doesn't know how to do a real no-content response. *sigh*
        return ""
    }
}

