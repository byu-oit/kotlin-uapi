package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.engines.HttpRoute
import edu.byu.uapi.server.http.errors.HttpErrorMapper
import edu.byu.uapi.server.http.errors.UAPIHttpMissingHeaderError
import edu.byu.uapi.server.http.errors.UAPIHttpUnrecognizedContentTypeError
import edu.byu.uapi.server.http.path.staticPart
import edu.byu.uapi.server.http.spark.fixtures.MockResponse
import edu.byu.uapi.server.http.spark.fixtures.mockRequest
import edu.byu.uapi.server.http.test.fixtures.MockHttpRoute
import edu.byu.uapi.server.http.test.fixtures.RethrowingErrorMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import spark.Request
import java.util.stream.Stream
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class HasBodyRouteAdapterTest
    : BaseSparkRouteAdapterTest<HasBodyRouteAdapter>() {
    override fun buildAdapterWithSingleRoute(
        route: HttpRoute<Request>,
        context: CoroutineContext,
        errorMapper: HttpErrorMapper
    ): HasBodyRouteAdapter {
        return HasBodyRouteAdapter(
            listOf(route),
            context,
            errorMapper
        )
    }

    @TestFactory
    fun `handle() uses the best-matching mime type handler`(): Stream<DynamicTest> {
        val wildcardHandler = MockHttpRoute<Request>(consumes = "*/*")
        val fooStarHandler = MockHttpRoute<Request>(consumes = "foo/*")
        val fooBarHandler = MockHttpRoute<Request>(consumes = "foo/bar")

        val types: List<Triple<String, String, MockHttpRoute<Request>>> = listOf(
            Triple("Exact Match", "foo/bar", fooBarHandler),
            Triple("Partial Match", "foo/baz", fooStarHandler),
            Triple("No Match", "baz/foo", wildcardHandler)
        )

        val routes = listOf(
            wildcardHandler,
            fooStarHandler,
            fooBarHandler
        )

        fun CoroutineScope.buildAdapter() = HasBodyRouteAdapter(
            routes,
            coroutineContext,
            RethrowingErrorMapper
        )

        return DynamicTest.stream(
            types.iterator(),
            { it.first }
        ) { (_, type, expectedHandler) ->
            runBlockingTest {
                val adapter = buildAdapter()

                val req = mockRequest {
                    method = "POST"
                    addHeader("Content-Type", type)
                    setContent("foobar".toByteArray())
                }

                val resp = MockResponse()

                adapter.handle(req, resp)

                assertEquals(1, expectedHandler.calls.size, "Expected one call to handler for $type")
            }
        }
    }

    private val fooPath = listOf(staticPart("foo"))

    @Test
    fun `uses the wildcard handler when the content-type header is missing`() = runBlockingTest {
        val adapter = HasBodyRouteAdapter(
            listOf(MockHttpRoute(consumes = "foo/bar")),
            coroutineContext,
            RethrowingErrorMapper
        )

        val req = mockRequest {
            method = "POST"
            setContent("foobar".toByteArray())
        }

        val resp = MockResponse()

        val ex = assertFailsWith<UAPIHttpMissingHeaderError> {
            adapter.handle(req, resp)
        }
        assertTrue("Content-Type" in ex.message)
    }

    @Test
    fun `throws when the there is no matching mime type`() = runBlockingTest {
        val adapter = HasBodyRouteAdapter(
            listOf(MockHttpRoute(consumes = "foo/bar")),
            coroutineContext,
            RethrowingErrorMapper
        )

        val req = mockRequest {
            method = "POST"
            addHeader("Content-Type", "bar/foo")
            setContent("foobar".toByteArray())
        }

        val resp = MockResponse()

        val ex = assertFailsWith<UAPIHttpUnrecognizedContentTypeError> {
            adapter.handle(req, resp)
        }
        assertTrue("foo/bar" in ex.message)
    }

}