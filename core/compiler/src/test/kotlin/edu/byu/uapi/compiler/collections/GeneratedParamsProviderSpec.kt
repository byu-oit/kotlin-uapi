package edu.byu.uapi.compiler.collections

import io.kotlintest.specs.DescribeSpec

class GeneratedParamsProviderSpec: DescribeSpec() {

    init {
        describe("GeneratedParamsProvider") {
            it("does stuff") {
                val model = ParamsModel(
                    "AllParams",
                    "params",
                    null,
                    SortingModel(
                        listOf(
                            SortFieldModel("field"),
                            SortFieldModel("a_field"),
                            SortFieldModel("another_default_field")
                        ),
                        listOf("field", "another_default_field")
                    ),
                    null
//                    SearchingModel(
//                        mapOf(
//                            "context" to listOf("field", "another_field"),
//                            "another_context" to listOf("a_field")
//                        )
//                    )
                )

                GeneratedParamsProvider(model).generate().writeTo(System.out)
            }
        }
    }

}
