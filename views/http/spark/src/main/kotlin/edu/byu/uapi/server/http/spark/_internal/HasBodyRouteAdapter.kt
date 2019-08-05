package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.engines.HttpRoute
import edu.byu.uapi.server.http.errors.HttpErrorMapper
import edu.byu.uapi.server.http.errors.UAPIHttpMissingHeaderError
import edu.byu.uapi.server.http.errors.UAPIHttpUnrecognizedContentTypeError
import spark.Request
import spark.utils.MimeParse
import kotlin.coroutines.CoroutineContext

internal class HasBodyRouteAdapter(
    routeList: List<HttpRoute<Request>>,
    context: CoroutineContext,
    errorMapper: HttpErrorMapper
) : BaseSparkRouteAdapter(context, errorMapper) {

    private val routes = routeList.associateBy { it.consumes ?: "*/*" }

    override fun getRouteFor(req: Request): HttpRoute<Request> {
        val requestType = req.contentType()
            ?: throw UAPIHttpMissingHeaderError("Content-Type")
        val type = MimeParse.bestMatch(routes.keys, requestType)
        if (type.isNullOrBlank()) {
            throw UAPIHttpUnrecognizedContentTypeError(routes.keys.toList())
        }
        return routes.getValue(type)
    }

    override fun toString(): String {
        return "HasBodyRouteAdapter: (${routes.keys})"
    }
}
