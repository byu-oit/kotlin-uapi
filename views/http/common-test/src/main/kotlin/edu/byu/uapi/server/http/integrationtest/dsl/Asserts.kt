package edu.byu.uapi.server.http.integrationtest.dsl

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.Response
import org.junit.jupiter.api.fail
import org.skyscreamer.jsonassert.JSONAssert
import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun Response.expectStatus(expected: Int) {
    assertEquals(expected, this.statusCode, "Expected response status to be $expected, was $statusCode")
}

fun Response.expectEmptyBody() {
    val data = this.data
    assertEquals(0, data.size, "Expected an empty body, got a body with length of ${data.size}")
}

fun Response.expectTextBody(expectedBody: String) {
    expectHeaderWithValue("Content-Type", "text/plain")
    val actual = this.body().asString("text/plain")
    assertEquals(expectedBody, actual)
}

fun Response.expectHeader(name: String): String {
    assertTrue(name in headers, "Expected header '$name' to be set")
    return headers.getValue(name).first()
}

fun Response.expectHeaderWithValue(name: String, value: String) {
    val actual = expectHeader(name)
    assertEquals(value, actual, "Header '$name':")
}

private fun Response.expectJsonBody(expectedJson: String, strict: Boolean) {
    expectHeaderWithValue("Content-Type", "application/json")
    val body = this.body().asString("application/json")
    JSONAssert.assertEquals(expectedJson, body, strict)
}

fun Response.expectJsonBodyLike(expectedJson: String) = expectJsonBody(expectedJson, false)
fun Response.expectJsonBodyEquals(expectedJson: String) = expectJsonBody(expectedJson, true)

inline fun <reified T : Any> Response.expectJsonParseableAs(expectedType: String = "application/json"): T {
    expectHeaderWithValue("Content-Type", expectedType)
    try {
        return this.body().toStream().use { jackson.readValue(it) }
    } catch (ex: Exception) {
        fail("Expected to be able to parse JSON response as ${T::class.simpleName}", ex)
    }
}

