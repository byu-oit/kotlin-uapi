package edu.byu.uapi.server.http.integrationtest

import edu.byu.uapi.server.http.HttpMethod
import edu.byu.uapi.server.http.integrationtest.dsl.ComplianceSpecSuite
import edu.byu.uapi.server.http.integrationtest.dsl.SuiteDsl
import edu.byu.uapi.server.http.integrationtest.dsl.TestHttpHandler
import edu.byu.uapi.server.http.integrationtest.dsl.TestResponse
import edu.byu.uapi.server.http.integrationtest.dsl.expectBodyOfTypeEquals
import edu.byu.uapi.server.http.integrationtest.dsl.expectTextBodyEquals
import edu.byu.uapi.server.http.integrationtest.dsl.forAllMethodsIt
import edu.byu.uapi.server.http.integrationtest.dsl.patch
import edu.byu.uapi.server.http.integrationtest.dsl.post
import edu.byu.uapi.server.http.integrationtest.dsl.put
import edu.byu.uapi.server.http.integrationtest.dsl.request
import edu.byu.uapi.server.http.integrationtest.dsl.type
import org.apache.commons.codec.digest.DigestUtils
import java.util.Base64

/**
 * The HTTP implementations should provide the handler with a representation of a body. The primary
 * way for the handlers to interact with the body is using HttpRequestBody#consumeBody
 */
object RequestBodySpecs : ComplianceSpecSuite() {
    override fun SuiteDsl.define() {
        describe("empty bodies") {
            givenRoutes {
                post {
                    //consumeBody doesn't invoke the callback if there is no request body
                    var invoked = false
                    val body = this.consumeBody { _, _ ->
                        invoked = true
                        "failure!"
                    }
                    TestResponse.Text("$invoked - $body")
                }
            }
            it("returns null from `consumeBody`") {
                whenCalledWith { post("") }
                then {
                    expectTextBodyEquals("false - null")
                }
            }
        }
        describe("real bodies") {
            forAllMethodsIt("passes body in `consumeBody`",
                methods = HttpMethod.Routable.values().filter { it.allowsBodyInUAPI }) { method ->
                val handler: TestHttpHandler = {
                    this.consumeBody { contentType, stream ->
                        TestResponse.Text("${this.method.name} $contentType - ${stream.reader().readText()}")
                    } ?: TestResponse.Empty()
                }
                givenRoutes {
                    post(handler = handler)
                    put(handler = handler)
                    patch(handler = handler)
                }
                whenCalledWith { request(method, "").type("foo/bar").body("foobar") }
                then {
                    expectTextBodyEquals("${method.name} foo/bar - foobar")
                }
            }
            describe("handles binary data") {
                givenRoutes {
                    post(echoActualBody = false) {
                        //Echoes the hash of the body
                        this.consumeBody { contentType, stream ->
                            TestResponse.Body(stream.readBytes().hash(), contentType)
                        } ?: TestResponse.Empty()
                    }
                }
                it("Handles the binary input properly") {
                    whenCalledWith { post("").type("some/binary").body(binaryData) }
                    then {
                        expectBodyOfTypeEquals("some/binary", binaryData.hash())
                    }
                }
            }
            describe("exceptional cases") {
                it("throws if consumeBody is called more than once") {
                }
            }
        }
    }
}

private val binaryData by lazy {
    Base64.getMimeDecoder().decode(binaryB64)
}

private fun ByteArray.hash(): String {
    return DigestUtils.sha256Hex(this)
}

private val binaryB64 = """
/9j/4AAQSkZJRgABAQAAAQABAAD//gA7Q1JFQVRPUjogZ2QtanBlZyB2MS4wICh1c2luZyBJSkcg
SlBFRyB2ODApLCBxdWFsaXR5ID0gNjUK/9sAQwALCAgKCAcLCgkKDQwLDREcEhEPDxEiGRoUHCkk
KyooJCcnLTJANy0wPTAnJzhMOT1DRUhJSCs2T1VORlRAR0hF/9sAQwEMDQ0RDxEhEhIhRS4nLkVF
RUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVFRUVF/8AAEQgAzgGQ
AwEiAAIRAQMRAf/EAB8AAAEFAQEBAQEBAAAAAAAAAAABAgMEBQYHCAkKC//EALUQAAIBAwMCBAMF
BQQEAAABfQECAwAEEQUSITFBBhNRYQcicRQygZGhCCNCscEVUtHwJDNicoIJChYXGBkaJSYnKCkq
NDU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6g4SFhoeIiYqSk5SVlpeYmZqi
o6Slpqeoqaqys7S1tre4ubrCw8TFxsfIycrS09TV1tfY2drh4uPk5ebn6Onq8fLz9PX29/j5+v/E
AB8BAAMBAQEBAQEBAQEAAAAAAAABAgMEBQYHCAkKC//EALURAAIBAgQEAwQHBQQEAAECdwABAgMR
BAUhMQYSQVEHYXETIjKBCBRCkaGxwQkjM1LwFWJy0QoWJDThJfEXGBkaJicoKSo1Njc4OTpDREVG
R0hJSlNUVVZXWFlaY2RlZmdoaWpzdHV2d3h5eoKDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKz
tLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uLj5OXm5+jp6vLz9PX29/j5+v/aAAwDAQACEQMRAD8A
9SYfN+ApFHzr9ac2c/gKav31+tJbAMQfLSt/qm+ooH3aD/q2/D+dHQOowjilGDs+h/nSOcfnTh/B
9DQCF/5aL9aZEPk/E/zp6/eX60yPof8AeP8AOgOg2cf6O491/nSleDSzj9w/4fzpW6GgOhHMv+lE
jvGtLGMGQf7Bom/4+B7xiliyWb/cNC2H1K0Y+RB7CrDf6+E/7DfzFRxr8iH2FSsP30P+638xR0Am
ZgttKT2XNcTrM+3SrFk5B3sPfAGP512cvFrP2Gz/ABrjbgxmw08SDc8SFth9wuP5Vz15cquzakrs
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
""".trim()
