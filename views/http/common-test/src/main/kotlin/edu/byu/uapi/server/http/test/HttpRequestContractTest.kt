package edu.byu.uapi.server.http.test

import edu.byu.uapi.server.http.HttpMethod
import edu.byu.uapi.server.http.HttpRequest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.io.IOException
import java.io.InputStream
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


@Suppress("FunctionName")
interface HttpRequestContractTest {

    fun buildRequest(
        method: String = "GET",
        headers: Map<String, String> = emptyMap(),
        queryParameters: Map<String, List<String>> = emptyMap(),
        pathParameters: Map<String, String> = emptyMap(),
        body: String? = null
    ): HttpRequest

    @TestFactory
    fun `val method handles all supported HTTP methods`(): Iterable<DynamicTest> {
        return HttpMethod.values.map { method ->
            DynamicTest.dynamicTest(method.name) {
                val req = buildRequest(method = method.name)
                assertEquals(method, req.method)
            }
        }
    }

    @Test
    fun `val headers contains all header values`() {
        val req = buildRequest(
            headers = mapOf("foo" to "fooval", "bar" to "barval")
        )

        assertEquals(
            mapOf("foo" to "fooval", "bar" to "barval"),
            req.headers
        )
    }

    @Test
    fun `val headers lower-cases all header names`() {
        val req = buildRequest(
            headers = mapOf("FOO" to "", "BaZ" to "", "bar" to "")
        )

        assertEquals(
            setOf("foo", "baz", "bar"),
            req.headers.keys
        )
    }


    @Test
    fun `val queryParams contains all query param values`() {
        val req = buildRequest(
            queryParameters = mapOf("foo" to listOf("bar"), "baz" to listOf("zab"))
        )

        assertEquals(
            mapOf("foo" to listOf("bar"), "baz" to listOf("zab")),
            req.queryParams
        )
    }

    @Test
    fun `val queryParams can handle multiple query param values`() {
        val req = buildRequest(
            queryParameters = mapOf("foo" to listOf("bar", "baz"))
        )

        assertEquals(
            listOf("bar", "baz"),
            req.queryParams["foo"]
        )
    }

    @Test
    fun `val queryParams is an empty if there are no query params`() {
        val req = buildRequest(
            queryParameters = emptyMap()
        )

        assertEquals(
            emptyMap(),
            req.queryParams
        )
    }

    @Test
    fun `val pathParams contains all path param values`() {
        val req = buildRequest(
            pathParameters = mapOf("foo" to "bar", "baz" to "zab")
        )

        assertEquals(
            mapOf("foo" to "bar", "baz" to "zab"),
            req.pathParams
        )
    }

    @Test
    fun `val pathParams is empty if there are no path params`() {
        val req = buildRequest(
            pathParameters = emptyMap()
        )

        assertEquals(
            emptyMap(),
            req.pathParams
        )
    }

    /**
     * Some servers, like Ktor, combine query and path params.  We don't want to do that.
     */
    @Test
    fun `val pathParams does not include any query params`() {
        val req = buildRequest(
            queryParameters = mapOf("bar" to listOf("baz")),
            pathParameters = mapOf("foo" to "bar")
        )

        assertFalse("bar" in req.pathParams)
    }

    @Test
    fun `consumeBody() calls the consumer if the request has a body`() = runBlockingTest {
        val expectedBody = "Hello World"
        val req = buildRequest(
            method = "POST",
            headers = mapOf("Content-Type" to "foo/bar"),
            body = expectedBody
        )

        val result = req.consumeBody(testConsumer())

        assertNotNull(result)
        val (type, body) = result
        assertEquals("foo/bar", type)

        assertEquals(expectedBody, body)
    }

    @Test
    fun `consumeBody() returns null if there is no Content-Type header`() = runBlockingTest {
        val req = buildRequest(
            method = "POST",
            body = "hi",
            headers = emptyMap()
        )

        val body = req.consumeBody(testConsumer())
        assertNull(body)
    }

    private fun testConsumer(): suspend (String, InputStream) -> Pair<String, String> = { type, stream ->
        type to stream.reader(Charsets.UTF_8).readText()
    }

    @TestFactory
    fun `consumeBody() returns null if the request method doesn't support bodies`() = runBlockingTest {
        DynamicTest.stream(
            HttpMethod.values().filterNot { it.allowsBodyInUAPI }.iterator(),
            { it.name }
        ) { method ->
            val req = buildRequest(
                method = method.name,
                body = "hi",
                headers = mapOf("Content-Type" to "foo/bar")
            )

            val body = runBlocking {
                req.consumeBody(testConsumer())
            }
            assertNull(body)
        }
    }

    @Test
    fun `consumeBody() returns null if there is no body`() = runBlockingTest {
        val req = buildRequest(
            method = "POST",
            body = null,
            headers = mapOf("Content-Type" to "foo/bar")
        )

        val body = req.consumeBody(testConsumer())
        assertNull(body)
    }

    @Test
    fun `consumeBody() always closes the input stream`() = runBlockingTest {
        val ex = RuntimeException("foobar")
        val req = buildRequest(
            method = "POST",
            body = "body",
            headers = mapOf("Content-Type" to "foo/bar")
        )
        lateinit var actualStream: InputStream
        val actualEx = assertFailsWith<RuntimeException> {
            req.consumeBody { _, stream ->
                actualStream = stream
                throw ex
            }
        }
        assertEquals(ex, actualEx)
        val ioex = assertFailsWith<IOException> { actualStream.available() }
        val msg = assertNotNull(ioex.message)
        assertTrue("closed" in msg.toLowerCase())
    }

}
