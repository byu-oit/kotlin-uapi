package edu.byu.uapi.server.http.integrationtest.dsl

import edu.byu.uapi.server.http.path.CompoundVariablePathPart
import edu.byu.uapi.server.http.path.PathFormatters
import edu.byu.uapi.server.http.path.SingleVariablePathPart
import edu.byu.uapi.server.http.path.staticPart
import edu.byu.uapi.server.http.path.unformat

fun RoutingInit.path(vararg parts: String, init: RoutingInit.() -> Unit) {
    path(parts.map { staticPart(it) }, init)
}

fun RoutingInit.pathSpec(pathSpec: String, init: RoutingInit.() -> Unit) {
    val parts = PathFormatters.CURLY_BRACE.unformat(pathSpec)
    path(parts, init)
}

fun RoutingInit.pathParam(name: String, init: RoutingInit.() -> Unit) {
    path(listOf(SingleVariablePathPart(name)), init)
}

fun RoutingInit.pathParam(vararg names: String, init: RoutingInit.() -> Unit) {
    path(listOf(CompoundVariablePathPart(names.toList())), init)
}

