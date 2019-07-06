package edu.byu.uapi.server.http.path

import edu.byu.uapi.server.http.errors.UAPIHttpMissingCompoundPathParamError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

internal class CompoundFlatteningFormatterTest {

    @Nested
    inner class Formatting {
        @Test
        fun `flattens compound variables into a single path variable`() {
            val formatter = CompoundFlatteningFormatter(
                "p_", "_s", "c__", "__"
            )

            val part = variablePart("foo", "bar")

            val result = formatter.formatVariable(part)

            assertEquals("p_c__foo__bar_s", result)
        }

        @Test
        fun `doesn't change how single variables are formatted`() {
            val simple = SimplePathFormatter("p_", "_s")
            val flat = CompoundFlatteningFormatter("p_", "_s")

            val part = variablePart("foo")

            val expected = simple.formatVariable(part)
            val actual = flat.formatVariable(part)

            assertEquals(expected, actual)
        }
    }

    @Nested
    inner class Unformatting {
        @Test
        fun `inflates both keys and values of compound variables`() {
            val formatter = CompoundFlatteningFormatter(
                "p_", "_s", "c__", "__"
            )

            val part = variablePart("foo", "bar")
            val values = mapOf(
                "p_c__foo__bar_s" to "fooval,barval"
            )

            val result = formatter.unformatVariableValues(part, values)

            assertEquals(
                mapOf("foo" to "fooval", "bar" to "barval"),
                result
            )
        }

        @Test
        fun `throws a UAPIHttpMissingCompoundPathParamError if a value is missing`() {
            val formatter = CompoundFlatteningFormatter(
                "p_", "_s", "c__", "__"
            )

            val part = variablePart("foo", "bar")
            val values = mapOf(
                "p_c__foo__bar_s" to "fooval"
            )

            assertFailsWith<UAPIHttpMissingCompoundPathParamError> { formatter.unformatVariableValues(part, values) }
        }

        @Test
        fun `can handle 'bare' variable names`() {
            val formatter = CompoundFlatteningFormatter(
                "p_", "_s", "c__", "__"
            )

            val part = variablePart("foo", "bar")
            val values = mapOf(
                "c__foo__bar" to "fooval,barval"
            )

            val result = formatter.unformatVariableValues(part, values)

            assertEquals(
                mapOf("foo" to "fooval", "bar" to "barval"),
                result
            )
        }

        @Test
        fun `unformats single variables the same`() {
            val simple = SimplePathFormatter("p_", "_s")
            val flat = CompoundFlatteningFormatter(
                "p_", "_s", "c__", "__"
            )

            val part = variablePart("foo")
            val values = mapOf(
                "p_foo_s" to "foo-right",
                "p_c__foo__bar_s" to "foo-wrong,barval"
            )

            val expected = simple.unformatVariableValues(part, values)
            val actual = flat.unformatVariableValues(part, values)

            assertEquals(
                expected,
                actual
            )
        }
    }
}
