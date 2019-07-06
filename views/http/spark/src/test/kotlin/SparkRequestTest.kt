package edu.byu.uapi.server.http.spark

import edu.byu.uapi.server.http.HttpRequest
import edu.byu.uapi.server.http.test.HttpRequestContractTest
import org.junit.platform.commons.util.ReflectionUtils
import org.springframework.mock.web.MockHttpServletRequest
import spark.Request
import spark.routematch.RouteMatch
import javax.servlet.http.HttpServletRequest
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.isAccessible
import kotlin.test.fail

internal class SparkRequestTest : HttpRequestContractTest {
    override fun buildRequest(
        headers: Map<String, String>,
        queryParameters: Map<String, List<String>>,
        pathParameters: Map<String, String>,
        body: String?
    ): HttpRequest {
        val matchUri = StringBuilder("/")
        val requestUri = StringBuilder("/")
        if (pathParameters.isNotEmpty()) {
            pathParameters.forEach { (k, v) ->
                matchUri.append(":$k/")
                requestUri.append("$v/")
            }
        }
        val match = RouteMatch(Any(), matchUri.toString(), requestUri.toString(), "*/*")

        val req = MockHttpServletRequest().apply {
            headers.forEach { (k, v) -> addHeader(k, v) }
            queryParameters.forEach { (k, v) -> addParameter(k, *v.toTypedArray()) }
        }

        val ctor = Request::class.constructors.find {
            it.parameters.size == 2 &&
                it.parameters.first().type.isSupertypeOf(RouteMatch::class.starProjectedType) &&
                it.parameters.last().type.isSupertypeOf(HttpServletRequest::class.starProjectedType)
        } ?: throw IllegalStateException("Unable to find constructor spark.Request(RouteMatch, HttpServletRequest)")

        ctor.isAccessible = true

        val sparkRequest = ctor.call(match, req)
        return SparkRequest(sparkRequest)
    }
}
