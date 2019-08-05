package edu.byu.uapi.server.http.test

import edu.byu.uapi.server.http.engines.RequestReader
import edu.byu.uapi.server.http.path.PathFormatter
import edu.byu.uapi.server.http.path.RoutePath
import edu.byu.uapi.server.http.path.extractVariableValues
import edu.byu.uapi.server.http.path.staticPart
import edu.byu.uapi.server.http.path.variablePart
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("FunctionName")
interface RequestReaderContractTests<EngineRequest> {

    val reader: RequestReader<EngineRequest>
    val pathFormatter: PathFormatter

    @Suppress("LongParameterList")
    fun buildRequest(
        method: String = "GET",
        requestPath: String = "/foo",
        pathSpec: RoutePath = listOf(staticPart("foo")),
        headers: Map<String, String> = emptyMap(),
        queryParameters: Map<String, List<String>> = emptyMap(),
        body: String? = null
    ): EngineRequest

    @Test
    fun `path() gets the correct path`() {
        val req = buildRequest(requestPath = "/correct/pathSpec")
        val result = reader.path(req)

        assertEquals("/correct/pathSpec", result)
    }

    @Test
    fun `headerNames() returns the header names in the same case they were given`() {
        val req = buildRequest(headers = mapOf("FOO" to "oof", "bar" to "rab", "BaZ" to "zab"))
        val result = reader.headerNames(req)

        assertEquals(
            setOf("FOO", "bar", "BaZ"),
            result
        )
    }

    @ParameterizedTest
    @CsvSource(
        "FOO, oof",
        "bar, rab",
        "BaZ, zab"
    )
    fun `headerValue() returns proper values for headers`(
        header: String,
        value: String
    ) {
        val req = buildRequest(headers = mapOf(header to value))
        val result = reader.headerValue(req, header)

        assertEquals(value, result)
    }

    @Test
    fun `queryParameters() returns all query param values`() {
        val req = buildRequest(
            queryParameters = mapOf("foo" to listOf("bar"), "baz" to listOf("zab"))
        )
        val result = reader.queryParameters(req)

        assertEquals(
            mapOf("foo" to listOf("bar"), "baz" to listOf("zab")),
            result
        )
    }

    @Test
    fun `queryParameters() can handle multiple query param values`() {
        val req = buildRequest(
            queryParameters = mapOf("foo" to listOf("bar", "baz"))
        )
        val result = reader.queryParameters(req)

        assertEquals(
            listOf("bar", "baz"),
            result["foo"]
        )
    }

    @Test
    fun `queryParameters() is an empty if there are no query params`() {
        val req = buildRequest(
            queryParameters = emptyMap()
        )
        val result = reader.queryParameters(req)

        assertEquals(
            emptyMap(),
            result
        )
    }

    @Test
    fun `pathParameters() returns all pathSpec param values`() {
        val path = listOf(
            staticPart("prefix"),
            variablePart("foo"),
            variablePart("bar", "baz", "qux"),
            staticPart("suffix")
        )
        val req = buildRequest(
            pathSpec = path,
            requestPath = "/prefix/oof/rab,zab,xuq/suffix"
        )

        val result = reader.pathParameters(req)

        // It's pretty hard to figure out how the params will be formatted,
        //  so we pass the result through the formatter before comparing.
        val extracted = pathFormatter.extractVariableValues(path, result)

        assertEquals(
            mapOf("foo" to "oof", "bar" to "rab", "baz" to "zab", "qux" to "xuq"),
            extracted
        )
    }

    @Test
    fun `pathParameters() is empty if the path has no variable parts`() {
        val list = listOf(staticPart("path"), staticPart("parts"))
        val req = buildRequest(
            pathSpec = list,
            requestPath = "/path/parts"
        )

        assertEquals(
            emptyMap(),
            reader.pathParameters(req)
        )
    }

    /**
     * Some servers, like Ktor, combine query and pathSpec params.  We don't want to do that.
     */
    @Test
    fun `pathParameters() does not include any query params`() {
        val path = listOf(
            variablePart("foo")
        )
        val req = buildRequest(
            queryParameters = mapOf("bar" to listOf("baz")),
            pathSpec = path,
            requestPath = "/bar"
        )

        val params = pathFormatter.extractVariableValues(
            path,
            reader.pathParameters(req)
        )

        assertFalse("bar" in params)
    }

    @Test
    fun `bodyStream() returns the request body`() = runBlockingTest {
        val req = buildRequest(
            body = "foobar"
        )

        val stream = reader.bodyStream(req)

        val actual = stream.use { it.reader().readText() }

        assertEquals("foobar", actual)
    }

    @Test
    fun `bodyStream() returns an empty stream if there is no body`() = runBlockingTest {
        val req = buildRequest(
            body = null
        )

        val stream = reader.bodyStream(req)

        val bytes = stream.use { it.readBytes() }
        assertTrue(bytes.isEmpty(), "Expected body to be empty")
    }
}
