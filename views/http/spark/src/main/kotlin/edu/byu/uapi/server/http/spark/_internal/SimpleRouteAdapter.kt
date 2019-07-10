package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.HttpHandler
import spark.Request
import kotlin.coroutines.CoroutineContext

internal class SimpleRouteAdapter(
    internal val handler: HttpHandler,
    context: CoroutineContext
): BaseSparkRouteAdapter(context) {
    override fun getHandlerFor(req: Request) = handler
}
