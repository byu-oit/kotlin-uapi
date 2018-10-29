package edu.byu.uapi.server.spi.reflective

import edu.byu.uapi.server.inputs.DefaultTypeDictionary
import edu.byu.uapi.server.spi.asError
import edu.byu.uapi.spi.functional.onFailure
import edu.byu.uapi.spi.input.ListParams
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
    ) : ListParams.Filtering<TestFilters>,
        ListParams.Searching<TestSearchContext>,
        ListParams.Sorting<TestSortField>

    enum class TestSearchContext {
        context,
        another_context
    }

    enum class TestSortField {
        field,
        a_field,
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
