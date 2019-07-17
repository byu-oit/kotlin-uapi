package edu.byu.uapi.server.http.integrationtest.dsl

import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.RequestFactory
import edu.byu.uapi.server.http.engines.RouteMethod

fun RequestFactory.request(method: RouteMethod, path: String, parameters: Parameters? = null): Request {
    return request(method.fuel, path, parameters)
}

fun Request.type(type: String): Request {
    return header("Content-Type", type)
}

fun Request.accept(type: String): Request {
    return header("Accept", type)
}

val RouteMethod.fuel
    get() = when (this) {
        RouteMethod.GET    -> Method.GET
        RouteMethod.PUT    -> Method.PUT
        RouteMethod.PATCH  -> Method.PATCH
        RouteMethod.POST   -> Method.POST
        RouteMethod.DELETE -> Method.DELETE
    }
