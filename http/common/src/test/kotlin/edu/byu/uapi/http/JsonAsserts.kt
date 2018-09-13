package edu.byu.uapi.http

import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.maps.shouldContainKey
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import javax.json.JsonObject
import javax.json.JsonString
import javax.json.JsonValue

fun JsonObject.mustHave(
    key: String,
    value: String
) = mustHave(key, value, JsonObject::getString)

fun JsonObject.shouldHave(
    key: String,
    value: String
) {
    this.shouldHave(key, value, JsonObject::getString)
}

fun JsonObject.mustHave(
    key: String,
    value: Int
) = mustHave(key, value, JsonObject::getInt)

fun JsonObject.shouldHave(
    key: String,
    value: Int
) {
    mustHave(key, value, JsonObject::getInt)
}


fun JsonObject.shouldHave(
    key: String,
    validation: (JsonObject) -> Boolean
) {
//  this.mustHave(key) && validation(this.getJsonObject(key))
    this.shouldHave(key)
    validation(this.getJsonObject(key))
}


fun JsonObject.mustHaveObject(
    key: String,
    validation: (JsonObject) -> Boolean
): Boolean = this.mustHave(key) && validation(this.getJsonObject(key))

inline fun JsonObject.shouldHaveObject(
    key: String,
    validation: (JsonObject) -> Unit
) {
    this.shouldHave(key)
    this.get(key)!!.valueType shouldBe JsonValue.ValueType.OBJECT
    validation(this.getJsonObject(key))
}

fun JsonObject.mustHaveAllStrings(
    key: String,
    value: Collection<String>
) = mustHaveAll<String, JsonString>(key, value, JsonString::getString)

fun JsonObject.shouldHaveAllStrings(
    key: String,
    value: Collection<String>
) = shouldHaveAll<String, JsonString>(key, value, JsonString::getString)

fun <T : Any, J : JsonValue> JsonObject.shouldHaveAll(
    key: String,
    value: Collection<T>,
    extract: J.() -> T
) {
    this.shouldHave(key)
    this.getJsonArray(key).getValuesAs<T, J>(extract).shouldContainExactlyInAnyOrder(value.toList())
}

fun JsonObject.mustHave(key: String): Boolean {
    return this.containsKey(key)
        && !this.isNull(key)
}

fun <T : Any, K : JsonValue> JsonObject.mustHaveAll(
    key: String,
    value: Collection<T>,
    extract: K.() -> T
): Boolean {
    return this.mustHave(key)
        && this.getJsonArray(key).getValuesAs(extract) == value.toList()
}

inline fun <T : Any> JsonObject.mustHave(
    key: String,
    value: T,
    func: JsonObject.(String) -> T
) = this.mustHave(key) && this.func(key) == value

fun JsonObject.shouldHave(key: String) {
    this.shouldContainKey(key)
    this.get(key) shouldNotBe JsonValue.NULL
    this.get(key)?.valueType shouldNotBe JsonValue.NULL
}

inline fun <T : Any> JsonObject.shouldHave(
    key: String,
    value: T,
    extract: JsonObject.(String) -> T
) {
    this.shouldHave(key)
    this.extract(key) shouldBe value
}
