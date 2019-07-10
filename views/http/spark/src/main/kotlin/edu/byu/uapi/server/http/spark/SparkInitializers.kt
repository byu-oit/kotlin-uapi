package edu.byu.uapi.server.http.spark

import edu.byu.uapi.server.http.HttpRouteSource
import edu.byu.uapi.server.http.spark._internal.ServiceRouteApplier
import edu.byu.uapi.server.http.spark._internal.StaticRouteApplier
import edu.byu.uapi.server.http.spark._internal.applyRoutes

fun spark.Service.uapi(routes: HttpRouteSource) {
    ServiceRouteApplier(this).applyRoutes(routes)
}

fun addUApiToSpark(routes: HttpRouteSource) {
    StaticRouteApplier.applyRoutes(routes)
}
