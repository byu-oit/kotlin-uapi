package edu.byu.uapi.server.spi

import edu.byu.uapi.spi.input.IdParamMeta
import edu.byu.uapi.spi.input.IdParamReader
import edu.byu.uapi.spi.input.ParamReadFailure
import edu.byu.uapi.spi.requests.IdParams
import edu.byu.uapi.spi.scalars.ScalarType

class ScalarTypeIdParamReader<T : Any>(
    private val paramName: String,
    val type: ScalarType<T>
) : IdParamReader<T> {

    override fun read(input: IdParams): T {
        return input[paramName]?.asScalar(type) ?: throw ParamReadFailure(paramName, type.type, "No parameter value specified")
    }

    override fun describe(): IdParamMeta = IdParamMeta.Default(listOf(
        IdParamMeta.Param(paramName, type.valueType, type.constraints)
    ))
}
