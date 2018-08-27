package edu.byu.uapidsl.http

import edu.byu.uapidsl.types.ErrorResponse
import edu.byu.uapidsl.types.UAPIErrorMetadata
import edu.byu.uapidsl.types.ValidationResponse


open class HttpError(
    val code: Int,
    override val message: String,
    val information: List<String> = emptyList(),
    cause: Throwable? = null
) : Exception(message, cause) {
    fun toResponse() = ErrorResponse(
        UAPIErrorMetadata(
            ValidationResponse(code, message),
            information
        )
    )
}

class BadCredentialsException(
    message: String,
    information: List<String> = emptyList(),
    cause: Throwable? = null
) : HttpError(401, message, information, cause)

class NoCredentialsException(
    message: String,
    information: List<String> = emptyList()
) : HttpError(401, message, information)


class NotFoundException(
    val type: String,
    val identifier: Any
): HttpError(404, "$type with ID of $identifier does not exist.")

class NotAuthorizedToViewException: HttpError(403, "You are not canUserCreate to view this resources")
