package edu.byu.uapi.server.http.errors

import edu.byu.uapi.server.spi.errors.UAPIInternalError

class UAPIHttpInternalError(
    message: String,
    cause: Throwable? = null
) : UAPIInternalError(message, cause)
