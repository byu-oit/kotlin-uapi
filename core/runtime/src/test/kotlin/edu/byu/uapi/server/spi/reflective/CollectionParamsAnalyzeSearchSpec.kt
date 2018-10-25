package edu.byu.uapi.server.spi.reflective

import edu.byu.uapi.server.inputs.DefaultTypeDictionary
import edu.byu.uapi.server.spi.asError
import edu.byu.uapi.spi.annotations.DefaultSort
import edu.byu.uapi.spi.annotations.SearchFields
import edu.byu.uapi.spi.functional.onFailure
import edu.byu.uapi.spi.input.Params
import edu.byu.uapi.spi.input.SearchParams
import edu.byu.uapi.spi.input.SortParams
import io.kotlintest.specs.DescribeSpec

class CollectionParamsAnalyzeSearchSpec : DescribeSpec() {
    init {
        describe("analyzeSearch") {
            it("does stuff") {
                println("warmup")
                speed(10_000)
                println("actual 1")
                speed(10_000)
            }
        }
    }

    fun speed(iterations: Int) {
        val list = ArrayList<AnalyzedParams>(iterations)
        val dict = DefaultTypeDictionary()
        val start = System.currentTimeMillis()
        for (i in 1..iterations) {
            list.add(analyzeAndValidate(AllParams::class, dict).onFailure { throw it.asError() })
        }
        val end = System.currentTimeMillis()
        val elapsed = end - start
        val avg = elapsed.toDouble() / iterations
        println("took $elapsed ms (avg $avg ms)")
    }

    data class AllParams(
        override val filter: TestFilters?,
        override val sort: SortParams<TestSortField>,
        override val search: SearchParams<TestSearchContext>?
    ) : Params.Filtering<TestFilters>,
        Params.Searching<TestSearchContext>,
        Params.Sorting<TestSortField>

    enum class TestSearchContext {
        @SearchFields("field", "another_field")
        context,
        @SearchFields("a_field")
        another_context
    }

    enum class TestSortField {
        @DefaultSort(order = 1)
        field,
        a_field,
        @DefaultSort(order = 2)
        another_default_field
    }

    data class TestFilters(
        val aString: String?,
        val anInt: Int?,
//        val nested: NestedFilters?,
        val multiple: Collection<Int>
    )

    data class NestedFilters(val nestedString: String?)

}
