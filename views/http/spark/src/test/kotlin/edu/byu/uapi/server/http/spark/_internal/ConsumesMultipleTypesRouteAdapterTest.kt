package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.HttpHandler
import edu.byu.uapi.server.http.errors.UAPIHttpMissingHeaderError
import edu.byu.uapi.server.http.errors.UAPIHttpUnrecognizedContentTypeError
import edu.byu.uapi.server.http.spark.fixtures.MockResponse
import edu.byu.uapi.server.http.spark.fixtures.mockRequest
import edu.byu.uapi.server.http.test.fixtures.MockHttpHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class ConsumesMultipleTypesRouteAdapterTest
    : BaseSparkRouteAdapterTest<ConsumesMultipleTypesRouteAdapter>() {

    override fun buildAdapterWithSingleHandler(
        handler: HttpHandler,
        context: CoroutineContext
    ): ConsumesMultipleTypesRouteAdapter {
        return ConsumesMultipleTypesRouteAdapter(
            mapOf("*/*" to handler),
            context
        )
    }

    @TestFactory
    fun `handle() uses the best-matching mime type handler`(): Stream<DynamicTest> {
        val wildcardHandler = MockHttpHandler()
        val fooStarHandler = MockHttpHandler()
        val fooBarHandler = MockHttpHandler()

        val types: List<Triple<String, String, MockHttpHandler>> = listOf(
            Triple("Exact Match", "foo/bar", fooBarHandler),
            Triple("Partial Match", "foo/baz", fooStarHandler),
            Triple("No Match", "baz/foo", wildcardHandler)
        )

        fun CoroutineScope.buildAdapter() = ConsumesMultipleTypesRouteAdapter(
            mapOf(
                "*/*" to wildcardHandler,
                "foo/*" to fooStarHandler,
                "foo/bar" to fooBarHandler
            ),
            coroutineContext
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

    @Test
    fun `throws when the content-type header is missing`() = runBlockingTest {
        val adapter = ConsumesMultipleTypesRouteAdapter(
            mapOf("foo/bar" to MockHttpHandler()),
            coroutineContext
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
        val adapter = ConsumesMultipleTypesRouteAdapter(
            mapOf("foo/bar" to MockHttpHandler()),
            coroutineContext
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
