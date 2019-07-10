package edu.byu.uapi.server.http.spark._internal

import edu.byu.uapi.server.http.HttpMethod
import edu.byu.uapi.server.http.HttpRoute
import edu.byu.uapi.server.http.path.staticPart
import edu.byu.uapi.server.http.test.fixtures.FakeHttpRouteSource
import edu.byu.uapi.server.http.test.fixtures.NoopHttpHandler
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
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
            val routes = FakeHttpRouteSource(
                HttpMethod.Routable.values().map { method ->
                    HttpRoute(
                        listOf(staticPart(method.name.toLowerCase())),
                        method,
                        NoopHttpHandler
                    )
                }
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
                            { assertEquals("/$method", call.first) },
                            { assertNull(call.second) },
                            {
                                val adapter = call.third
                                assertTrue(adapter is SimpleRouteAdapter, "Expected route to be SparkRouteAdapter")
                                assertEquals(
                                    NoopHttpHandler,
                                    adapter.handler
                                )
                            }
                        )
                    }
                }
            )
        }

        @Test
        fun `groups multiple consumes types into the same route`() {
            val path = listOf(staticPart("foo"))
            val routes = listOf(
                HttpRoute(path, HttpMethod.GET, NoopHttpHandler, consumes = "foo/bar"),
                HttpRoute(path, HttpMethod.GET, NoopHttpHandler, consumes = "bar/baz")
            )

            val applier = FakeRouteApplier()

            applier.applyRoutes(FakeHttpRouteSource(routes))

            val (actualPath, accepts, route) = applier.gets.assertHasSingle()

            assertEquals("/foo", actualPath)
            assertNull(accepts)
            assertTrue(route is ConsumesMultipleTypesRouteAdapter)

            assertEquals(2, route.handlers.size)
        }
    }

    fun assertHasSize(expectedSize: Int, actual: Collection<*>, message: String? = null) {
        assertEquals(expectedSize, actual.size, message ?: "Expected collection to have size of $expectedSize")
    }

    fun <T> Collection<T>.assertHasSingleMatching(message: String? = null, asserts: T.() -> Unit) {
        assertHasSize(1, this, message)
        this.single().asserts()
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

