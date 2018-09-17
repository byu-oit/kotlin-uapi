package edu.byu.uapi.http.json

import edu.byu.uapi.server.types.NullsAreSpecialSerializationStrategy
import edu.byu.uapi.server.types.SerializationStrategyBase
import java.math.BigDecimal
import java.math.BigInteger
import javax.json.Json
import javax.json.JsonObject
import javax.json.JsonObjectBuilder
import javax.json.JsonValue

class JsonSerializationStrategy : NullsAreSpecialSerializationStrategy<JsonSerializationStrategy>() {
    override fun addNull(key: String) {
        root.addNull(key)
    }

    val root: JsonObjectBuilder = Json.createObjectBuilder()

    fun finish(): JsonObject = root.build()

    override fun createSerializer() = JsonSerializationStrategy()

    override fun addListFromSerializers(
        key: String,
        sers: List<JsonSerializationStrategy>
    ) {
        root.add(key, sers.fold(Json.createArrayBuilder()) {arr, cur -> arr.add(cur.root)})
    }

    override fun strings(
        key: String,
        v: Collection<String>
    ) {
        root.add(key, Json.createArrayBuilder(v))
    }

    override fun strings(
        key: String,
        v: Array<String>
    ) {
        root.add(key, Json.createArrayBuilder(v.toList()))
    }


    override fun booleans(
        key: String,
        v: Collection<Boolean>
    ) {
        root.add(key, Json.createArrayBuilder(v))
    }

    override fun booleans(
        key: String,
        v: BooleanArray
    ) {
        root.add(key, Json.createArrayBuilder(v.toList()))
    }

    override fun numbers(
        key: String,
        v: Collection<Number>
    ) {
        if (v.isEmpty()) {
            root.add(key, JsonValue.EMPTY_JSON_ARRAY)
        } else {
            val first = v.first()
            val list = when(first) {
                is Byte -> v.map { it.toInt() }
                is Short -> v.map { it.toInt() }
                is Float -> v.map { it.toDouble() }
                else -> v
            }
            root.add(key, Json.createArrayBuilder(list))
        }
    }

    override fun numbers(
        key: String,
        v: IntArray
    ) {
        root.add(key, Json.createArrayBuilder(v.toList()))
    }

    override fun numbers(
        key: String,
        v: LongArray
    ) {
        root.add(key, Json.createArrayBuilder(v.toList()))
    }

    override fun numbers(
        key: String,
        v: FloatArray
    ) {
        root.add(key, Json.createArrayBuilder(v.map { it.toDouble() }.toList()))
    }

    override fun numbers(
        key: String,
        v: DoubleArray
    ) {
        root.add(key, Json.createArrayBuilder(v.toList()))
    }

    override fun safeAdd(
        key: String,
        v: String
    ) {
        root.add(key, v)
    }

    override fun safeAdd(
        key: String,
        v: Number
    ) {
        when(v) {
            is Byte -> root.add(key, v.toInt())
            is Short -> root.add(key, v.toInt())
            is Int -> root.add(key, v)
            is Long -> root.add(key, v)
            is Float -> root.add(key, v.toDouble())
            is Double -> root.add(key, v)
            is BigInteger -> root.add(key, v)
            is BigDecimal -> root.add(key, v)
        }
    }

    override fun safeAdd(
        key: String,
        v: Boolean
    ) {
        root.add(key, v)
    }

    override fun safeAdd(
        key: String,
        v: Enum<*>
    ) {
        root.add(key, v.toString())
    }

    override fun safeAddFromSerializer(
        key: String,
        ser: JsonSerializationStrategy
    ) {
        root.add(key, ser.root)
    }
}
