package edu.byu.uapi.server.validation

import kotlin.reflect.KProperty0


fun Validating.expectPositive(field: String, value: Int): Boolean {
    return expect(field, "be positive") { value > 0 }
}

fun Validating.expectPositiveInt(field: KProperty0<Int>): Boolean {
    return expectPositive(field.name, field.get())
}

fun Validating.expectPositive(field: String, value: Long): Boolean {
    return expect(field, "be positive") { value > 0 }
}

fun Validating.expectPositiveLong(field: KProperty0<Long>): Boolean {
    return expectPositive(field.name, field.get())
}
