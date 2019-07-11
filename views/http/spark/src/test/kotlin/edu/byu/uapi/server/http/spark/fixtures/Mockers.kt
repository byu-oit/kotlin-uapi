package edu.byu.uapi.server.http.spark.fixtures

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import spark.Request
import spark.Response
import spark.routematch.RouteMatch
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.isAccessible
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

fun mockRequest(
    match: RouteMatch = RouteMatch(Any(), "/foo", "/foo", null),
    block: MockHttpServletRequest.() -> Unit
): Request {
    val req = MockHttpServletRequest()
    req.pathInfo = match.requestURI
    req.block()
    return requestConstructor.call(match, req)
}

private val requestConstructor: KFunction<Request> by lazy {
    val found = Request::class.constructors.find {
        it.parameters.size == 2 &&
            it.parameters.first().type.isSupertypeOf(RouteMatch::class.starProjectedType) &&
            it.parameters.last().type.isSupertypeOf(HttpServletRequest::class.starProjectedType)
    }
    assertNotNull(found, "Expected to find constructor spark.Request(RouteMatch, HttpServletRequest)")
    found.isAccessible = true
    found!!
}

private val responseField: KMutableProperty1<Response, HttpServletResponse> by lazy {
    val found = Response::class.declaredMemberProperties
        .find {
            it.returnType.isSubtypeOf(HttpServletResponse::class.starProjectedType)
        }

    assertNotNull(found, "Expected to find field spark.Response.response: HttpServletResponse")
    found.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    found as KMutableProperty1<Response, HttpServletResponse>
}

class MockResponse: Response() {
    val servletResponse = MockHttpServletResponse()
    init {
        responseField.set(this, servletResponse)
    }
}

internal class MockersFixtureTests {
    @Test
    fun makeSureResponseWorks() {
        val resp = MockResponse()
        resp.status(321)

        assertEquals(321, resp.servletResponse.status)
    }
}
