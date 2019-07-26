package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.engines.RequestReader
import edu.byu.uapi.server.http.path.PathFormatter
import edu.byu.uapi.server.http.path.RoutePath
import edu.byu.uapi.server.http.path.format
import edu.byu.uapi.server.http.spark.fixtures.mockRequest
import edu.byu.uapi.server.http.test.RequestReaderContractTests
import spark.Request
import spark.routematch.RouteMatch

internal class SparkRequestReaderTest : RequestReaderContractTests<Request> {
    override val pathFormatter: PathFormatter = SparkEngine.pathFormatter
    override val reader: RequestReader<Request> = SparkRequestReader

    override fun buildRequest(
        method: String,
        requestPath: String,
        pathSpec: RoutePath,
        headers: Map<String, String>,
        queryParameters: Map<String, List<String>>,
        body: String?
    ): Request {
        val routePath = SparkEngine.pathFormatter.format(pathSpec)
        val match = RouteMatch(Any(), routePath, requestPath, "*/*")
        return mockRequest(match) {
            this.method = method
            headers.forEach { (k, v) -> addHeader(k, v) }
            queryParameters.forEach { (k, v) -> addParameter(k, *v.toTypedArray()) }
            if (body != null) {
                this.setContent(body.toByteArray())
            }
        }
    }

}
