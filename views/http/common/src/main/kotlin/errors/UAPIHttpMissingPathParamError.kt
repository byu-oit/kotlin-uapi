package edu.byu.uapi.server.http.errors

import edu.byu.uapi.server.spi.errors.UAPIMissingIdParamValueError

class UAPIHttpMissingCompoundPathParamError(
    paramNames: List<String>
) : UAPIMissingIdParamValueError(paramNames)
