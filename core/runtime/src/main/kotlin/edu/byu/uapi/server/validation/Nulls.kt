package edu.byu.uapi.server.validation

import edu.byu.uapi.spi.validation.Validating
import kotlin.reflect.KProperty0

fun Validating.expectNull(field: String, obj: Any?): Boolean {
    return expect(field, "be null") {
        obj == null
    }
}

fun Validating.expectNull(field: KProperty0<Any?>): Boolean {
    return this.expectNull(field.name, field.get())
}

fun Validating.expectNotNull(field: String, obj: Any?): Boolean {
    return expect(field, "not be null") { obj != null }
}

fun Validating.expectNotNull(field: KProperty0<Any?>): Boolean {
    return this.expectNotNull(field.name, field.get())
}
