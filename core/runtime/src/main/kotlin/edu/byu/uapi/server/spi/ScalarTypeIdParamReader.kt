package edu.byu.uapi.server.spi

import edu.byu.uapi.spi.functional.Failure
import edu.byu.uapi.spi.input.IdParamMeta
import edu.byu.uapi.spi.input.IdParamReader
import edu.byu.uapi.spi.input.ParamReadFailure
import edu.byu.uapi.spi.input.ParamReadResult
import edu.byu.uapi.spi.requests.IdParams
import edu.byu.uapi.spi.scalars.ScalarType

class ScalarTypeIdParamReader<T : Any>(
    prefix: String,
    val type: ScalarType<T>
) : IdParamReader<T> {
    private val paramName = prefix + "id"

    override fun read(input: IdParams): ParamReadResult<T> {
        return input[paramName]?.asScalar(type) ?: Failure(ParamReadFailure(paramName, type.type, "No parameter value specified"))
    }

    override fun describe(): IdParamMeta = IdParamMeta.Default(listOf(
        IdParamMeta.Param(paramName, type.scalarFormat)
    ))
}