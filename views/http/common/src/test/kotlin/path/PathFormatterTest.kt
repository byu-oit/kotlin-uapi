package edu.byu.uapi.server.http.path

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PathFormatterTest {

    @Test
    fun `PathFormatter#format(parts) joins the parts together`() {
        val formatter = object : PathFormatter {
            override fun formatVariable(part: VariablePathPart): String {
                return when (part) {
                    is SingleVariablePathPart   -> "var_" + part.name
                    is CompoundVariablePathPart -> part.names.joinToString(",") { "var_$it" }
                }
            }

            override fun extractVariableValues(
                part: VariablePathPart,
                values: Map<String, String>
            ): Map<String, String> {
                TODO("not implemented")
            }

            override fun unformat(part: String): PathPart {
                TODO("not implemented")
            }
        }

        val result = formatter.format(allPathPartTypes)
        assertEquals(
            "/foo/bar/var_baz/var_i,var_j,var_k",
            result
        )
    }

    private val allPathPartTypes = listOf(
        StaticPathPart("foo"),
        StaticPathPart("bar"),
        SingleVariablePathPart("baz"),
        CompoundVariablePathPart(listOf("i", "j", "k"))
    )

    @Test
    fun `PathFormatters#COLON prefixes variables with a colon`() {
        val result = PathFormatters.COLON.format(allPathPartTypes)
        assertEquals(
            "/foo/bar/:baz/:i,:j,:k",
            result
        )
    }

    @Test
    fun `PathFormatters#FLAT_COLON prefixes variables with a colon`() {
        val result = PathFormatters.FLAT_COLON.format(allPathPartTypes)
        assertEquals(
            "/foo/bar/:baz/:compound__i__j__k",
            result
        )
    }

    @Test
    fun `PathFormatters#CURLY_BRACE wraps variables with curly braces`() {
        val result = PathFormatters.CURLY_BRACE.format(allPathPartTypes)
        assertEquals(
            "/foo/bar/{baz}/{i},{j},{k}",
            result
        )
    }


    @Test
    fun `PathFormatters#FLAT_CURLY_BRACE flattens compound parts`() {
        val result = PathFormatters.FLAT_CURLY_BRACE.format(allPathPartTypes)
        assertEquals(
            "/foo/bar/{baz}/{compound__i__j__k}",
            result
        )
    }

}
