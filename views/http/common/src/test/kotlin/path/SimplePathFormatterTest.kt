package edu.byu.uapi.server.http.path

import edu.byu.uapi.server.http.errors.UAPIHttpInternalError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertFailsWith

internal class SimplePathFormatterTest {

    @Nested
    @DisplayName("formatVariable()")
    inner class FormatVariable {
        @Test
        fun `adds the prefix and suffix to a single variable`() {
            val formatter = SimplePathFormatter("prefix_", "_suffix")

            val part = variablePart("foo")

            val result = formatter.formatVariable(part)

            assertEquals("prefix_foo_suffix", result)
        }

        @Test
        fun `adds the prefix and suffix to each entry in a compound variable`() {
            val formatter = SimplePathFormatter("prefix_", "_suffix")

            val result = formatter.formatVariable(
                variablePart(listOf("foo"))
            )

            assertEquals("prefix_foo_suffix", result)
        }

        @Test
        fun `concatenates each entry in a compound variable with a ','`() {
            val formatter = SimplePathFormatter("_", "!")

            val result = formatter.formatVariable(
                variablePart("foo", "bar")
            )

            assertEquals("_foo!,_bar!", result)
        }

        @Test
        fun `overriding formatSingle() changes the behavior with single variables`() {
            val formatter = object : SimplePathFormatter("p", "s") {
                override fun formatSingle(name: String): String {
                    return "$prefix-%-$name-%-$suffix"
                }
            }

            val result = formatter.formatVariable(variablePart("foo"))

            assertEquals(
                "p-%-foo-%-s",
                result
            )
        }

        @Test
        fun `overriding formatSingle() changes the behavior with compound variables`() {
            val formatter = object : SimplePathFormatter("p", "s") {
                override fun formatSingle(name: String): String {
                    return "$prefix-%-$name-%-$suffix"
                }
            }

            val result = formatter.formatVariable(variablePart("foo", "bar"))

            assertEquals(
                "p-%-foo-%-s,p-%-bar-%-s",
                result
            )
        }

        @Test
        fun `overriding formatCompound() doesn't change behavior with single variables`() {
            val formatter = object : SimplePathFormatter("p_", "_s") {
                override fun formatCompound(names: List<String>): String {
                    return names.joinToString("^")
                }
            }

            val result = formatter.formatVariable(variablePart("foo"))

            assertEquals(
                "p_foo_s",
                result
            )
        }


        @Test
        fun `overriding formatCompound() changes behavior with compound variables`() {
            val formatter = object : SimplePathFormatter("p_", "_s") {
                override fun formatCompound(names: List<String>): String {
                    return names.joinToString("^", prefix = "start ", postfix = " end")
                }
            }

            val result = formatter.formatVariable(variablePart("foo", "bar"))

            assertEquals(
                "start foo^bar end",
                result
            )
        }
    }

    @Nested
    @DisplayName("extractVariableValues()")
    inner class ProcessVariableValues {

        @Test
        fun `strips the prefix and suffix from the name of a single variable`() {
            val formatter = SimplePathFormatter("prefix_", "_suffix")

            val part = variablePart("foo")

            val values = mapOf("prefix_foo_suffix" to "bar")

            val result = formatter.extractVariableValues(part, values)

            assertEquals(
                mapOf("foo" to "bar"),
                result
            )
        }

        @Test
        fun `leaves bare single variable name alone`() {
            val formatter = SimplePathFormatter("prefix_", "_suffix")

            val part = variablePart("foo")

            val values = mapOf("foo" to "bar")

            val result = formatter.extractVariableValues(part, values)

            assertEquals(
                mapOf("foo" to "bar"),
                result
            )
        }

        @Test
        fun `throws UAPIHttpInternalError if simple variable value is missing`() {
            val formatter = SimplePathFormatter("prefix_", "_suffix")
            val part = variablePart("foo")

            assertFailsWith<UAPIHttpInternalError> {
                formatter.extractVariableValues(part, emptyMap())
            }
        }

        @Test
        fun `removes the prefix and suffix from the name of each entry of a compound variable`() {
            val formatter = SimplePathFormatter("prefix_", "_suffix")

            val values = mapOf("prefix_foo_suffix" to "bar")

            val result = formatter.extractVariableValues(
                variablePart(listOf("foo")),
                values
            )

            assertEquals(
                mapOf("foo" to "bar"),
                result
            )
        }


        @Test
        fun `throws UAPIHttpInternalError if a compound variable value is missing`() {
            val formatter = SimplePathFormatter("prefix_", "_suffix")
            val part = variablePart("foo", "bar")

            assertFailsWith<UAPIHttpInternalError> {
                formatter.extractVariableValues(part, mapOf("foo" to "baz"))
            }
        }

        @Test
        fun `overriding formatSingle() changes the lookup behavior with single variables`() {
            val formatter = object : SimplePathFormatter("p", "s") {
                override fun formatSingle(name: String): String {
                    return "$prefix-%-$name-%-$suffix"
                }
            }

            val values = mapOf(
                "pfoos" to "wrong",
                "p-%-foo-%-s" to "right"
            )

            val result = formatter.extractVariableValues(
                variablePart("foo"),
                values
            )

            assertEquals(
                mapOf("foo" to "right"),
                result
            )
        }

        @Test
        fun `overriding formatSingle() changes the lookup behavior with compound variables`() {
            val formatter = object : SimplePathFormatter("p", "s") {
                override fun formatSingle(name: String): String {
                    return "$prefix-%-$name-%-$suffix"
                }
            }

            val values = mapOf(
                "pfoos" to "wrong-foo",
                "pbars" to "wrong-bar",
                "p-%-foo-%-s" to "right-foo",
                "p-%-bar-%-s" to "right-bar"
            )

            val result = formatter.extractVariableValues(
                variablePart("foo", "bar"),
                values
            )

            assertEquals(
                mapOf("foo" to "right-foo", "bar" to "right-bar"),
                result
            )
        }

        @Test
        fun `overriding unformatSingle() changes the lookup behavior with single variables`() {
            val formatter = object : SimplePathFormatter("p", "s") {
                override fun extractSingleVariableValues(name: String, values: Map<String, String>): String {
                    return values.getValue("p-%-$name-%-s") + "_found"
                }
            }

            val values = mapOf(
                "p-%-foo-%-s" to "right"
            )

            val result = formatter.extractVariableValues(
                variablePart("foo"),
                values
            )

            assertEquals(
                mapOf("foo" to "right_found"),
                result
            )
        }

        @Test
        fun `overriding unformatSingle() changes the lookup behavior with compound variables`() {
            val formatter = object : SimplePathFormatter("p", "s") {
                override fun extractSingleVariableValues(name: String, values: Map<String, String>): String {
                    return values.getValue("p-%-$name-%-s") + "_found"
                }
            }

            val values = mapOf(
                "pfoos" to "wrong-foo",
                "pbars" to "wrong-bar",
                "p-%-foo-%-s" to "right-foo",
                "p-%-bar-%-s" to "right-bar"
            )

            val result = formatter.extractVariableValues(
                variablePart("foo", "bar"),
                values
            )

            assertEquals(
                mapOf("foo" to "right-foo_found", "bar" to "right-bar_found"),
                result
            )
        }

        @Test
        fun `overriding formatCompound() doesn't change lookup behavior with single variables`() {
            val formatter = object : SimplePathFormatter("p_", "_s") {
                override fun formatCompound(names: List<String>): String {
                    return names.joinToString("^")
                }
            }

            val values = mapOf("p_foo_s" to "right")

            val result = formatter.extractVariableValues(variablePart("foo"), values)

            assertEquals(
                mapOf("foo" to "right"),
                result
            )
        }


        @Test
        fun `overriding formatCompound() doesn't directly change behavior with compound variables`() {
            val formatter = object : SimplePathFormatter("p_", "_s") {
                override fun formatCompound(names: List<String>): String {
                    return names.joinToString("^", prefix = "start ", postfix = " end")
                }
            }
            val values = mapOf(
                "p_foo_s" to "right-foo",
                "p_bar_s" to "right-bar"
            )

            val result = formatter.extractVariableValues(variablePart("foo", "bar"), values)

            assertEquals(
                mapOf("foo" to "right-foo", "bar" to "right-bar"),
                result
            )
        }

        @Test
        fun `overriding unformatCompound() doesn't change behavior with single variables`() {
            val formatter = object : SimplePathFormatter("p_", "_s") {
                override fun extractCompoundVariableValues(
                    names: List<String>,
                    values: Map<String, String>
                ): Map<String, String> {
                    return super.extractCompoundVariableValues(names, values).mapValues { it.value + "_modified" }
                }
            }

            val values = mapOf("p_foo_s" to "right")

            val result = formatter.extractVariableValues(variablePart("foo"), values)

            assertEquals(
                mapOf("foo" to "right"),
                result
            )
        }

        @Test
        fun `overriding unformatCompound() changes the behavior with compound variables`() {
            val formatter = object : SimplePathFormatter("p_", "_s") {
                override fun extractCompoundVariableValues(
                    names: List<String>,
                    values: Map<String, String>
                ): Map<String, String> {
                    return names.associateWith {
                        values.getValue(it + "_key") + "_found"
                    }
                }
            }

            val values = mapOf(
                "foo_key" to "right-foo",
                "bar_key" to "right-bar"
            )

            val result = formatter.extractVariableValues(variablePart("foo", "bar"), values)

            assertEquals(
                mapOf("foo" to "right-foo_found", "bar" to "right-bar_found"),
                result
            )
        }
    }

    @Nested
    @DisplayName("unformatPart")
    inner class Unformat {
        private val formatter = SimplePathFormatter("{", "}")

        @Test
        fun `maps static parts`() {
            val result = formatter.unformatPart("static")

            assertEquals(StaticPathPart("static"), result)
        }

        @Test
        fun `maps simple variable parts`() {
            val result = formatter.unformatPart("{varname}")

            assertEquals(SingleVariablePathPart("varname"), result)
        }

        @Test
        fun `maps compound variable parts`() {
            val result = formatter.unformatPart("{foo},{bar}")

            assertEquals(CompoundVariablePathPart(listOf("foo", "bar")), result)
        }

        @Test
        fun `maps more than two variable parts`() {
            val result = formatter.unformatPart("{foo},{bar},{baz},{oof},{rab},{zab}")

            assertEquals(
                CompoundVariablePathPart(
                    listOf(
                        "foo", "bar", "baz", "oof", "rab", "zab"
                    )
                ), result
            )
        }

        @ParameterizedTest
        @ValueSource(strings = [
            "{}",
            "{},{}",
            "{foo}bar",
            "foo{bar}",
            "foo{bar}baz",
            "foo,bar",
            "foo{bar},{baz}",
            "{foo},{bar}baz",
            "foo,{bar},{baz}",
            "{foo},{bar},baz"
        ])
        fun `doesn't parse garbage as variables`(value: String) {
            val result = formatter.unformatPart(value)
            assertEquals(
                StaticPathPart(value),
                result
            )
        }
    }
}
