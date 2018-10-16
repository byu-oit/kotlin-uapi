package edu.byu.uapi.spi.scalars

import edu.byu.uapi.spi.dictionary.DeserializationFailure
import edu.byu.uapi.spi.functional.SuccessOrFailure
import edu.byu.uapi.spi.rendering.ScalarRenderer
import kotlin.reflect.KClass

interface ScalarType<T : Any> {
    val type: KClass<T>
    fun fromString(value: String): SuccessOrFailure<T, DeserializationFailure<T>>
    fun <S> render(
        value: T,
        renderer: ScalarRenderer<S>
    ): S
}
