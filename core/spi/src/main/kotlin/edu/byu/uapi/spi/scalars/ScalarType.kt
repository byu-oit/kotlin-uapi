package edu.byu.uapi.spi.scalars

import edu.byu.uapi.spi.dictionary.MaybeTypeFailure
import edu.byu.uapi.spi.rendering.ScalarRenderer
import kotlin.reflect.KClass

interface ScalarType<T : Any> {
    val type: KClass<T>
    val scalarFormat: ScalarFormat
    fun fromString(value: String): MaybeTypeFailure<T>
    fun renderToString(value: T): String = value.toString()
    fun <S> render(
        value: T,
        renderer: ScalarRenderer<S>
    ): S
}
