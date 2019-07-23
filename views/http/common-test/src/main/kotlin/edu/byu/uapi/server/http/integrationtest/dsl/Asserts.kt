package edu.byu.uapi.server.http.integrationtest.dsl

import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.Response
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.fail
import org.skyscreamer.jsonassert.JSONAssert
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

fun Response.expectStatus(expected: Int) {
    assertEquals(expected, this.code, "Expected response status to be $expected, was $code")
}

fun Response.expectBodyOfType(expectedType: String): String {
    expectHeaderWithValue("Content-Type", expectedType)
    return assertNotNull(this.body?.string())
}

fun Response.expectBodyOfTypeEquals(expectedType: String, expectedBody: String) {
    val body = expectBodyOfType(expectedType)
    assertEquals(body, expectedBody)
}

fun Response.expectEmptyBody() {
    val bytes = this.body?.bytes() ?: ByteArray(0)
    assertEquals(0, bytes.size, "Expected an empty body, got a body with length of ${bytes.size}")
}

fun Response.expectTextBody(): String {
    expectHeaderWithValue("Content-Type", "text/plain")
    return assertNotNull(this.body?.string())
}

fun Response.expectTextBodyEquals(expectedBody: String) {
    assertEquals(expectedBody, expectTextBody())
}

fun Response.expectHeader(name: String): String {
    assertTrue(name in headers.names(), "Expected header '$name' to be set")
    return assertNotNull(headers[name])
}

fun Response.expectHeaderWithValue(name: String, value: String) {
    val actual = expectHeader(name)
    assertEquals(value, actual, "Header '$name':")
}

private fun Response.expectJsonBody(expectedJson: String, strict: Boolean) {
    expectHeaderWithValue("Content-Type", "application/json")
    val body = this.body?.string()
    JSONAssert.assertEquals(expectedJson, body, strict)
}

fun Response.expectJsonBodyLike(
    @Language("JSON") expectedJson: String
) = expectJsonBody(expectedJson, false)

fun Response.expectJsonBodyEquals(
    @Language("JSON") expectedJson: String
) = expectJsonBody(expectedJson, true)

inline fun <reified T : Any> Response.expectJsonParseableAs(expectedType: String = "application/json"): T {
    expectHeaderWithValue("Content-Type", expectedType)
    try {
        return assertNotNull(this.body?.byteStream()?.use { jackson.readValue<T>(it) })
    } catch (ex: Exception) {
        fail("Expected to be able to parse JSON response as ${T::class.simpleName}", ex)
    }
}

