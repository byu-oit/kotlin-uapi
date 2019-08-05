package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.engines.HttpRoute
import edu.byu.uapi.server.http.errors.HttpErrorMapper
import spark.Request
import kotlin.coroutines.CoroutineContext

internal class NoBodyRouteAdapter(
    private val route: HttpRoute<Request>,
    context: CoroutineContext,
    errorHandler: HttpErrorMapper
): BaseSparkRouteAdapter(
    context,
    errorHandler
) {
    override fun getRouteFor(req: Request) = route

    override fun toString(): String {
        return "NoBodyRouteAdapter"
    }
}
