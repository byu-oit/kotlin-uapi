package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.HttpHandler
import edu.byu.uapi.server.http.spark.fixtures.MockResponse
import edu.byu.uapi.server.http.spark.fixtures.mockRequest
import edu.byu.uapi.server.http.test.fixtures.MockHttpHandler
import edu.byu.uapi.server.http.test.fixtures.fakeResponse
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal abstract class BaseSparkRouteAdapterTest<U: BaseSparkRouteAdapter> {

    abstract fun buildAdapterWithSingleHandler(handler: HttpHandler, context: CoroutineContext): U

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
            handler,
            coroutineContext
        )

        val respBody = unit.handle(req, resp)
        assertNull(respBody)
    }

}
