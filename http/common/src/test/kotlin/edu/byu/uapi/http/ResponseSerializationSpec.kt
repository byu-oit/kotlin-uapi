package edu.byu.uapi.http

import edu.byu.uapi.server.types.*
import io.kotlintest.matchers.maps.shouldContainExactly
import io.kotlintest.matchers.maps.shouldContainKeys
import io.kotlintest.matchers.maps.shouldNotContainKey
import io.kotlintest.matchers.maps.shouldNotContainKeys
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll
import io.kotlintest.specs.DescribeSpec
import java.time.Instant
import javax.json.JsonString

class ResponseSerializationSpec : DescribeSpec() {

    fun httpStatuses(): Gen<Int> = Gen.choose(100, 599)
    fun validationResponse(): Gen<ValidationResponse> = Gen.bind(httpStatuses(), Gen.string(), ::ValidationResponse)
    fun cacheMeta(): Gen<CacheMeta> = Gen.bind(Gen.choose(Instant.MIN.epochSecond, Instant.MAX.epochSecond)) {
        CacheMeta(Instant.ofEpochSecond(it))
    }

    fun searchContexts(): Gen<Map<String, Collection<String>>> = Gen.bind(Gen.map(Gen.string(), Gen.list(Gen.string()))) { m ->
        m.toList().take(m.size / 4).map {
            it.first to (it.second.take(it.second.size / 4)) as Collection<String>
        }.toMap()
    }

    init {
        describe("ValidationInformation.toJson()") {
            it("Generates JSON properly") {
                forAll(validationResponse()) {
                    val json = it.toJson()

                    json.mustHave("code", it.code)
                        && json.mustHave("message", it.message)
                }
            }
        }

        describe("CacheMeta.toJson()") {
            it("Generates JSON properly") {
                forAll(cacheMeta()) { it ->
                    val json = it.toJson()

                    json.mustHave("date_time", it.dateTime.toString())
                }
            }
        }

        describe("UAPIErrorMetadata.toJson()") {
            checkBasicResponseMetadata { response, infos ->
                UAPIErrorMetadata(response, infos)
            }
        }

        describe("UAPICollectionMetadata.toJson()") {
            checkBasicResponseMetadataWithCache { resp, infos, cache ->
                CollectionMetadata(
                    0, null, null, null, resp, infos, cache
                )
            }
            it("handles collection_size") {
                forAll(Gen.positiveIntegers()) { size ->
                    val input = CollectionMetadata(
                        size
                    )

                    val json = input.toJson()

                    json.mustHave("collection_size", size)
                }
            }
            context("sort metadata") {
                it("handles null sort metadata") {
                    val input = CollectionMetadata(
                        collectionSize = 1,
                        sortMetadata = null
                    )

                    val json = input.toJson()

                    json.shouldNotContainKeys(
                        "sort_properties_available",
                        "sort_properties_default",
                        "sort_order_default"
                    )
                }
                it("serializes properly") {
                    forAll(Gen.list(Gen.string()), Gen.list(Gen.string()), Gen.enum<SortOrder>()) { available, default, order ->
                        val input = CollectionMetadata(
                            0,
                            sortMetadata = SortableCollectionMetadata(
                                sortPropertiesAvailable = available.toList(),
                                sortPropertiesDefault = default.toList(),
                                sortOrderDefault = order
                            )
                        )

                        val json = input.toJson()

                        json.shouldContainKeys(
                            "sort_properties_available",
                            "sort_properties_default",
                            "sort_order_default"
                        )

                        json.shouldHaveAllStrings("sort_properties_available", available)
                        json.shouldHaveAllStrings("sort_properties_default", default)
                        json.shouldHave("sort_order_default", order.name.toLowerCase())
                        true
                    }
                }
            }
            context("search metadata") {
                it("handles null search metadata") {
                    val input = CollectionMetadata(
                        collectionSize = 1,
                        searchMetadata = null
                    )

                    val json = input.toJson()

                    json.shouldNotContainKeys("search_contexts_available")
                }
                it("serializes properly") {
                    forAll(50, searchContexts()) { contexts ->
                        val input = CollectionMetadata(
                            collectionSize = 0,
                            searchMetadata = SearchableCollectionMetadata(
                                contexts
                            )
                        )
                        val json = input.toJson()

                        json.shouldContainKeys("search_contexts_available")

                        json.shouldHaveObject("search_contexts_available") { obj ->
                            obj.shouldContainKeys(*contexts.keys.toTypedArray())
                            obj.mapValues { it.value.asJsonArray().getValuesAs(JsonString::getString) as Collection<String> }
                                .shouldContainExactly(contexts)
                        }

                        true
                    }
                }
            }
            context("subset metadata") {
                it("handles null subset metadata") {
                    val input = CollectionMetadata(
                        collectionSize = 1,
                        subsetMetadata = null
                    )

                    val json = input.toJson()

                    json.shouldNotContainKeys(
                        "subset_size",
                        "subset_start",
                        "default_subset_size",
                        "max_subset_size"
                    )
                }
                it("serializes properly") {
                    forAll(Gen.nats(), Gen.nats(), Gen.nats(), Gen.nats()) { size, start, def, max ->
                        val input = CollectionMetadata(
                            collectionSize = 1,
                            subsetMetadata = CollectionSubsetMetadata(
                                size, start, def, max
                            )
                        )

                        val json = input.toJson()

                        json.shouldContainKeys(
                            "subset_size",
                            "subset_start",
                            "default_subset_size",
                            "max_subset_size"
                        )

                        json.shouldHave("subset_size", size)
                        json.shouldHave("subset_start", start)
                        json.shouldHave("default_subset_size", def)
                        json.shouldHave("max_subset_size", max)
                        true
                    }
                }
            }
        }

        describe("SortOrder.toJson()") {
            it("should serialize properly") {
                forAll(Gen.enum<SortOrder>()) {
                    it.toJson() == it.name.toLowerCase()
                }
            }
        }
    }

    private inline fun <T : ResponseMetadata> DescribeScope.checkBasicResponseMetadata(
        crossinline build: (ValidationResponse, List<String>) -> T
    ) {
        context("ResponseMetadata Fields") {
            it("Generates JSON properly") {
                forAll(validationResponse(), Gen.list(Gen.string())) { response, info ->
                    val input: T = build(response, info)

                    val json = input.toJson()

                    json.mustHaveObject("validation_response") {
                        it.mustHave("code", response.code)
                            && it.mustHave("message", response.message)
                    } && json.mustHaveAllStrings("validation_information", info)
                }
            }
        }
    }

    private inline fun <T : ResponseMetadata> DescribeScope.checkBasicResponseMetadataWithCache(
        crossinline build: (ValidationResponse, List<String>, CacheMeta?) -> T
    ) {
        context("ResponseMetadata Fields") {
            it("Generates JSON properly") {
                forAll(validationResponse(), Gen.list(Gen.string()), cacheMeta()) { response, info, cacheMeta ->
                    val input: T = build(response, info, cacheMeta)

                    val json = input.toJson()

                    json.mustHaveObject("validation_response") {
                        it.mustHave("code", response.code)
                            && it.mustHave("message", response.message)
                    } && json.mustHaveAllStrings("validation_information", info)
                        && json.mustHaveObject("cache") {
                        it.mustHave("date_time", cacheMeta.dateTime.toString())
                    }
                }
            }
            it("handles null cache meta") {
                val input: T = build(ValidationResponse.OK, emptyList(), null)

                val json = input.toJson()

                json.shouldNotContainKey("cache")
            }
        }
    }
}

