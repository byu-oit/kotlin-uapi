package edu.byu.uapi.server.spi

import edu.byu.uapi.server.scalars.EnumScalarType
import kotlin.reflect.KClass

class DefaultParameterStyleEnumScalar<E: Enum<E>>(
    type: KClass<E>
) : EnumScalarType<E>(type, strict = true) {
    override fun renderToString(value: E): String {
        return value.name.toLowerCase()
    }
}
