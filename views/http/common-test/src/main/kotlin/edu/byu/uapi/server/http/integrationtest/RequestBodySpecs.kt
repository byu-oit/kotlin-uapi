@file:Suppress("BlockingMethodInNonBlockingContext")

package edu.byu.uapi.server.http.integrationtest

import edu.byu.uapi.server.http._internal.HttpRequestWithBody
import edu.byu.uapi.server.http._internal.contentType
import edu.byu.uapi.server.http.engines.RouteMethod
import edu.byu.uapi.server.http.integrationtest.dsl.ComplianceSpecSuite
import edu.byu.uapi.server.http.integrationtest.dsl.SuiteDsl
import edu.byu.uapi.server.http.integrationtest.dsl.TestHttpHandler
import edu.byu.uapi.server.http.integrationtest.dsl.TestResponse
import edu.byu.uapi.server.http.integrationtest.dsl.expectBodyOfTypeEquals
import edu.byu.uapi.server.http.integrationtest.dsl.expectTextBodyEquals
import edu.byu.uapi.server.http.integrationtest.dsl.forAllMethodsIt
import edu.byu.uapi.server.http.integrationtest.dsl.hash
import edu.byu.uapi.server.http.integrationtest.dsl.request
import edu.byu.uapi.server.http.integrationtest.dsl.type

/**
 * The HTTP implementations should provide the handler with a representation of a body, and shouldn't run any
 * transformations on that body. They should be able to cope with bodies of all types, including non-standard
 * MIME types.
 */
object RequestBodySpecs : ComplianceSpecSuite() {
    override fun SuiteDsl.define() {
        describe("empty bodies") {
            givenRoutes {
                post {
                    val stream = inputStream
                    val empty = stream.read() == -1
                    TestResponse.Text("$empty")
                }
            }
            it("returns null from `consumeBody`") {
                whenCalledWith { post("") }
                then {
                    expectTextBodyEquals("true")
                }
            }
        }
        describe("real bodies") {
            forAllMethodsIt("passes body in `inputStream`",
                methods = RouteMethod.values().filter { it.mayHaveBody }) { method ->
                fun <R : HttpRequestWithBody> handler(): TestHttpHandler<R> = {
                    this.inputStream.use { stream ->
                        TestResponse.Text("${method.name} ${contentType()} - ${stream.reader().readText()}")
                    }
                }
                givenRoutes {
                    post(handler = handler())
                    put(handler = handler())
                    patch(handler = handler())
                }
                whenCalledWith { request(method, "").type("foo/bar").body("foobar") }
                then {
                    expectTextBodyEquals("${method.name} foo/bar - foobar")
                }
            }
            it("Handles binary input properly") {
                givenRoutes {
                    post {
                        //Echoes the hash of the body
                        val bytes = this.inputStream.use { it.readBytes() }
                        println("handler body: " + bytes.size)
                        val hash = bytes.hash()
                        TestResponse.Body(hash, this.contentType()!!)
                    }
                }
                whenCalledWith { post("").type("some/binary").body(binaryData) }
                then {
                    println(binaryData.size)
                    expectBodyOfTypeEquals("some/binary", binaryData.hash())
                }
            }
        }
    }
}

