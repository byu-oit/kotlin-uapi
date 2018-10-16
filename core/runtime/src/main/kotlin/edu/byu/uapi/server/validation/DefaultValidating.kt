package edu.byu.uapi.server.validation

import edu.byu.uapi.spi.validation.Validating
import edu.byu.uapi.spi.validation.ValidationFailure
import java.util.*

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

