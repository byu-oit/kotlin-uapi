package edu.byu.uapi.spi.validation

data class ValidationFailure(
    val field: String,
    val should: String
)
