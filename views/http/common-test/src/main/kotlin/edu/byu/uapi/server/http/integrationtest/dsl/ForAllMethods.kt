package edu.byu.uapi.server.http.integrationtest.dsl

import edu.byu.uapi.server.http.engines.RouteMethod

fun TestGroupInit.forAllMethodsIt(
    name: String,
    methods: Iterable<RouteMethod> = RouteMethod.values().toList(),
    block: TestInit.(method: RouteMethod) -> Unit
) {
    this.describe(name) {
        methods.forEach { testMethod ->
            it(testMethod.name) {
                block(testMethod)
            }
        }
    }
}
