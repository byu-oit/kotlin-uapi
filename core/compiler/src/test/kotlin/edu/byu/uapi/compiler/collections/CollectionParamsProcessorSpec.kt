package edu.byu.uapi.compiler.collections

import com.google.testing.compile.CompilationSubject.assertThat
import com.google.testing.compile.Compiler.javac
import com.google.testing.compile.JavaFileObjects
import io.kotlintest.specs.DescribeSpec

class CollectionParamsProcessorSpec : DescribeSpec() {
    init {
        describe("CollectionParamsProcessor") {
            val proc = CollectionParamsProcessor()

            it("Does stuff") {
//                val compilation = javac()
//                    .withClasspathFrom(javaClass.classLoader)
//                    .withProcessors(proc)
//                    .compile(
//                        JavaFileObjects.forResource("params/AllParams.java"),
//                        JavaFileObjects.forResource("params/TestFilters.java"),
//                        JavaFileObjects.forResource("params/TestSearchContext.java"),
//                        JavaFileObjects.forResource("params/TestSortField.java")
//                    )
//                assertThat(compilation)
//                    .succeededWithoutWarnings()
//                assertThat(compilation)
//                    .generatedSourceFile("params/AllParams\$ListParamReader")
//
//                assertThat(
//                ).withClasspathFrom(CollectionParamsProcessorSpec::class.java.classLoader)
//                    .processedWith(proc)
//                    .compilesWithoutError()
////                    .and().generatesFiles(
////                        J
////                    )
            }
        }
    }
}
