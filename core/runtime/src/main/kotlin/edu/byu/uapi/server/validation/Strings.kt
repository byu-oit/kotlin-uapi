package edu.byu.uapi.server.validation

import edu.byu.uapi.spi.validation.Validating
import kotlin.reflect.KProperty0


fun Validating.expectNotEmpty(field: String, value: String?): Boolean {
    return expect(field, "not be empty") { value != null && value.isNotEmpty() }
}

fun Validating.expectNotEmpty(field: KProperty0<String?>): Boolean {
    return this.expectNotEmpty(field.name, field.get())
}

fun Validating.expectNotBlank(field: String, value: String?): Boolean {
    return expect(field, "not be blank") { value != null && value.isNotBlank() }
}

fun Validating.expectNotBlank(field: KProperty0<String?>): Boolean {
    return this.expectNotBlank(field.name, field.get())
}

fun Validating.expectMatches(field: String, regex: Regex, description: String, value: String?): Boolean {
    return expect(field, description) {value != null && value.matches(regex)}
}

fun Validating.expectMatches(field: KProperty0<String?>, regex: Regex, description: String): Boolean {
    return expectMatches(field.name, regex, description, field.get())
}
