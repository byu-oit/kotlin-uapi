package edu.byu.uapi.spi.validation

interface Validating {

    fun expect(field: String, should: String, condition: () -> Boolean): Boolean

    fun collectFailures(): List<ValidationFailure>

}
