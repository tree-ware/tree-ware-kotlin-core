package org.treeWare.json

import okio.Buffer
import org.treeWare.util.TokenException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class TokenizeJsonTests {
    private fun doubleQuoted(value: String): String = "\"$value\""

    // region String values

    @Test
    fun must_tokenize_empty_string_value() {
        val buffer = Buffer().writeUtf8(doubleQuoted(""))
        val expected = listOf(JsonToken.ValueString(""))
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_string_value() {
        val buffer = Buffer().writeUtf8(doubleQuoted("hello world"))
        val expected = listOf(JsonToken.ValueString("hello world"))
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_string_value_with_escaped_double_quote() {
        val buffer = Buffer().writeUtf8(doubleQuoted("\\\""))
        val expected = listOf(JsonToken.ValueString("\""))
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_string_value_with_escaped_back_slash() {
        val buffer = Buffer().writeUtf8(doubleQuoted("\\\\"))
        val expected = listOf(JsonToken.ValueString("\\"))
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_string_value_with_escaped_forward_slash() {
        val buffer = Buffer().writeUtf8(doubleQuoted("\\/"))
        val expected = listOf(JsonToken.ValueString("/"))
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_string_value_with_escaped_backspace() {
        val buffer = Buffer().writeUtf8(doubleQuoted("\\b"))
        val expected = listOf(JsonToken.ValueString("\b"))
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_string_value_with_escaped_form_feed() {
        val buffer = Buffer().writeUtf8(doubleQuoted("\\f"))
        val expected = listOf(JsonToken.ValueString("\u000c"))
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_string_value_with_escaped_line_feed() {
        val buffer = Buffer().writeUtf8(doubleQuoted("\\n"))
        val expected = listOf(JsonToken.ValueString("\n"))
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_string_value_with_escaped_carriage_return() {
        val buffer = Buffer().writeUtf8(doubleQuoted("\\r"))
        val expected = listOf(JsonToken.ValueString("\r"))
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_string_value_with_escaped_horizontal_tab() {
        val buffer = Buffer().writeUtf8(doubleQuoted("\\t"))
        val expected = listOf(JsonToken.ValueString("\t"))
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_string_value_with_escaped_unicode() {
        val buffer = Buffer().writeUtf8(doubleQuoted("\\uC4f3 \\u0030"))
        val expected = listOf(JsonToken.ValueString("\uc4f3 \u0030"))
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_string_value_with_escaped_and_non_escaped() {
        val buffer = Buffer().writeUtf8(doubleQuoted("\\\" \\\\ \\/ \\thello\\nworld\\f\\r\\uC4f3 \\b\\u0030"))
        val expected = listOf(JsonToken.ValueString("\" \\ / \thello\nworld\u000c\r\uC4f3 \b\u0030"))
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    // endregion

    // region Positive number values

    @Test
    fun must_tokenize_integer_value() {
        val buffer = Buffer().writeUtf8("\t123  \n")
        val expected = listOf(JsonToken.ValueNumber("123"))
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_decimal_value() {
        val buffer = Buffer().writeUtf8("\t123.45  \n")
        val expected = listOf(JsonToken.ValueNumber("123.45"))
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_exponent_value() {
        val buffer = Buffer().writeUtf8("\t123.45e6  \n")
        val expected = listOf(JsonToken.ValueNumber("123.45e6"))
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_exponent_value_with_positive_exponent() {
        val buffer = Buffer().writeUtf8("\t123.45E+6  \n")
        val expected = listOf(JsonToken.ValueNumber("123.45E+6"))
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_exponent_value_with_negative_exponent() {
        val buffer = Buffer().writeUtf8("\t123.45E-6  \n")
        val expected = listOf(JsonToken.ValueNumber("123.45E-6"))
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    // endregion

    // region Negative number values

    @Test
    fun must_tokenize_negative_integer_value() {
        val buffer = Buffer().writeUtf8("\t-123  \n")
        val expected = listOf(JsonToken.ValueNumber("-123"))
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_negative_decimal_value() {
        val buffer = Buffer().writeUtf8("\t-123.45  \n")
        val expected = listOf(JsonToken.ValueNumber("-123.45"))
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_negative_exponent_value() {
        val buffer = Buffer().writeUtf8("\t-123.45e6  \n")
        val expected = listOf(JsonToken.ValueNumber("-123.45e6"))
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_negative_exponent_value_with_positive_exponent() {
        val buffer = Buffer().writeUtf8("\t-123.45E+6  \n")
        val expected = listOf(JsonToken.ValueNumber("-123.45E+6"))
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_negative_exponent_value_with_negative_exponent() {
        val buffer = Buffer().writeUtf8("\t-123.45E-6  \n")
        val expected = listOf(JsonToken.ValueNumber("-123.45E-6"))
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    // endregion

    // region Boolean values

    @Test
    fun must_tokenize_true_value() {
        val buffer = Buffer().writeUtf8("\ttrue  \n")
        val expected = listOf(JsonToken.ValueTrue)
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_false_value() {
        val buffer = Buffer().writeUtf8("\tfalse  \n")
        val expected = listOf(JsonToken.ValueFalse)
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    // endregion

    // region Null values

    @Test
    fun must_tokenize_null_value() {
        val buffer = Buffer().writeUtf8("\tnull  \n")
        val expected = listOf(JsonToken.ValueNull)
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    // endregion

    // region Objects

    @Test
    fun must_tokenize_empty_object() {
        val buffer = Buffer().writeUtf8("\t{ \t\n } \n")
        val expected = listOf(JsonToken.ObjectStart, JsonToken.ObjectEnd)
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_object_with_true_value() {
        val buffer = Buffer().writeUtf8("\t{ \"key\": \ttrue \t\n } \n")
        val expected = listOf(
            JsonToken.ObjectStart,
            JsonToken.KeyName("key"),
            JsonToken.ValueTrue,
            JsonToken.ObjectEnd
        )
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_object_with_false_value() {
        val buffer = Buffer().writeUtf8("\t{ \"key\"\t:false \t\n } \n")
        val expected = listOf(
            JsonToken.ObjectStart,
            JsonToken.KeyName("key"),
            JsonToken.ValueFalse,
            JsonToken.ObjectEnd
        )
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_object_with_null_value() {
        val buffer = Buffer().writeUtf8("\t{ \"key\"\t: null \t\n } \n")
        val expected = listOf(
            JsonToken.ObjectStart,
            JsonToken.KeyName("key"),
            JsonToken.ValueNull,
            JsonToken.ObjectEnd
        )
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_object_with_string_value() {
        val buffer = Buffer().writeUtf8("\t{ \"key\"\t: \"value\" \t\n } \n")
        val expected = listOf(
            JsonToken.ObjectStart,
            JsonToken.KeyName("key"),
            JsonToken.ValueString("value"),
            JsonToken.ObjectEnd
        )
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_object_with_number_value() {
        val buffer = Buffer().writeUtf8("\t{ \"key\"\t: -123.45e+6 \t\n } \n")
        val expected = listOf(
            JsonToken.ObjectStart,
            JsonToken.KeyName("key"),
            JsonToken.ValueNumber("-123.45e+6"),
            JsonToken.ObjectEnd
        )
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_object_with_object_value() {
        val buffer = Buffer().writeUtf8("\t{\"outerKey\": { \"innerKey\"\t: -123.45e+6 \t\n }} \n")
        val expected = listOf(
            JsonToken.ObjectStart,
            JsonToken.KeyName("outerKey"),
            JsonToken.ObjectStart,
            JsonToken.KeyName("innerKey"),
            JsonToken.ValueNumber("-123.45e+6"),
            JsonToken.ObjectEnd,
            JsonToken.ObjectEnd
        )
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_object_with_array_value() {
        val buffer = Buffer().writeUtf8("\t{ \"key\"\t: [-123.45e+6] \t\n } \n")
        val expected = listOf(
            JsonToken.ObjectStart,
            JsonToken.KeyName("key"),
            JsonToken.ArrayStart,
            JsonToken.ValueNumber("-123.45e+6"),
            JsonToken.ArrayEnd,
            JsonToken.ObjectEnd
        )
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_token_object_with_mixed_values() {
        val buffer = Buffer().writeUtf8(
            """
            {
                "true-field": true,
                "false_field": false,
                "nullField": null,
                "StringField": "hello",
                "NUMBER_FIELD": -123.45e+6,
                "empty object": {},
                "non-empty object": {"key": "value"},
                "empty array": [],
                "non-empty array": [true, false]
            }
            """
        )
        val expected = listOf(
            JsonToken.ObjectStart,
            JsonToken.KeyName("true-field"),
            JsonToken.ValueTrue,
            JsonToken.KeyName("false_field"),
            JsonToken.ValueFalse,
            JsonToken.KeyName("nullField"),
            JsonToken.ValueNull,
            JsonToken.KeyName("StringField"),
            JsonToken.ValueString("hello"),
            JsonToken.KeyName("NUMBER_FIELD"),
            JsonToken.ValueNumber("-123.45e+6"),
            JsonToken.KeyName("empty object"),
            JsonToken.ObjectStart,
            JsonToken.ObjectEnd,
            JsonToken.KeyName("non-empty object"),
            JsonToken.ObjectStart,
            JsonToken.KeyName("key"),
            JsonToken.ValueString("value"),
            JsonToken.ObjectEnd,
            JsonToken.KeyName("empty array"),
            JsonToken.ArrayStart,
            JsonToken.ArrayEnd,
            JsonToken.KeyName("non-empty array"),
            JsonToken.ArrayStart,
            JsonToken.ValueTrue,
            JsonToken.ValueFalse,
            JsonToken.ArrayEnd,
            JsonToken.ObjectEnd
        )
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    // endregion

    // region Arrays

    @Test
    fun must_tokenize_empty_array() {
        val buffer = Buffer().writeUtf8("\t[ \t\n ] \n")
        val expected = listOf(JsonToken.ArrayStart, JsonToken.ArrayEnd)
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_array_with_one_true_value() {
        val buffer = Buffer().writeUtf8("\t[ \ttrue \t\n ] \n")
        val expected = listOf(JsonToken.ArrayStart, JsonToken.ValueTrue, JsonToken.ArrayEnd)
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_array_with_many_true_values() {
        val buffer = Buffer().writeUtf8("\t[ \ttrue, true,true \t\n ] \n")
        val expected = listOf(
            JsonToken.ArrayStart,
            JsonToken.ValueTrue,
            JsonToken.ValueTrue,
            JsonToken.ValueTrue,
            JsonToken.ArrayEnd
        )
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_array_with_one_false_value() {
        val buffer = Buffer().writeUtf8("\t[ \tfalse \t\n ] \n")
        val expected = listOf(JsonToken.ArrayStart, JsonToken.ValueFalse, JsonToken.ArrayEnd)
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_array_with_many_false_values() {
        val buffer = Buffer().writeUtf8("\t[ \tfalse, false,false \t\n ] \n")
        val expected = listOf(
            JsonToken.ArrayStart,
            JsonToken.ValueFalse,
            JsonToken.ValueFalse,
            JsonToken.ValueFalse,
            JsonToken.ArrayEnd
        )
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_array_with_one_null_value() {
        val buffer = Buffer().writeUtf8("\t[ \tnull \t\n ] \n")
        val expected = listOf(JsonToken.ArrayStart, JsonToken.ValueNull, JsonToken.ArrayEnd)
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_array_with_many_null_values() {
        val buffer = Buffer().writeUtf8("\t[ \tnull, null,null \t\n ] \n")
        val expected = listOf(
            JsonToken.ArrayStart,
            JsonToken.ValueNull,
            JsonToken.ValueNull,
            JsonToken.ValueNull,
            JsonToken.ArrayEnd
        )
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_array_with_one_string_value() {
        val buffer = Buffer().writeUtf8("\t[ \t\"hello!\" \t\n ] \n")
        val expected = listOf(JsonToken.ArrayStart, JsonToken.ValueString("hello!"), JsonToken.ArrayEnd)
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_array_with_many_string_values() {
        val buffer = Buffer().writeUtf8("\t[ \t\"how\", \"are\",\"you?\" \t\n ] \n")
        val expected = listOf(
            JsonToken.ArrayStart,
            JsonToken.ValueString("how"),
            JsonToken.ValueString("are"),
            JsonToken.ValueString("you?"),
            JsonToken.ArrayEnd
        )
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_array_with_one_number_value() {
        val buffer = Buffer().writeUtf8("\t[ \t-123.45e+6 \t\n ] \n")
        val expected = listOf(JsonToken.ArrayStart, JsonToken.ValueNumber("-123.45e+6"), JsonToken.ArrayEnd)
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_array_with_many_number_values() {
        val buffer = Buffer().writeUtf8("\t[ \t-123.45, 123,-123.45e+6 \t\n ] \n")
        val expected = listOf(
            JsonToken.ArrayStart,
            JsonToken.ValueNumber("-123.45"),
            JsonToken.ValueNumber("123"),
            JsonToken.ValueNumber("-123.45e+6"),
            JsonToken.ArrayEnd
        )
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_array_with_one_object() {
        val buffer = Buffer().writeUtf8("\t[ \t{\"key\": \"value\"} \t\n ] \n")
        val expected = listOf(
            JsonToken.ArrayStart,
            JsonToken.ObjectStart,
            JsonToken.KeyName("key"),
            JsonToken.ValueString("value"),
            JsonToken.ObjectEnd,
            JsonToken.ArrayEnd
        )
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_array_with_many_objects() {
        val buffer = Buffer().writeUtf8(
            """
            [
                {"key1": "value1"},
                {"key2": "value2"},
                {"key3": "value3"}
            ]
            """.trimIndent()
        )
        val expected = listOf(
            JsonToken.ArrayStart,
            JsonToken.ObjectStart,
            JsonToken.KeyName("key1"),
            JsonToken.ValueString("value1"),
            JsonToken.ObjectEnd,
            JsonToken.ObjectStart,
            JsonToken.KeyName("key2"),
            JsonToken.ValueString("value2"),
            JsonToken.ObjectEnd,
            JsonToken.ObjectStart,
            JsonToken.KeyName("key3"),
            JsonToken.ValueString("value3"),
            JsonToken.ObjectEnd,
            JsonToken.ArrayEnd
        )
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_array_with_one_array() {
        val buffer = Buffer().writeUtf8("\t[ \t[\"hello\", \"world\"] \t\n ] \n")
        val expected = listOf(
            JsonToken.ArrayStart,
            JsonToken.ArrayStart,
            JsonToken.ValueString("hello"),
            JsonToken.ValueString("world"),
            JsonToken.ArrayEnd,
            JsonToken.ArrayEnd
        )
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_array_with_many_arrays() {
        val buffer = Buffer().writeUtf8(
            """
            [
                [true, false],
                ["hello", "world"],
                [123, null]
            ]
            """.trimIndent()
        )
        val expected = listOf(
            JsonToken.ArrayStart,
            JsonToken.ArrayStart,
            JsonToken.ValueTrue,
            JsonToken.ValueFalse,
            JsonToken.ArrayEnd,
            JsonToken.ArrayStart,
            JsonToken.ValueString("hello"),
            JsonToken.ValueString("world"),
            JsonToken.ArrayEnd,
            JsonToken.ArrayStart,
            JsonToken.ValueNumber("123"),
            JsonToken.ValueNull,
            JsonToken.ArrayEnd,
            JsonToken.ArrayEnd
        )
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    @Test
    fun must_tokenize_array_with_mixed_values() {
        val buffer = Buffer().writeUtf8(
            """
            [
                true, false, null, "hello", 123,
                {"key": "value"},
                [1, 2, 3]
            ]
            """.trimIndent()
        )
        val expected = listOf(
            JsonToken.ArrayStart,
            JsonToken.ValueTrue,
            JsonToken.ValueFalse,
            JsonToken.ValueNull,
            JsonToken.ValueString("hello"),
            JsonToken.ValueNumber("123"),
            JsonToken.ObjectStart,
            JsonToken.KeyName("key"),
            JsonToken.ValueString("value"),
            JsonToken.ObjectEnd,
            JsonToken.ArrayStart,
            JsonToken.ValueNumber("1"),
            JsonToken.ValueNumber("2"),
            JsonToken.ValueNumber("3"),
            JsonToken.ArrayEnd,
            JsonToken.ArrayEnd
        )
        val actual = tokenizeJson(buffer).toList()
        assertEquals(expected, actual)
    }

    // endregion

    // region Errors

    @Test
    fun must_fail_if_string_value_does_not_end_with_double_quotes() {
        testError("\"hello", "Incomplete JSON", 1, 6, 6)
        testError("{\n\t\"a\": \"hello}", "Incomplete JSON", 2, 16, 15)
        testError("[\r\n\t\"hello]", "Incomplete JSON", 2, 11, 11)
    }

    @Test
    fun must_fail_for_invalid_hex_escape() {
        testError("\"\\uC4f\"", "Expected hex digit but found '\"'", 1, 7, 7)
    }

    @Test
    fun must_fail_for_invalid_single_digit_integer() {
        testError("-a", "Expected digit", 1, 2, 2)
    }

    @Test
    fun must_fail_for_invalid_multi_digit_integer() {
        testError("1a", "Unexpected character", 1, 2, 2)
    }

    @Test
    fun must_fail_for_invalid_single_digit_fraction() {
        testError("-12.a", "Expected digit", 1, 5, 5)
    }

    @Test
    fun must_fail_for_invalid_multi_digit_fraction() {
        testError("-12.3a", "Unexpected character", 1, 6, 6)
    }

    @Test
    fun must_fail_for_invalid_single_digit_exponent() {
        testError("-12.34Ea", "Expected digit", 1, 8, 8)
    }

    @Test
    fun must_fail_for_invalid_multi_digit_exponent() {
        testError("-12.34E5a", "Unexpected character", 1, 9, 9)
    }

    @Test
    fun must_fail_for_invalid_true() {
        testError("[ tRUE ]", "Expected 'true'", 1, 4, 4)
    }

    @Test
    fun must_fail_for_invalid_false() {
        testError("[\nfaLSE\n]", "Expected 'false'", 2, 3, 5)
    }

    @Test
    fun must_fail_for_invalid_null() {
        testError("nULL", "Expected 'null'", 1, 2, 2)
    }

    @Test
    fun must_fail_for_invalid_value() {
        testError("hello", "Unknown value type", 1, 1, 1)
    }

    @Test
    fun must_fail_if_starts_with_object_end() {
        testError("}", "Unexpected '}'", 1, 1, 1)
    }

    @Test
    fun must_fail_if_starts_with_array_end() {
        testError("]", "Unexpected ']'", 1, 1, 1)
    }

    @Test
    fun must_fail_if_starts_with_colon() {
        testError(":", "Unknown value type", 1, 1, 1)
    }

    @Test
    fun must_fail_if_starts_with_comma() {
        testError(",", "Unknown value type", 1, 1, 1)
    }

    @Test
    fun must_fail_for_invalid_value_in_an_object() {
        testError("{\"greeting\": hello}", "Unknown value type", 1, 14, 14)
    }

    @Test
    fun must_fail_for_invalid_value_in_an_array() {
        testError("[hello]", "Unknown value type", 1, 2, 2)
    }

    @Test
    fun must_fail_if_field_name_does_not_start_with_double_quotes() {
        testError("{a: 1}", "Expected '\"'", 1, 2, 2)
    }

    @Test
    fun must_fail_if_field_name_does_not_end_with_double_quotes() {
        testError("{\"a: 1}", "Incomplete JSON", 1, 7, 7)
    }

    @Test
    fun must_fail_if_colon_is_missing_after_field_name() {
        testError("{\"a\" 1}", "Expected ':'", 1, 6, 6)
    }

    @Test
    fun must_fail_for_trailing_comma_in_object() {
        testError("{\"a\": 1,}", "Expected '\"'", 1, 9, 9)
    }

    @Test
    fun must_fail_if_object_is_not_closed() {
        testError("{\"a\": 1", "Incomplete JSON", 1, 7, 7)
    }

    @Test
    fun must_fail_if_object_close_is_not_balanced() {
        testError("[}", "Unexpected '}'", 1, 2, 2)
    }

    @Test
    fun must_fail_for_trailing_comma_in_array() {
        testError("[1,]", "Unsupported trailing comma", 1, 3, 3)
    }

    @Test
    fun must_fail_if_array_is_not_closed() {
        testError("[1", "Incomplete JSON", 1, 2, 2)
    }

    @Test
    fun must_fail_if_array_close_is_not_balanced() {
        testError("{]", "Expected '}'", 1, 2, 2)
    }

    // endregion

    // region Helper Functions

    private fun testError(json: String, error: String, line: Int, column: Int, charactersFromStart: Int) {
        val buffer = Buffer().writeUtf8(json)
        val exception = assertFailsWith<TokenException> {
            tokenizeJson(buffer).toList()
        }
        assertEquals(error, exception.message)
        assertEquals(line, exception.line)
        assertEquals(column, exception.column)
        assertEquals(charactersFromStart, exception.charactersFromStart)
    }

    // endregion
}