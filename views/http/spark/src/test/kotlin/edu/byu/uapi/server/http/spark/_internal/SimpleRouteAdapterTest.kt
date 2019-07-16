package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.errors.HttpErrorMapper
import edu.byu.uapi.server.http.HttpHandler
import edu.byu.uapi.server.http.path.RoutePath
import kotlin.coroutines.CoroutineContext

internal class SimpleRouteAdapterTest
    : BaseSparkRouteAdapterTest<SimpleRouteAdapter>() {
    override fun buildAdapterWithSingleHandler(
        routePath: RoutePath,
        handler: HttpHandler,
        context: CoroutineContext,
        errorMapper: HttpErrorMapper
    ): SimpleRouteAdapter {
        return SimpleRouteAdapter(routePath, handler, context, errorMapper)
    }
}
