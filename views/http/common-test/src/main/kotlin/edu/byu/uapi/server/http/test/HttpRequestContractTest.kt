package edu.byu.uapi.server.http.test

import edu.byu.uapi.server.http.HttpRequest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse


@Suppress("FunctionName")
interface HttpRequestContractTest {
    data class ContractTestParameters(
        val headers: Map<String, String> = emptyMap(),
        val queryParameters: Map<String, List<String>> = emptyMap(),
        val pathParameters: Map<String, String> = emptyMap(),
        val body: String? = null
    )

    fun buildRequest(
        headers: Map<String, String> = emptyMap(),
        queryParameters: Map<String, List<String>> = emptyMap(),
        pathParameters: Map<String, String> = emptyMap(),
        body: String? = null
    ): HttpRequest


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

}
