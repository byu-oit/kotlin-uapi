package edu.byu.uapi.server.validation

import java.util.*

interface Validating {

    fun expect(field: String, should: String, condition: () -> Boolean): Boolean

    fun collectFailures(): List<ValidationFailure>

}

class DefaultValidating: Validating {

    private val failures = mutableListOf<ValidationFailure>()

    override fun expect(field: String, should: String, condition: () -> Boolean): Boolean {
        val result = condition()
        if (!result) {
            failures.add(ValidationFailure(field, should))
        }
        return result
    }

    override fun collectFailures(): List<ValidationFailure> {
        return Collections.unmodifiableList(failures)
    }
}

data class ValidationFailure(
    val field: String,
    val should: String
)
