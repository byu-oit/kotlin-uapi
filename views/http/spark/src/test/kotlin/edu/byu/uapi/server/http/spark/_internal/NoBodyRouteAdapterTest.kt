package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.engines.HttpRoute
import edu.byu.uapi.server.http.errors.HttpErrorMapper
import spark.Request
import kotlin.coroutines.CoroutineContext

internal class NoBodyRouteAdapterTest
    : BaseSparkRouteAdapterTest<NoBodyRouteAdapter>() {
    override fun buildAdapterWithSingleRoute(
        route: HttpRoute<Request>,
        context: CoroutineContext,
        errorMapper: HttpErrorMapper
    ): NoBodyRouteAdapter {
        return NoBodyRouteAdapter(route, context, errorMapper)
    }
}
