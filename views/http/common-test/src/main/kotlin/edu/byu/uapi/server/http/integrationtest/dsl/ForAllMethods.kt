package edu.byu.uapi.server.http.integrationtest.dsl

import edu.byu.uapi.server.http.HttpMethod

fun TestGroupInit.forAllMethodsIt(
    name: String,
    block: TestInit.(method: HttpMethod.Routable) -> Unit
) {
    this.describe(name) {
        HttpMethod.Routable.values().forEach { testMethod ->
            it(testMethod.name) {
                block(testMethod)
            }
        }
    }
}
