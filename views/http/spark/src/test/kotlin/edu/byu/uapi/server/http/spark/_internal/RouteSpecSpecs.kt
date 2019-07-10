package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.HttpMethod
import edu.byu.uapi.server.http.HttpRoute
import edu.byu.uapi.server.http.path.PathPart
import edu.byu.uapi.server.http.path.staticPart
import edu.byu.uapi.server.http.path.variablePart
import edu.byu.uapi.server.http.test.fixtures.NoopHttpHandler
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals

internal class RouteSpecSpecs {
    @Test
    fun `constructor handles basic mapping`() {
        val route = HttpRoute(
            pathParts = listOf(staticPart("foo")),
            method = HttpMethod.PATCH,
            handler = NoopHttpHandler,
            consumes = "foo/bar",
            produces = "bar/baz"
        )

        val result = RouteSpec(route)

        assertAll(
            { assertEquals("/foo", result.path) },
            { assertEquals(HttpMethod.PATCH, result.method) },
            { assertEquals("bar/baz", result.acceptType) }
        )
    }

    @Test
    fun `constructor flattens path parts properly`() {
        val parts: List<PathPart> = listOf(
            staticPart("foo"),
            variablePart("bar"),
            variablePart("baz", "rab", "oof")
        )

        val result = RouteSpec(
            HttpRoute(
                pathParts = parts,
                method = HttpMethod.GET,
                handler = NoopHttpHandler
            )
        )

        assertEquals(
            "/foo/:bar/:compound__baz__rab__oof",
            result.path
        )
    }
}
