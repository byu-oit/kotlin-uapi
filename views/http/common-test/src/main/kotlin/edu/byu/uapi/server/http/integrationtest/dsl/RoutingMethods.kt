package edu.byu.uapi.server.http.integrationtest.dsl

import edu.byu.uapi.server.http.HttpMethod

fun RoutingInit.get(consumes: String? = null, produces: String? = null, handler: TestHttpHandler) {
    route(HttpMethod.Routable.GET, consumes, produces, handler)
}

fun RoutingInit.post(consumes: String? = null, produces: String? = null, handler: TestHttpHandler) {
    route(HttpMethod.Routable.POST, consumes, produces, handler)
}

fun RoutingInit.put(consumes: String? = null, produces: String? = null, handler: TestHttpHandler) {
    route(HttpMethod.Routable.PUT, consumes, produces, handler)
}

fun RoutingInit.patch(consumes: String? = null, produces: String? = null, handler: TestHttpHandler) {
    route(HttpMethod.Routable.PATCH, consumes, produces, handler)
}

fun RoutingInit.delete(consumes: String? = null, produces: String? = null, handler: TestHttpHandler) {
    route(HttpMethod.Routable.DELETE, consumes, produces, handler)
}
