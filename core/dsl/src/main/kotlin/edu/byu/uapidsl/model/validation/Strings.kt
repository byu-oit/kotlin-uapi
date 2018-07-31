package edu.byu.uapidsl.model.validation

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
