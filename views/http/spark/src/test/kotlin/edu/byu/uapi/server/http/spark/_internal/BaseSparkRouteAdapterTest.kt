package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.engines.HttpRoute
import edu.byu.uapi.server.http.errors.HttpErrorMapper
import edu.byu.uapi.server.http.path.format
import edu.byu.uapi.server.http.path.staticPart
import edu.byu.uapi.server.http.path.variablePart
import edu.byu.uapi.server.http.spark.fixtures.MockResponse
import edu.byu.uapi.server.http.spark.fixtures.mockRequest
import edu.byu.uapi.server.http.test.fixtures.MockHttpRoute
import edu.byu.uapi.server.http.test.fixtures.RethrowingErrorMapper
import edu.byu.uapi.server.http.test.fixtures.fakeResponse
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import spark.Request
import spark.routematch.RouteMatch
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal abstract class BaseSparkRouteAdapterTest<U : BaseSparkRouteAdapter> {

    abstract fun buildAdapterWithSingleRoute(
        route: HttpRoute<Request>,
        context: CoroutineContext,
        errorMapper: HttpErrorMapper
    ): U

    private val fooPath = listOf(staticPart("foo"))

    @Test
    fun `handle() invokes the underlying handler and sends its response`() = runBlockingTest {
        val req = mockRequest {
            method = "POST"
            contentType = "foo/bar"
            setContent("foobar".toByteArray())
        }
        val resp = MockResponse()
        val route = MockHttpRoute<Request>(
            fakeResponse {
                status = 200
                body("foobar", "foo/bar")
            }
        )

        val unit = buildAdapterWithSingleRoute(
            route,
            coroutineContext,
            RethrowingErrorMapper
        )

        val respBody = unit.handle(req, resp)

        assertEquals(1, route.calls.size)

        assertTrue(respBody is ByteArray)

        assertEquals("foobar", respBody.toString(Charsets.UTF_8))

        assertEquals(200, resp.status())

        val rawResp = resp.servletResponse
        assertEquals("foo/bar", rawResp.contentType)
    }

    @Test
    fun `handle() returns empty string if the handler returns null`() = runBlockingTest {
        val req = mockRequest {
            method = "POST"
            contentType = "foo/bar"
            setContent("foobar".toByteArray())
        }
        val resp = MockResponse()
        val route = MockHttpRoute<Request>(
            fakeResponse {
                status = 200
                noBody()
            }
        )

        val unit = buildAdapterWithSingleRoute(
            route,
            coroutineContext,
            RethrowingErrorMapper
        )

        val respBody = unit.handle(req, resp)
        assertEquals("", respBody)
    }

    @Test
    fun `handle() parses path parameters properly`() = runBlockingTest {
        val singleParam = variablePart("foo")
        val compoundParam = variablePart("bar", "baz")

        val path = listOf(
            staticPart("before"),
            singleParam,
            staticPart("middle"),
            compoundParam,
            staticPart("end")
        )

        val matchUri = SparkEngine.pathFormatter.format(path)
        val realUri = "/before/oof/middle/rab,zab/end"

        val req = mockRequest(
            match = RouteMatch(Any(), matchUri, realUri, null)
        ) {
            method = "POST"
            contentType = "foo/bar"
            setContent("foobar".toByteArray())
        }
        val resp = MockResponse()
        val route = MockHttpRoute<Request>(
            fakeResponse { status = 200; noBody() }
        )

        val unit = buildAdapterWithSingleRoute(
            route,
            coroutineContext,
            RethrowingErrorMapper
        )
        unit.handle(req, resp)

        assertEquals(1, route.calls.size)
    }

}
