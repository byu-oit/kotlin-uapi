package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.HttpRequest
import edu.byu.uapi.server.http.path.PathPart
import edu.byu.uapi.server.http.path.variablePart
import edu.byu.uapi.server.http.spark.fixtures.mockRequest
import edu.byu.uapi.server.http.test.HttpRequestContractTest
import spark.routematch.RouteMatch

internal class SparkRequestAdapterTest : HttpRequestContractTest {
    override fun buildRequest(
        method: String,
        headers: Map<String, String>,
        queryParameters: Map<String, List<String>>,
        pathParameters: Map<String, String>,
        body: String?
    ): HttpRequest {
        val matchUri = StringBuilder("/")
        val requestUri = StringBuilder("/")
        val routePath = mutableListOf<PathPart>()
        if (pathParameters.isNotEmpty()) {
            pathParameters.forEach { (k, v) ->
                matchUri.append(":$k/")
                requestUri.append("$v/")
                routePath += variablePart(k)
            }
        }
        val match = RouteMatch(Any(), matchUri.toString(), requestUri.toString(), "*/*")

        val req = mockRequest(match) {
            this.method = method
            headers.forEach { (k, v) -> addHeader(k, v) }
            queryParameters.forEach { (k, v) -> addParameter(k, *v.toTypedArray()) }
            if (body != null) {
                this.setContent(body.toByteArray())
            }
        }

        return SparkRequestAdapter(req, routePath)
    }
}
