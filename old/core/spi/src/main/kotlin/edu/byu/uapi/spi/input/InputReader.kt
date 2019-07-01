package edu.byu.uapi.spi.input

import edu.byu.uapi.spi.dictionary.TypeDictionary
import kotlin.reflect.KClass

interface InputReader<Input: Any> {
    val typeDictionary: TypeDictionary

    fun string(
        key: String
    ): String = value(key, String::class)

    fun optString(
        key: String
    ): String? = optValue(key, String::class)

    fun <T: Any> value(
        key: String,
        type: KClass<T>
    ): T

    fun <T: Any> optValue(
        key: String,
        type: KClass<T>
    ): T?

    fun <T: Any> valueOr(
        key: String,
        getDefault: () -> T
    ): T

    fun <T: Any> valueList(
        key: String,
        type: KClass<T>
    ): List<T>

    fun <T: Any> optValueList(
        key: String,
        type: KClass<T>
    ): List<T>?

    fun <T: Any> nullableValueList(
        key: String,
        type: KClass<T>
    ): List<T?>

    fun <T: Any> tree()

}
