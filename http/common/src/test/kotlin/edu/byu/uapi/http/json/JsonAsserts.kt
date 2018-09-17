package edu.byu.uapi.http.json

import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.maps.shouldContainKey
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import javax.json.JsonNumber
import javax.json.JsonObject
import javax.json.JsonString
import javax.json.JsonValue
import kotlin.math.exp

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
    value: Byte
) {
    shouldHave(key, value) {
        this.getInt(it).toByte()
    }
}

fun JsonObject.shouldHave(
    key: String,
    value: Short
) {
    shouldHave(key, value) {
        this.getInt(it).toShort()
    }
}

fun JsonObject.shouldHave(
    key: String,
    value: Int
) {
    shouldHave(key, value, JsonObject::getInt)
}

fun JsonObject.shouldHave(
    key: String,
    value: Long
) {
    shouldHave(key, value) {
        this.getJsonNumber(it).longValue()
    }
}


fun JsonObject.shouldHave(
    key: String,
    value: Float
) {
    shouldHave(key, value) {
        this.getJsonNumber(it).doubleValue().toFloat()
    }
}

fun JsonObject.shouldHave(
    key: String,
    value: Double
) {
    shouldHave(key, value) {
        this.getJsonNumber(it).doubleValue()
    }
}

fun JsonObject.shouldHave(
    key: String,
    value: Boolean
) {
    shouldHave(key, value, JsonObject::getBoolean)
}

fun JsonObject.shouldHave(
    key: String,
    validation: (JsonObject) -> Boolean
) {
//  this.mustHave(key) && validation(this.getJsonObject(key))
    this.shouldHave(key)
    validation(this.getJsonObject(key))
}

fun JsonObject.shouldHaveNull(
    key: String
) {
    this.shouldContainKey(key)
    this[key] shouldBe JsonValue.NULL
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

fun JsonObject.shouldHaveAllBytes(
    key: String,
    value: Collection<Byte>
) = shouldHaveAll(key, value, JsonNumber::intValue)

fun JsonObject.shouldHaveAllShorts(
    key: String,
    value: Collection<Short>
) = shouldHaveAll<Short, JsonNumber>(key, value) {
    this.intValue().toShort()
}

fun JsonObject.shouldHaveAllInts(
    key: String,
    value: Collection<Int>
) = shouldHaveAll(key, value, JsonNumber::intValue)

fun JsonObject.shouldHaveAllDoubles(
    key: String,
    value: Collection<Double>
) = shouldHaveAll(key, value, JsonNumber::doubleValue)

fun JsonObject.shouldHaveAllFloats(
    key: String,
    value: Collection<Float>
) = shouldHaveAll<Float, JsonNumber>(key, value) { this.bigDecimalValue().toFloat() }

fun JsonObject.shouldHaveAllLongs(
    key: String,
    value: Collection<Long>
) = shouldHaveAll(key, value, JsonNumber::longValue)

fun JsonObject.shouldHaveAllBooleans(
    key: String,
    value: List<Boolean>
) = shouldHaveAll<Boolean, JsonValue>(key, value) {
    when(this) {
        JsonValue.TRUE -> true
        JsonValue.FALSE -> false
        else -> throw AssertionError("Expected value of TRUE or FALSE, was \"$this\"")
    }
}

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

fun JsonObject.shouldHaveArray(key: String) {
    this.shouldHave(key)
    this[key]!!.valueType shouldBe JsonValue.ValueType.ARRAY
}

fun <T: Any> JsonObject.shouldHaveObjectArrayMatching(key: String, expected: List<T>, matcher: (JsonObject, T) -> Unit) {
    this.shouldHaveArray(key)
    val array = this.getJsonArray(key)
    array.shouldHaveSize(expected.size)
    array.forEach { it.valueType shouldBe JsonValue.ValueType.OBJECT }
    array.map { it.asJsonObject()!! }
        .zip(expected)
        .forEach { matcher(it.first, it.second) }
}

fun <T : Any> JsonObject.shouldHave(
    key: String,
    value: T,
    extract: JsonObject.(String) -> T
) {
    this.shouldHave(key)
    this.extract(key) shouldBe value
}
