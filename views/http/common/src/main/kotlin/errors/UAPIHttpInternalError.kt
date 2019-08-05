package edu.byu.uapi.server.http.errors

import edu.byu.uapi.server.spi.errors.UAPIInternalError

open class UAPIHttpInternalError(
    message: String,
    cause: Throwable? = null
) : UAPIInternalError(message, cause)

class UAPIHttpMissingPathParamValueError(
    paramName: String
): UAPIHttpInternalError(
    "Unable to find path parameter named '$paramName'"
)
