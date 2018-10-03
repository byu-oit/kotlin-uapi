package edu.byu.uapi.http.json

//import edu.byu.uapi.server.serialization.TreeSerializationStrategy
//import edu.byu.uapi.server.serialization.UAPISerializableTree
//import io.kotlintest.matchers.collections.shouldContainExactly
//import io.kotlintest.properties.Gen
//import io.kotlintest.properties.forAll
//import io.kotlintest.specs.DescribeSpec
//import javax.json.JsonObject
//import kotlin.reflect.KClass
//
//class JsonSerializationTreesSpec : DescribeSpec() {
//
//    fun jsonKey(): Gen<String> = Gen.string().filter { it.isNotBlank() }
//
//    fun stringMap() = Gen.map(jsonKey(), Gen.string())
//
//    fun serializables(): Gen<TestUAPISerializable> = Gen.bind(stringMap()) { m ->
//        TestUAPISerializable(m)
//    }
//
//    fun bytes() = Gen.choose(Byte.MIN_VALUE.toInt(), Byte.MAX_VALUE.toInt()).map { it.toByte() }
//    fun shorts() = Gen.choose(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).map { it.toShort() }
//
//    fun finiteFloats(): Gen<Float> = Gen.float().filter { it.isFinite() }
//
//    fun finiteDoubles(): Gen<Double> = Gen.double().filter { it.isFinite() }
//
//    data class TestUAPISerializable(val map: Map<String, String>) : UAPISerializableTree {
//        override fun serialize(strategy: TreeSerializationStrategy) {
//            map.forEach { k, v -> strategy.string(k, v) }
//        }
//    }
//
//    init {
//        describe("scalars") {
//            scalarSerialization(
//                Gen.string(),
//                JsonSerialization.Trees::string,
//                JsonObject::shouldHave
//            )
//            scalarSerialization(
//                Gen.int(),
//                JsonSerialization.Trees::number,
//                JsonObject::shouldHave
//            )
//            scalarSerialization(
//                bytes(),
//                JsonSerialization.Trees::number,
//                JsonObject::shouldHave
//            )
//            scalarSerialization(
//                shorts(),
//                JsonSerialization.Trees::number,
//                JsonObject::shouldHave
//            )
//            scalarSerialization(
//                Gen.long(),
//                JsonSerialization.Trees::number,
//                JsonObject::shouldHave
//            )
//            scalarSerialization(
//                finiteFloats(),
//                JsonSerialization.Trees::number,
//                JsonObject::shouldHave
//            )
//            scalarSerialization(
//                finiteDoubles(),
//                JsonSerialization.Trees::number,
//                JsonObject::shouldHave
//            )
//            scalarSerialization(
//                Gen.bool(),
//                JsonSerialization.Trees::boolean,
//                JsonObject::shouldHave
//            )
//            scalarSerialization(
//                Gen.enum<TestEnum>(),
//                JsonSerialization.Trees::enum
//            ) { key, value ->
//                this.shouldHave(key, value.toString())
//            }
//            scalarSerialization(
//                Gen.enum<TestEnumWithToString>(),
//                JsonSerialization.Trees::enum
//            ) { key, value ->
//                this.shouldHave(key, value.toString())
//            }
//        }
//        describe("trees") {
//            context("tree(key, UAPISerializableTree?)") {
//                it("serializes a UAPISerializableTree") {
//                    forAll(100, Gen.string(), serializables()) { key, value ->
//                        val json = setup { it.tree(key, value) }
//                        json.shouldHaveObject(key) { obj ->
//                            obj.shouldMatch(value)
//                        }
//                        true
//                    }
//                }
//                it("serializes nulls") {
//                    val json = setup { it.tree("key", null as UAPISerializableTree?) }
//
//                    json.shouldHaveNull("key")
//                }
//            }
//            context("Strategy receiver") {
//                it("serializes from a receiver function") {
//                    forAll(100, Gen.string(), stringMap()) { key, value ->
//                        val json = setup {
//                            it.tree(key) {
//                                value.forEach { k, v -> string(k, v) }
//                            }
//                        }
//                        json.shouldHaveObject(key) { obj ->
//                            obj.shouldMatch(value)
//                        }
//                        true
//                    }
//                }
//            }
//            context("Map of String -> UAPISerializableTree?") {
//                it("serializes a map") {
//                    forAll(100,
//                           Gen.string(), serializableMap()) { key, value ->
//                        val json = setup { it.tree(key, value) }
//                        // 🤮 we have to recurse a LOT to check this structure. But worth it to have this convenient method!
//                        json.shouldHaveObject(key) { shallow ->
//                            shallow.keys.shouldContainExactly(value.keys)
//                            value.forEach { k, v ->
//                                shallow.shouldHaveObject(k) { deep ->
//                                    deep.shouldMatch(v)
//                                }
//                            }
//                        }
//                        true
//                    }
//                }
//            }
//        }
//        describe("arrays") {
//            scalarArraySerialization(
//                Gen.string(),
//                List<String>::toTypedArray,
//                JsonSerialization.Trees::strings,
//                JsonSerialization.Trees::strings,
//                JsonObject::shouldHaveAllStrings
//            )
//            scalarArraySerialization(
//                shorts(),
//                JsonSerialization.Trees::numbers,
//                JsonObject::shouldHaveAllShorts
//            )
////            scalarArraySerialization(
////                bytes(),
////                JsonSerialization.Trees::numbers,
////                JsonObject::shouldHaveAllBytes
////            )
//            scalarArraySerialization(
//                Gen.int(),
//                List<Int>::toIntArray,
//                JsonSerialization.Trees::numbers,
//                JsonSerialization.Trees::numbers,
//                JsonObject::shouldHaveAllInts
//            )
//            scalarArraySerialization(
//                Gen.long(),
//                List<Long>::toLongArray,
//                JsonSerialization.Trees::numbers,
//                JsonSerialization.Trees::numbers,
//                JsonObject::shouldHaveAllLongs
//            )
//            scalarArraySerialization(
//                finiteFloats(),
//                List<Float>::toFloatArray,
//                JsonSerialization.Trees::numbers,
//                JsonSerialization.Trees::numbers,
//                JsonObject::shouldHaveAllFloats
//            )
//            scalarArraySerialization(
//                finiteDoubles(),
//                List<Double>::toDoubleArray,
//                JsonSerialization.Trees::numbers,
//                JsonSerialization.Trees::numbers,
//                JsonObject::shouldHaveAllDoubles
//            )
//            scalarArraySerialization(
//                Gen.bool(),
//                List<Boolean>::toBooleanArray,
//                JsonSerialization.Trees::booleans,
//                JsonSerialization.Trees::booleans,
//                JsonObject::shouldHaveAllBooleans
//            )
//            context("object array") {
//                it("serializes a collection") {
//                    forAll(100, jsonKey(), Gen.list(serializables())) { key, value ->
//                        val json = setup {
//                            it.trees(key, value)
//                        }
//
//                        json.shouldHaveArray(key)
//                        json.shouldHaveObjectArrayMatching(key, value) { actual, expected ->
//                            actual.shouldMatch(expected)
//                        }
//                        true
//                    }
//                }
//            }
//        }
//
//        describe("mergeTree") {
//            it("serializes a map of UAPISerializableTree") {
//                forAll(100,
//                       serializableMap()
//                ) { toMerge ->
//                    val json = setup {
//                        it.string("already_there", "foo")
//                        it.mergeTree(toMerge)
//                    }
//
//                    json.shouldHave("already_there", "foo")
//                    toMerge.forEach { k, v ->
//                        json.shouldHaveObject(k) {
//                            it.shouldMatch(v.map)
//                        }
//                    }
//
//                    true
//                }
//            }
//        }
//
//    }
//
//    private fun serializableMap() = Gen.map(jsonKey(), serializables())
//
//    private inline fun setup(fn: (JsonSerialization.Trees) -> Unit): JsonObject {
//        val ser = JsonSerialization.Trees()
//        fn(ser)
//        return ser.finish()
//    }
//
//    private fun JsonObject.shouldMatch(expected: TestUAPISerializable) {
//        this.shouldMatch(expected.map)
//    }
//
//    private fun JsonObject.shouldMatch(expected: Map<String, String>) {
//        this.keys.shouldContainExactly(expected.keys)
//        expected.forEach { k, v -> this.shouldHave(k, v) }
//    }
//
//    private fun nameOf(type: KClass<*>) = type.simpleName ?: type.toString()
//
//    private enum class TestEnum {
//        ONE, TWO, THREE_IS_BEST
//    }
//
//    private enum class TestEnumWithToString {
//        ONE, TWO, THREE_IS_BEST;
//
//        override fun toString(): String {
//            return "special_" + this.name
//        }
//    }
//
//    private inline fun <reified T> DescribeScope.scalarSerialization(
//        gen: Gen<T>,
//        crossinline add: JsonSerialization.Trees.(String, T?) -> Unit,
//        crossinline shouldHave: JsonObject.(String, T) -> Unit
//    ) {
//        context(nameOf(T::class)) {
//            it("serializes any values") {
//                forAll(100, jsonKey(), gen) { key, value ->
//                    val json = setup { it.add(key, value) }
//                    json.shouldHave(key, value)
//                    true
//                }
//            }
//            it("serializes nulls") {
//                val json = setup { it.add("key", null as T?) }
//
//                json.shouldHaveNull("key")
//            }
//        }
//    }
//
//    private inline fun <reified T> DescribeScope.scalarArraySerialization(
//        gen: Gen<T>,
//        crossinline addCollection: JsonSerialization.Trees.(String, Collection<T>) -> Unit,
//        crossinline shouldHaveAll: JsonObject.(String, List<T>) -> Unit
//    ) {
//        context("array of " + nameOf(T::class)) {
//            itShouldSerializeACollection(gen, addCollection, shouldHaveAll)
//        }
//    }
//
//    private inline fun <reified T> DescribeScope.itShouldSerializeACollection(
//        gen: Gen<T>,
//        crossinline addCollection: JsonSerialization.Trees.(String, Collection<T>) -> Unit,
//        crossinline shouldHaveAll: JsonObject.(String, List<T>) -> Unit
//    ) {
//        it("serializes a collection") {
//            forAll(100, jsonKey(), Gen.list(gen)) { key, list ->
//                val json = setup { it.addCollection(key, list) }
//
//                json.shouldHaveAll(key, list)
//                true
//            }
//        }
//    }
//
//    private inline fun <reified T, Array> DescribeScope.scalarArraySerialization(
//        gen: Gen<T>,
//        crossinline toArray: (List<T>) -> Array,
//        crossinline addCollection: JsonSerialization.Trees.(String, Collection<T>) -> Unit,
//        crossinline addArray: JsonSerialization.Trees.(String, Array) -> Unit,
//        crossinline shouldHaveAll: JsonObject.(String, List<T>) -> Unit
//    ) {
//        context("array of " + nameOf(T::class)) {
//            itShouldSerializeACollection(gen, addCollection, shouldHaveAll)
//            it("serializes an array") {
//                forAll(100, jsonKey(), Gen.list(gen)) { key, list ->
//                    val array = toArray(list)
//                    val json = setup { it.addArray(key, array) }
//
//                    json.shouldHaveAll(key, list)
//                    true
//                }
//            }
//        }
//    }
//}
//
