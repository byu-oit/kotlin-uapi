package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.HttpHandler
import edu.byu.uapi.server.http.HttpResponse
import edu.byu.uapi.server.http.errors.HttpErrorMapper
import edu.byu.uapi.server.http.errors.runHandlingErrors
import edu.byu.uapi.server.http.path.RoutePath
import kotlinx.coroutines.runBlocking
import spark.Request
import spark.Response
import spark.Route
import java.io.ByteArrayOutputStream
import kotlin.coroutines.CoroutineContext

internal abstract class BaseSparkRouteAdapter(
    private val routePath: RoutePath,
    private val context: CoroutineContext,
    private val errorMapper: HttpErrorMapper
) : Route {
    protected abstract fun getHandlerFor(req: Request): HttpHandler

    override fun handle(sparkReq: Request, sparkResp: Response): Any? {
        return errorMapper.runHandlingErrors {
            val uapiReq = SparkRequestAdapter(sparkReq, routePath)

            val handler = getHandlerFor(sparkReq)

            runBlocking(context = context) {
                handler.handle(uapiReq)
            }
        }.renderTo(sparkResp)
    }

    private fun HttpResponse.renderTo(sparkResp: Response): Any {
        sparkResp.status(status)
        headers.forEach { (k, v) -> sparkResp.header(k, v) }
        val body = this.body
        return if (body != null) {
            sparkResp.type(body.contentType)
            // Always be buffering (because there's a bug in Spark's stream handling)
            val baos = ByteArrayOutputStream()
            body.writeTo(baos)
            baos.toByteArray()
        } else {
            // Spark is buuuuuggy and doesn't know how to do a real no-content response. *sigh*
            ""
        }
    }
}

