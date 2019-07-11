package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.HttpHandler
import edu.byu.uapi.server.http.path.RoutePath
import spark.Request
import kotlin.coroutines.CoroutineContext

internal class SimpleRouteAdapter(
    routePath: RoutePath,
    internal val handler: HttpHandler,
    context: CoroutineContext
): BaseSparkRouteAdapter(routePath, context) {
    override fun getHandlerFor(req: Request) = handler
}
