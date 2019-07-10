package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.HttpHandler
import kotlin.coroutines.CoroutineContext

internal class SimpleRouteAdapterTest
    : BaseSparkRouteAdapterTest<SimpleRouteAdapter>() {
    override fun buildAdapterWithSingleHandler(
        handler: HttpHandler,
        context: CoroutineContext
    ): SimpleRouteAdapter {
        return SimpleRouteAdapter(handler, context)
    }
}
