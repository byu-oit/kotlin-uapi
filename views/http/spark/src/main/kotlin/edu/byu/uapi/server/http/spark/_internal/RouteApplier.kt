package edu.byu.uapi.server.http.spark._internal

import spark.Route
import spark.Service
import spark.Spark

interface RouteApplier {
    fun get(path: String, accepts: String?, route: Route)
    fun put(path: String, accepts: String?, route: Route)
    fun patch(path: String, accepts: String?, route: Route)
    fun post(path: String, accepts: String?, route: Route)
    fun delete(path: String, accepts: String?, route: Route)
}

object StaticRouteApplier : RouteApplier {

    override fun get(path: String, accepts: String?, route: Route) {
        Spark.get(path, accepts, route)
    }

    override fun put(path: String, accepts: String?, route: Route) {
        Spark.put(path, accepts, route)
    }

    override fun patch(path: String, accepts: String?, route: Route) {
        Spark.patch(path, accepts, route)
    }

    override fun post(path: String, accepts: String?, route: Route) {
        Spark.post(path, accepts, route)
    }

    override fun delete(path: String, accepts: String?, route: Route) {
        Spark.delete(path, accepts, route)
    }
}

class ServiceRouteApplier(private val service: Service) : RouteApplier {
    override fun get(path: String, accepts: String?, route: Route) {
        service.get(path, accepts, route)
    }

    override fun put(path: String, accepts: String?, route: Route) {
        service.put(path, accepts, route)
    }

    override fun patch(path: String, accepts: String?, route: Route) {
        service.patch(path, accepts, route)
    }

    override fun post(path: String, accepts: String?, route: Route) {
        service.post(path, accepts, route)
    }

    override fun delete(path: String, accepts: String?, route: Route) {
        service.delete(path, accepts, route)
    }
}
