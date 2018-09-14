package edu.byu.uapi.http.json

import edu.byu.uapi.server.types.SerializationStrategyBase
import javax.json.Json
import javax.json.JsonObject
import javax.json.JsonObjectBuilder

class JsonSerializationStrategy : SerializationStrategyBase<JsonSerializationStrategy>() {

    val root: JsonObjectBuilder = Json.createObjectBuilder()

    fun finish(): JsonObject = root.build()

    override fun createSerializer() = JsonSerializationStrategy()

    override fun addFromSerializer(
        key: String,
        ser: JsonSerializationStrategy?
    ) {
        if (ser == null) {
            root.addNull(key)
        } else {
            root.add(key, ser.root)
        }
    }

    override fun addListFromSerializers(
        key: String,
        sers: List<JsonSerializationStrategy>
    ) {
        root.add(key, sers.fold(Json.createArrayBuilder()) {arr, cur -> arr.add(cur.root)})
    }

    override fun add(
        key: String,
        v: String?
    ) {
        if (v == null) {
            root.addNull(key)
        } else {
            root.add(key, v)
        }
    }

    override fun add(
        key: String,
        v: Int?
    ) {
        if (v == null) {
            root.addNull(key)
        } else {
            root.add(key, v)
        }
    }

    override fun add(
        key: String,
        v: Double?
    ) {
        if (v == null) {
            root.addNull(key)
        } else {
            root.add(key, v)
        }
    }

    override fun add(
        key: String,
        v: Boolean?
    ) {
        if (v == null) {
            root.addNull(key)
        } else {
            root.add(key, v)
        }
    }

    override fun add(
        key: String,
        v: Enum<*>?
    ) {
        if (v == null) {
            root.addNull(key)
        } else {
            root.add(key, v.toString())
        }
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

    override fun ints(
        key: String,
        v: Collection<Int>
    ) {
        root.add(key, Json.createArrayBuilder(v))
    }

    override fun ints(
        key: String,
        v: IntArray
    ) {
        root.add(key, Json.createArrayBuilder(v.toList()))
    }

    override fun doubles(
        key: String,
        v: Collection<Double>
    ) {
        root.add(key, Json.createArrayBuilder(v))
    }

    override fun doubles(
        key: String,
        v: DoubleArray
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

}
