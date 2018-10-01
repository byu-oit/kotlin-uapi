package edu.byu.uapi.http.json

import edu.byu.uapi.server.serialization.NullsAreSpecialArrayStrategyBase
import edu.byu.uapi.server.serialization.NullsAreSpecialTreeStrategyBase
import edu.byu.uapi.server.serialization.NullsAreSpecialValueStrategyBase
import java.math.BigDecimal
import java.math.BigInteger
import javax.json.Json
import javax.json.JsonObjectBuilder
import javax.json.JsonValue

object JsonSerialization {
    class Trees : NullsAreSpecialTreeStrategyBase<Trees, Values, Arrays>() {

        val json: JsonObjectBuilder = Json.createObjectBuilder()

        fun finish() = json.build()

        override fun treeSerializer(): Trees = Trees()
        override fun valueSerializer(): Values = Values()
        override fun arraySerializer(): Arrays = Arrays()

        override fun addValuesFromStrategy(
            key: String,
            strategy: Arrays
        ) {
            json.add(key, strategy.json)
        }

        override fun nullValue(key: String) {
            json.addNull(key)
        }

        override fun safeString(
            key: String,
            value: String
        ) {
            json.add(key, value)
        }

        override fun safeNumber(
            key: String,
            value: Number
        ) {
            json.add(key, value.toJsonValue())
        }

        override fun safeBoolean(
            key: String,
            value: Boolean
        ) {
            json.add(key, value)
        }

        override fun safeEnum(
            key: String,
            value: Enum<*>
        ) {
            json.add(key, value.toString())
        }

        override fun safeAddValueFromStrategy(
            key: String,
            strategy: Values
        ) {
            json.add(key, strategy.finish())
        }

        override fun safeAddValueFromStrategy(
            key: String,
            strategy: Trees
        ) {
            json.add(key, strategy.json)
        }

    }

    class Values : NullsAreSpecialValueStrategyBase<Values, Trees, Arrays>() {

        lateinit var json: JsonValue

        fun finish(): JsonValue = json

        override fun treeSerializer(): Trees = Trees()
        override fun arraySerializer(): Arrays = Arrays()

        override fun addValuesFromStrategy(strategy: Arrays) {
            json = strategy.finish()
        }

        override fun nullValue() {
            json = JsonValue.NULL
        }

        override fun safeString(value: String) {
            json = Json.createValue(value)
        }

        override fun safeNumber(value: Number) {
            json = value.toJsonValue()
        }

        override fun safeBoolean(value: Boolean) {
            json = value.toJsonValue()
        }

        override fun safeEnum(value: Enum<*>) {
            json = Json.createValue(value.toString())
        }

        override fun safeAddValueFromStrategy(strategy: Trees) {
            json = strategy.finish()
        }
    }

    class Arrays : NullsAreSpecialArrayStrategyBase<Arrays, Trees, Values>() {

        val json = Json.createArrayBuilder()

        fun finish() = json.build()

        override fun treeSerializer(): Trees = Trees()
        override fun valueSerializer(): Values = Values()
        override fun arraySerializer(): Arrays = Arrays()

        override fun addNull() {
            json.addNull()
        }

        override fun safeAddString(value: String) {
            json.add(value)
        }

        override fun safeAddNumber(value: Number) {
            json.add(value.toJsonValue())
        }

        override fun safeAddBoolean(value: Boolean) {
            json.add(value)
        }

        override fun safeAddEnum(value: Enum<*>) {
            json.add(value.toString())
        }

        override fun safeAddValueFromStrategy(value: Trees) {
            json.add(value.json)
        }

        override fun safeAddValueFromStrategy(value: Values) {
            json.add(value.json)
        }

        override fun safeAddValuesFromStrategy(value: Arrays) {
            json.add(value.json)
        }
    }
}

private fun Boolean.toJsonValue(): JsonValue = if (this) JsonValue.TRUE else JsonValue.FALSE

private fun Number.toJsonValue(): JsonValue {
    return when (this) {
        is Byte -> Json.createValue(this.toInt())
        is Short -> Json.createValue(this.toInt())
        is Int -> Json.createValue(this)
        is Long -> Json.createValue(this)
        is Float -> Json.createValue(this.toDouble())
        is Double -> Json.createValue(this)
        is BigInteger -> Json.createValue(this)
        is BigDecimal -> Json.createValue(this)
        else -> Json.createValue(this.toDouble())
    }
}
