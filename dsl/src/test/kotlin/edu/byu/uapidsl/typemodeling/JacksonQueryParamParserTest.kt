package edu.byu.uapidsl.typemodeling

import edu.byu.uapidsl.dsl.PagingParams
import org.junit.Test
import kotlin.test.assertEquals


class JacksonQueryParamParserTest {

    @Test
    fun parsePageParams() {
        val modeler = DefaultTypeModeler()
        val parser = modeler.queryParamReaderFor(PagingParams::class)

        val result = parser.read(mapOf(
            "page_start" to setOf("1"),
            "page_size" to setOf("20")
        ))

        assertEquals(1, result.pageStart)
    }

}
