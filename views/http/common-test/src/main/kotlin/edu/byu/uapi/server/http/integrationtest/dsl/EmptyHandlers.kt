package edu.byu.uapi.server.http.integrationtest.dsl

fun RoutingInit.emptyGet() {
    get {
        TestResponse.Empty()
    }
}

fun RoutingInit.emptyPost(consumes: String? = null) {
    post(consumes) {
        TestResponse.Empty()
    }
}

fun RoutingInit.emptyPut(consumes: String? = null) {
    put(consumes) {
        TestResponse.Empty()
    }
}

fun RoutingInit.emptyPatch(consumes: String? = null) {
    patch(consumes) {
        TestResponse.Empty()
    }
}

fun RoutingInit.emptyDelete() {
    delete {
        TestResponse.Empty()
    }
}
