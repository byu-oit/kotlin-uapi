package edu.byu.uapi.server.http.integrationtest

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.core.Response
import org.junit.jupiter.api.fail
import org.skyscreamer.jsonassert.JSONAssert
import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun Response.assertStatus(expected: Int) {
    assertEquals(expected, this.statusCode, "Expected response status to be $expected, was $statusCode")
}

fun Response.assertEmptyBody() {
    val data = this.data
    assertEquals(0, data.size, "Expected an empty body, got a body with length of ${data.size}")
}

fun Response.assertHasHeader(name: String): String {
    assertTrue(name in headers, "Expected header '$name' to be set")
    return headers.getValue(name).first()
}

fun Response.assertHasHeaderValue(name: String, value: String) {
    val actual = assertHasHeader(name)
    assertEquals(value, actual, "Header '$name':")
}

private fun Response.assertJsonBody(expectedJson: String, strict: Boolean) {
    assertHasHeaderValue("Content-Type", "application/json")
    val body = this.body().asString("application/json")
    JSONAssert.assertEquals(expectedJson, body, strict)
}

fun Response.assertJsonBodyLike(expectedJson: String) = assertJsonBody(expectedJson, false)
fun Response.assertJsonBodyEquals(expectedJson: String) = assertJsonBody(expectedJson, true)

inline fun <reified T : Any> Response.assertJsonParseableAs(expectedType: String = "application/json"): T {
    assertHasHeaderValue("Content-Type", expectedType)
    try {
        return this.body().toStream().use { jackson.readValue(it) }
    } catch (ex: Exception) {
        fail("Expected to be able to parse JSON response as ${T::class.simpleName}", ex)
    }
}

