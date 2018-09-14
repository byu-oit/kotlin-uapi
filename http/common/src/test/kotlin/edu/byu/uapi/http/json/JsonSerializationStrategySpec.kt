package edu.byu.uapi.http.json

import edu.byu.uapi.server.types.SerializationStrategy
import edu.byu.uapi.server.types.UAPISerializable
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll
import io.kotlintest.specs.DescribeSpec
import javax.json.JsonObject
import kotlin.reflect.KClass

class JsonSerializationStrategySpec : DescribeSpec() {

    fun jsonKey(): Gen<String> = Gen.string().filter { it.isNotBlank() }

    fun stringMap() = Gen.map(jsonKey(), Gen.string())

    fun serializables(): Gen<TestUAPISerializable> = Gen.bind(stringMap()) { m ->
        TestUAPISerializable(m)
    }

    fun finiteDoubles(): Gen<Double> = Gen.double().filter { it.isFinite() }

    data class TestUAPISerializable(val map: Map<String, String>) : UAPISerializable {
        override fun serialize(ser: SerializationStrategy) {
            map.forEach { k, v -> ser.add(k, v) }
        }
    }

    init {
        describe("scalars") {
            scalarSerialization(
                Gen.string(),
                JsonSerializationStrategy::add,
                JsonObject::shouldHave
            )
            scalarSerialization(
                Gen.int(),
                JsonSerializationStrategy::add,
                JsonObject::shouldHave
            )
            scalarSerialization(
                Gen.double().filter { it.isFinite() },
                JsonSerializationStrategy::add,
                JsonObject::shouldHave
            )
            scalarSerialization(
                Gen.bool(),
                JsonSerializationStrategy::add,
                JsonObject::shouldHave
            )
            scalarSerialization(
                Gen.enum<TestEnum>(),
                JsonSerializationStrategy::add
            ) { key, value ->
                this.shouldHave(key, value.toString())
            }
            scalarSerialization(
                Gen.enum<TestEnumWithToString>(),
                JsonSerializationStrategy::add
            ) { key, value ->
                this.shouldHave(key, value.toString())
            }
        }
        describe("objects") {
            context("obj(key, UAPISerializable?)") {
                it("serializes a UAPISerializable") {
                    forAll(100, Gen.string(), serializables()) { key, value ->
                        val json = setup { it.obj(key, value) }
                        json.shouldHaveObject(key) { obj ->
                            obj.shouldMatch(value)
                        }
                        true
                    }
                }
                it("serializes nulls") {
                    val json = setup { it.obj("key", null as UAPISerializable?) }

                    json.shouldHaveNull("key")
                }
            }
            context("Strategy receiver") {
                it("serializes from a receiver function") {
                    forAll(100, Gen.string(), stringMap()) { key, value ->
                        val json = setup {
                            it.obj(key) {
                                value.forEach { k, v -> add(k, v) }
                            }
                        }
                        json.shouldHaveObject(key) { obj ->
                            obj.shouldMatch(value)
                        }
                        true
                    }
                }
            }
            context("Map of String -> UAPISerializable?") {
                it("serializes a map") {
                    forAll(100,
                           Gen.string(), serializableMap()) { key, value ->
                        val json = setup { it.obj(key, value) }
                        // 🤮 we have to recurse a LOT to check this structure. But worth it to have this convenient method!
                        json.shouldHaveObject(key) { shallow ->
                            shallow.keys.shouldContainExactly(value.keys)
                            value.forEach { k, v ->
                                shallow.shouldHaveObject(k) { deep ->
                                    deep.shouldMatch(v)
                                }
                            }
                        }
                        true
                    }
                }
            }
        }
        describe("arrays") {
            scalarArraySerialization(
                Gen.string(),
                List<String>::toTypedArray,
                JsonSerializationStrategy::strings,
                JsonSerializationStrategy::strings,
                JsonObject::shouldHaveAllStrings
            )
            scalarArraySerialization(
                Gen.int(),
                List<Int>::toIntArray,
                JsonSerializationStrategy::ints,
                JsonSerializationStrategy::ints,
                JsonObject::shouldHaveAllInts
            )
            scalarArraySerialization(
                finiteDoubles(),
                List<Double>::toDoubleArray,
                JsonSerializationStrategy::doubles,
                JsonSerializationStrategy::doubles,
                JsonObject::shouldHaveAllDoubles
            )
            scalarArraySerialization(
                Gen.bool(),
                List<Boolean>::toBooleanArray,
                JsonSerializationStrategy::booleans,
                JsonSerializationStrategy::booleans,
                JsonObject::shouldHaveAllBooleans
            )
            context("object array") {
                it("serializes a collection") {
                    forAll(100, jsonKey(), Gen.list(serializables())) { key, value ->
                        val json = setup {
                            it.objects(key, value)
                        }

                        json.shouldHaveArray(key)
                        json.shouldHaveObjectArrayMatching(key, value) { actual, expected ->
                            actual.shouldMatch(expected)
                        }
                        true
                    }
                }
            }
        }

        describe("merge") {
            it("serializes a map of UAPISerializable") {
                forAll(100,
                       serializableMap()
                ) { toMerge ->
                    val json = setup {
                        it.add("already_there", "foo")
                        it.merge(toMerge)
                    }

                    json.shouldHave("already_there", "foo")
                    toMerge.forEach { k, v -> json.shouldHaveObject(k) {
                        it.shouldMatch(v.map)
                    } }

                    true
                }
            }
        }

    }

    private fun serializableMap() = Gen.map(jsonKey(), serializables())

    private inline fun setup(fn: (JsonSerializationStrategy) -> Unit): JsonObject {
        val ser = JsonSerializationStrategy()
        fn(ser)
        return ser.finish()
    }

    private fun JsonObject.shouldMatch(expected: TestUAPISerializable) {
        this.shouldMatch(expected.map)
    }

    private fun JsonObject.shouldMatch(expected: Map<String, String>) {
        this.keys.shouldContainExactly(expected.keys)
        expected.forEach { k, v -> this.shouldHave(k, v) }
    }

    private fun nameOf(type: KClass<*>) = type.simpleName ?: type.toString()

    private enum class TestEnum {
        ONE, TWO, THREE_IS_BEST
    }

    private enum class TestEnumWithToString {
        ONE, TWO, THREE_IS_BEST;

        override fun toString(): String {
            return "special_" + this.name
        }
    }

    private inline fun <reified T> DescribeScope.scalarSerialization(
        gen: Gen<T>,
        crossinline add: JsonSerializationStrategy.(String, T?) -> Unit,
        crossinline shouldHave: JsonObject.(String, T) -> Unit
    ) {
        context(nameOf(T::class)) {
            it("serializes any values") {
                forAll(100, jsonKey(), gen) { key, value ->
                    val json = setup { it.add(key, value) }
                    json.shouldHave(key, value)
                    true
                }
            }
            it("serializes nulls") {
                val json = setup { it.add("key", null as T?) }

                json.shouldHaveNull("key")
            }
        }
    }

    private inline fun <reified T, Array> DescribeScope.scalarArraySerialization(
        gen: Gen<T>,
        crossinline toArray: (List<T>) -> Array,
        crossinline addCollection: JsonSerializationStrategy.(String, Collection<T>) -> Unit,
        crossinline addArray: JsonSerializationStrategy.(String, Array) -> Unit,
        crossinline shouldHaveAll: JsonObject.(String, List<T>) -> Unit
    ) {
        context("array of " + nameOf(T::class)) {
            it("serializes a collection") {
                forAll(100, jsonKey(), Gen.list(gen)) { key, list ->
                    val json = setup { it.addCollection(key, list) }

                    json.shouldHaveAll(key, list)
                    true
                }
            }
            it("serializes an array") {
                forAll(100, jsonKey(), Gen.list(gen)) { key, list ->
                    val array = toArray(list)
                    val json = setup { it.addArray(key, array) }

                    json.shouldHaveAll(key, list)
                    true
                }
            }
        }
    }
}

