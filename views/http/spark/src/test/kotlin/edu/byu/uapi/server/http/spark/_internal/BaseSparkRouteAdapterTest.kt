package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.HttpHandler
import edu.byu.uapi.server.http.path.RoutePath
import edu.byu.uapi.server.http.path.format
import edu.byu.uapi.server.http.path.staticPart
import edu.byu.uapi.server.http.path.variablePart
import edu.byu.uapi.server.http.spark.fixtures.MockResponse
import edu.byu.uapi.server.http.spark.fixtures.mockRequest
import edu.byu.uapi.server.http.test.fixtures.MockHttpHandler
import edu.byu.uapi.server.http.test.fixtures.fakeResponse
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import spark.routematch.RouteMatch
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal abstract class BaseSparkRouteAdapterTest<U : BaseSparkRouteAdapter> {

    abstract fun buildAdapterWithSingleHandler(
        routePath: RoutePath,
        handler: HttpHandler,
        context: CoroutineContext
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
        val handler = MockHttpHandler(
            fakeResponse {
                status = 200
                body("foobar", "foo/bar")
            }
        )

        val unit = buildAdapterWithSingleHandler(
            fooPath,
            handler,
            coroutineContext
        )

        val respBody = unit.handle(req, resp)

        assertEquals(1, handler.calls.size)

        assertTrue(respBody is ByteArray)

        assertEquals("foobar", respBody.toString(Charsets.UTF_8))

        assertEquals(200, resp.status())

        val rawResp = resp.servletResponse
        assertEquals("foo/bar", rawResp.contentType)
    }

    @Test
    fun `handle() returns null if the handler does`() = runBlockingTest {
        val req = mockRequest {
            method = "POST"
            contentType = "foo/bar"
            setContent("foobar".toByteArray())
        }
        val resp = MockResponse()
        val handler = MockHttpHandler(
            fakeResponse {
                status = 200
                noBody()
            }
        )

        val unit = buildAdapterWithSingleHandler(
            fooPath,
            handler,
            coroutineContext
        )

        val respBody = unit.handle(req, resp)
        assertNull(respBody)
    }

    @Test
    fun `handle() parses path parameters properly`() = runBlockingTest {
        val singleParam = variablePart("foo")
        val compoundParam = variablePart("bar", "baz")

        val path = listOf(staticPart("before"), singleParam, staticPart("middle"), compoundParam, staticPart("end"))

        val matchUri = sparkPaths.format(path)
        val realUri = "/before/oof/middle/rab,zab/end"

        val req = mockRequest(
            match = RouteMatch(Any(), matchUri, realUri, null)
        ) {
            method = "POST"
            contentType = "foo/bar"
            setContent("foobar".toByteArray())
        }
        val resp = MockResponse()
        val handler = MockHttpHandler(
            fakeResponse { status = 200; noBody() }
        )

        val unit = buildAdapterWithSingleHandler(path, handler, coroutineContext)
        unit.handle(req, resp)

        val call = assertNotNull(handler.calls.firstOrNull())
        assertEquals(realUri, call.path)
        assertEquals(
            mapOf("foo" to "oof", "bar" to "rab", "baz" to "zab"),
            call.pathParams
        )
    }

}
