package edu.byu.uapi.server.spi.errors

@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate")
open class UAPIApplicationError(
    val summary: String,
    val explanation: String,
    cause: Throwable? = null
) : Exception(
    """UAPI Application Error: $summary
        
$explanation

This is not an issue with the UAPI Runtime, but with the application using it.""",
    cause
)
