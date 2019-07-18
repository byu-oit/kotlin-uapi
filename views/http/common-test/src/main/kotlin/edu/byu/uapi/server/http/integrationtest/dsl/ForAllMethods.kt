package edu.byu.uapi.server.http.integrationtest.dsl

import edu.byu.uapi.server.http.HttpMethod

fun TestGroupInit.forAllMethodsIt(
    name: String,
    methods: Iterable<HttpMethod.Routable> = HttpMethod.Routable.values(),
    block: TestInit.(method: HttpMethod.Routable) -> Unit
) {
    this.describe(name) {
        methods.forEach { testMethod ->
            it(testMethod.name) {
                block(testMethod)
            }
        }
    }
}
