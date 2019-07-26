package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.engines.HttpRoute
import edu.byu.uapi.server.http.engines.RouteMethod
import edu.byu.uapi.server.http.test.fixtures.FakeHttpRouteSource
import edu.byu.uapi.server.http.test.fixtures.MockHttpRoute
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import spark.Request
import spark.Route
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ApplyRoutesKtTest {

    @Nested
    @DisplayName("RouteApplier#applyRoutes()")
    inner class ApplyRoutes {
        @Test
        fun `maps routes to proper methods`() {
            val routes = FakeHttpRouteSource<Request>(
                MockHttpRoute(method = RouteMethod.GET, pathSpec = "get"),
                MockHttpRoute(method = RouteMethod.POST, pathSpec = "post"),
                MockHttpRoute(method = RouteMethod.PUT, pathSpec = "put"),
                MockHttpRoute(method = RouteMethod.PATCH, pathSpec = "patch"),
                MockHttpRoute(method = RouteMethod.DELETE, pathSpec = "delete")
            )

            val applier = FakeRouteApplier()

            applier.applyRoutes(routes)

            assertAll(
                listOf(
                    "get" to applier.gets,
                    "post" to applier.posts,
                    "put" to applier.puts,
                    "patch" to applier.patches,
                    "delete" to applier.deletes
                ).map { (method, calls) ->
                    {
                        val call = calls.assertHasSingle("Expected single call to '$method'")
                        assertAll("call to '$method'",
                            { assertEquals(method, call.first) },
                            { assertNull(call.second) },
                            {
                                val adapter = call.third
                                assertTrue(
                                    adapter is BaseSparkRouteAdapter,
                                    "Expected route to be BaseSparkRouteAdapter"
                                )
                            }
                        )
                    }
                }
            )
        }

        @Test
        fun `groups multiple consumes types into the same route`() {
            val path = "/foo"
            val routes: List<HttpRoute<Request>> = listOf(
                MockHttpRoute(method = RouteMethod.POST, pathSpec = path, consumes = "foo/bar"),
                MockHttpRoute(method = RouteMethod.POST, pathSpec = path, consumes = "foo/bar")
            )

            val applier = FakeRouteApplier()

            applier.applyRoutes(FakeHttpRouteSource(routes))

            val (actualPath, accepts, route) = applier.posts.assertHasSingle()

            assertEquals("/foo", actualPath)
            assertNull(accepts)
            assertTrue(route is HasBodyRouteAdapter)
        }
    }

    fun assertHasSize(expectedSize: Int, actual: Collection<*>, message: String? = null) {
        assertEquals(expectedSize, actual.size, message ?: "Expected collection to have size of $expectedSize")
    }

    fun <T> Collection<T>.assertHasSingle(message: String? = null): T {
        assertHasSize(1, this, message)
        return this.single()
    }

    class FakeRouteApplier : RouteApplier {
        val gets = mutableListOf<Triple<String, String?, Route>>()
        val puts = mutableListOf<Triple<String, String?, Route>>()
        val posts = mutableListOf<Triple<String, String?, Route>>()
        val patches = mutableListOf<Triple<String, String?, Route>>()
        val deletes = mutableListOf<Triple<String, String?, Route>>()

        override fun get(path: String, accepts: String?, route: Route) {
            gets += Triple(path, accepts, route)
        }

        override fun put(path: String, accepts: String?, route: Route) {
            puts += Triple(path, accepts, route)
        }

        override fun patch(path: String, accepts: String?, route: Route) {
            patches += Triple(path, accepts, route)
        }

        override fun post(path: String, accepts: String?, route: Route) {
            posts += Triple(path, accepts, route)
        }

        override fun delete(path: String, accepts: String?, route: Route) {
            deletes += Triple(path, accepts, route)
        }
    }
}
