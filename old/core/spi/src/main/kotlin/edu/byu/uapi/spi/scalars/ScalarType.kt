package edu.byu.uapi.spi.scalars

import edu.byu.uapi.model.UAPIValueConstraints
import edu.byu.uapi.model.UAPIValuePropertyTypeModel
import edu.byu.uapi.model.UAPIValueType
import edu.byu.uapi.spi.rendering.ScalarRenderer
import kotlin.reflect.KClass

interface ScalarType<T : Any> {
    val type: KClass<T>
    val scalarFormat: ScalarFormat
    fun fromString(value: String): T
    fun renderToString(value: T): String = value.toString()
    fun <S> render(
        value: T,
        renderer: ScalarRenderer<S>
    ): S

    val valueType: UAPIValueType
    val constraints: UAPIValueConstraints?
        get() = null

    fun asValueDefinition(): UAPIValuePropertyTypeModel {
        return UAPIValuePropertyTypeModel(
            valueType,
            constraints
        )
    }
}
