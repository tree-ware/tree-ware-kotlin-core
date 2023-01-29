package org.treeWare.json

import okio.Buffer
import org.treeWare.util.TokenException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class TokenizeJsonTests {
    private fun doubleQuoted(value: String): String = "\"$value\""

    // region Empty and blank JSON

    @Test
    fun must_not_emit_tokens_for_empty_json() {
        val json = ""
        val expected = emptyList<JsonToken>()
        testSuccess(json, expected)
    }

    @Test
    fun must_not_emit_tokens_for_blank_json() {
        val json = " \n\t\r\n"
        val expected = emptyList<JsonToken>()
        testSuccess(json, expected)
    }

    // endregion

    // region String values

    @Test
    fun must_tokenize_empty_string_value() {
        val json = doubleQuoted("")
        val expected = listOf(ImmutableJsonToken(JsonTokenType.VALUE_STRING, "", 1, 1, 1))
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_string_value() {
        val json = doubleQuoted("hello world")
        val expected = listOf(ImmutableJsonToken(JsonTokenType.VALUE_STRING, "hello world", 1, 1, 1))
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_string_value_with_escaped_double_quote() {
        val json = doubleQuoted("\\\"")
        val expected = listOf(ImmutableJsonToken(JsonTokenType.VALUE_STRING, "\"", 1, 1, 1))
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_string_value_with_escaped_back_slash() {
        val json = doubleQuoted("\\\\")
        val expected = listOf(ImmutableJsonToken(JsonTokenType.VALUE_STRING, "\\", 1, 1, 1))
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_string_value_with_escaped_forward_slash() {
        val json = doubleQuoted("\\/")
        val expected = listOf(ImmutableJsonToken(JsonTokenType.VALUE_STRING, "/", 1, 1, 1))
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_string_value_with_escaped_backspace() {
        val json = doubleQuoted("\\b")
        val expected = listOf(ImmutableJsonToken(JsonTokenType.VALUE_STRING, "\b", 1, 1, 1))
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_string_value_with_escaped_form_feed() {
        val json = doubleQuoted("\\f")
        val expected = listOf(ImmutableJsonToken(JsonTokenType.VALUE_STRING, "\u000c", 1, 1, 1))
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_string_value_with_escaped_line_feed() {
        val json = doubleQuoted("\\n")
        val expected = listOf(ImmutableJsonToken(JsonTokenType.VALUE_STRING, "\n", 1, 1, 1))
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_string_value_with_escaped_carriage_return() {
        val json = doubleQuoted("\\r")
        val expected = listOf(ImmutableJsonToken(JsonTokenType.VALUE_STRING, "\r", 1, 1, 1))
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_string_value_with_escaped_horizontal_tab() {
        val json = doubleQuoted("\\t")
        val expected = listOf(ImmutableJsonToken(JsonTokenType.VALUE_STRING, "\t", 1, 1, 1))
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_string_value_with_escaped_unicode() {
        val json = doubleQuoted("\\uC4f3 \\u0030")
        val expected = listOf(ImmutableJsonToken(JsonTokenType.VALUE_STRING, "\uc4f3 \u0030", 1, 1, 1))
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_string_value_with_escaped_and_non_escaped() {
        val json = doubleQuoted("\\\" \\\\ \\/ \\thello\\nworld\\f\\r\\uC4f3 \\b\\u0030")
        val expected = listOf(
            ImmutableJsonToken(
                JsonTokenType.VALUE_STRING,
                "\" \\ / \thello\nworld\u000c\r\uC4f3 \b\u0030",
                1,
                1,
                1
            )
        )
        testSuccess(json, expected)
    }

    // endregion

    // region Positive number values

    @Test
    fun must_tokenize_integer_value() {
        val json = "\t123  \n"
        val expected = listOf(ImmutableJsonToken(JsonTokenType.VALUE_NUMBER, "123", 1, 5, 2))
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_decimal_value() {
        val json = "\t123.45  \n"
        val expected = listOf(ImmutableJsonToken(JsonTokenType.VALUE_NUMBER, "123.45", 1, 5, 2))
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_exponent_value() {
        val json = "\t123.45e6  \n"
        val expected = listOf(ImmutableJsonToken(JsonTokenType.VALUE_NUMBER, "123.45e6", 1, 5, 2))
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_exponent_value_with_positive_exponent() {
        val json = "\t123.45E+6  \n"
        val expected = listOf(ImmutableJsonToken(JsonTokenType.VALUE_NUMBER, "123.45E+6", 1, 5, 2))
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_exponent_value_with_negative_exponent() {
        val json = "\t123.45E-6  \n"
        val expected = listOf(ImmutableJsonToken(JsonTokenType.VALUE_NUMBER, "123.45E-6", 1, 5, 2))
        testSuccess(json, expected)
    }

    // endregion

    // region Negative number values

    @Test
    fun must_tokenize_negative_integer_value() {
        val json = "\t-123  \n"
        val expected = listOf(ImmutableJsonToken(JsonTokenType.VALUE_NUMBER, "-123", 1, 5, 2))
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_negative_decimal_value() {
        val json = "\t-123.45  \n"
        val expected = listOf(ImmutableJsonToken(JsonTokenType.VALUE_NUMBER, "-123.45", 1, 5, 2))
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_negative_exponent_value() {
        val json = "\t-123.45e6  \n"
        val expected = listOf(ImmutableJsonToken(JsonTokenType.VALUE_NUMBER, "-123.45e6", 1, 5, 2))
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_negative_exponent_value_with_positive_exponent() {
        val json = "\t-123.45E+6  \n"
        val expected = listOf(ImmutableJsonToken(JsonTokenType.VALUE_NUMBER, "-123.45E+6", 1, 5, 2))
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_negative_exponent_value_with_negative_exponent() {
        val json = "\t-123.45E-6  \n"
        val expected = listOf(ImmutableJsonToken(JsonTokenType.VALUE_NUMBER, "-123.45E-6", 1, 5, 2))
        testSuccess(json, expected)
    }

    // endregion

    // region Boolean values

    @Test
    fun must_tokenize_true_value() {
        val json = "\ttrue  \n"
        val expected = listOf(ImmutableJsonToken(JsonTokenType.VALUE_TRUE, "", 1, 5, 2))
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_false_value() {
        val json = "\tfalse  \n"
        val expected = listOf(ImmutableJsonToken(JsonTokenType.VALUE_FALSE, "", 1, 5, 2))
        testSuccess(json, expected)
    }

    // endregion

    // region Null values

    @Test
    fun must_tokenize_null_value() {
        val json = "\tnull  \n"
        val expected = listOf(ImmutableJsonToken(JsonTokenType.VALUE_NULL, "", 1, 5, 2))
        testSuccess(json, expected)
    }

    // endregion

    // region Objects

    @Test
    fun must_tokenize_empty_object() {
        val json = "\t{ \t\n } \n"
        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.OBJECT_START, "", 1, 5, 2),
            ImmutableJsonToken(JsonTokenType.OBJECT_END, "", 2, 2, 7)
        )
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_object_with_true_value() {
        val json = "\t{ \"key\": \ttrue \t\n } \n"
        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.OBJECT_START, "", 1, 5, 2),
            ImmutableJsonToken(JsonTokenType.KEY_NAME, "key", 1, 7, 4),
            ImmutableJsonToken(JsonTokenType.VALUE_TRUE, "", 1, 18, 12),
            ImmutableJsonToken(JsonTokenType.OBJECT_END, "", 2, 2, 20)
        )
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_object_with_false_value() {
        val json = "\t{ \"key\"\t:false \t\n } \n"
        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.OBJECT_START, "", 1, 5, 2),
            ImmutableJsonToken(JsonTokenType.KEY_NAME, "key", 1, 7, 4),
            ImmutableJsonToken(JsonTokenType.VALUE_FALSE, "", 1, 17, 11),
            ImmutableJsonToken(JsonTokenType.OBJECT_END, "", 2, 2, 20)
        )
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_object_with_null_value() {
        val json = "\t{ \"key\"\t: null \t\n } \n"
        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.OBJECT_START, "", 1, 5, 2),
            ImmutableJsonToken(JsonTokenType.KEY_NAME, "key", 1, 7, 4),
            ImmutableJsonToken(JsonTokenType.VALUE_NULL, "", 1, 18, 12),
            ImmutableJsonToken(JsonTokenType.OBJECT_END, "", 2, 2, 20),
        )
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_object_with_string_value() {
        val json = "\t{ \"key\"\t: \"value\" \t\n } \n"
        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.OBJECT_START, "", 1, 5, 2),
            ImmutableJsonToken(JsonTokenType.KEY_NAME, "key", 1, 7, 4),
            ImmutableJsonToken(JsonTokenType.VALUE_STRING, "value", 1, 18, 12),
            ImmutableJsonToken(JsonTokenType.OBJECT_END, "", 2, 2, 23),
        )
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_object_with_number_value() {
        val json = "\t{ \"key\"\t: -123.45e+6 \t\n } \n"
        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.OBJECT_START, "", 1, 5, 2),
            ImmutableJsonToken(JsonTokenType.KEY_NAME, "key", 1, 7, 4),
            ImmutableJsonToken(JsonTokenType.VALUE_NUMBER, "-123.45e+6", 1, 18, 12),
            ImmutableJsonToken(JsonTokenType.OBJECT_END, "", 2, 2, 26),
        )
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_object_with_object_value() {
        val json = "\t{\"outerKey\": { \"innerKey\"\t: -123.45e+6 \t\n }} \n"
        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.OBJECT_START, "", 1, 5, 2),
            ImmutableJsonToken(JsonTokenType.KEY_NAME, "outerKey", 1, 6, 3),
            ImmutableJsonToken(JsonTokenType.OBJECT_START, "", 1, 18, 15),
            ImmutableJsonToken(JsonTokenType.KEY_NAME, "innerKey", 1, 20, 17),
            ImmutableJsonToken(JsonTokenType.VALUE_NUMBER, "-123.45e+6", 1, 36, 30),
            ImmutableJsonToken(JsonTokenType.OBJECT_END, "", 2, 2, 44),
            ImmutableJsonToken(JsonTokenType.OBJECT_END, "", 2, 3, 45),
        )
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_object_with_array_value() {
        val json = "\t{ \"key\"\t: [-123.45e+6] \t\n } \n"
        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.OBJECT_START, "", 1, 5, 2),
            ImmutableJsonToken(JsonTokenType.KEY_NAME, "key", 1, 7, 4),
            ImmutableJsonToken(JsonTokenType.ARRAY_START, "", 1, 18, 12),
            ImmutableJsonToken(JsonTokenType.VALUE_NUMBER, "-123.45e+6", 1, 19, 13),
            ImmutableJsonToken(JsonTokenType.ARRAY_END, "", 1, 29, 23),
            ImmutableJsonToken(JsonTokenType.OBJECT_END, "", 2, 2, 28),
        )
        testSuccess(json, expected)
    }

    @Test
    fun must_token_object_with_mixed_values() {
        val json =
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
            """.trimIndent()
        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.OBJECT_START, "", 1, 1, 1),
            ImmutableJsonToken(JsonTokenType.KEY_NAME, "true-field", 2, 5, 7),
            ImmutableJsonToken(JsonTokenType.VALUE_TRUE, "", 2, 19, 21),
            ImmutableJsonToken(JsonTokenType.KEY_NAME, "false_field", 3, 5, 31),
            ImmutableJsonToken(JsonTokenType.VALUE_FALSE, "", 3, 20, 46),
            ImmutableJsonToken(JsonTokenType.KEY_NAME, "nullField", 4, 5, 57),
            ImmutableJsonToken(JsonTokenType.VALUE_NULL, "", 4, 18, 70),
            ImmutableJsonToken(JsonTokenType.KEY_NAME, "StringField", 5, 5, 80),
            ImmutableJsonToken(JsonTokenType.VALUE_STRING, "hello", 5, 20, 95),
            ImmutableJsonToken(JsonTokenType.KEY_NAME, "NUMBER_FIELD", 6, 5, 108),
            ImmutableJsonToken(JsonTokenType.VALUE_NUMBER, "-123.45e+6", 6, 21, 124),
            ImmutableJsonToken(JsonTokenType.KEY_NAME, "empty object", 7, 5, 140),
            ImmutableJsonToken(JsonTokenType.OBJECT_START, "", 7, 21, 156),
            ImmutableJsonToken(JsonTokenType.OBJECT_END, "", 7, 22, 157),
            ImmutableJsonToken(JsonTokenType.KEY_NAME, "non-empty object", 8, 5, 164),
            ImmutableJsonToken(JsonTokenType.OBJECT_START, "", 8, 25, 184),
            ImmutableJsonToken(JsonTokenType.KEY_NAME, "key", 8, 26, 185),
            ImmutableJsonToken(JsonTokenType.VALUE_STRING, "value", 8, 33, 192),
            ImmutableJsonToken(JsonTokenType.OBJECT_END, "", 8, 40, 199),
            ImmutableJsonToken(JsonTokenType.KEY_NAME, "empty array", 9, 5, 206),
            ImmutableJsonToken(JsonTokenType.ARRAY_START, "", 9, 20, 221),
            ImmutableJsonToken(JsonTokenType.ARRAY_END, "", 9, 21, 222),
            ImmutableJsonToken(JsonTokenType.KEY_NAME, "non-empty array", 10, 5, 229),
            ImmutableJsonToken(JsonTokenType.ARRAY_START, "", 10, 24, 248),
            ImmutableJsonToken(JsonTokenType.VALUE_TRUE, "", 10, 25, 249),
            ImmutableJsonToken(JsonTokenType.VALUE_FALSE, "", 10, 31, 255),
            ImmutableJsonToken(JsonTokenType.ARRAY_END, "", 10, 36, 260),
            ImmutableJsonToken(JsonTokenType.OBJECT_END, "", 11, 1, 262),
        )
        testSuccess(json, expected)
    }

    // endregion

    // region Arrays

    @Test
    fun must_tokenize_empty_array() {
        val json = "\t[ \t\n ] \n"
        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.ARRAY_START, "", 1, 5, 2),
            ImmutableJsonToken(JsonTokenType.ARRAY_END, "", 2, 2, 7),
        )
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_array_with_one_true_value() {
        val json = "\t[ \ttrue \t\n ] \n"
        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.ARRAY_START, "", 1, 5, 2),
            ImmutableJsonToken(JsonTokenType.VALUE_TRUE, "", 1, 11, 5),
            ImmutableJsonToken(JsonTokenType.ARRAY_END, "", 2, 2, 13),
        )
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_array_with_many_true_values() {
        val json = "\t[ \ttrue, true,true \t\n ] \n"
        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.ARRAY_START, "", 1, 5, 2),
            ImmutableJsonToken(JsonTokenType.VALUE_TRUE, "", 1, 11, 5),
            ImmutableJsonToken(JsonTokenType.VALUE_TRUE, "", 1, 17, 11),
            ImmutableJsonToken(JsonTokenType.VALUE_TRUE, "", 1, 22, 16),
            ImmutableJsonToken(JsonTokenType.ARRAY_END, "", 2, 2, 24),
        )
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_array_with_one_false_value() {
        val json = "\t[ \tfalse \t\n ] \n"
        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.ARRAY_START, "", 1, 5, 2),
            ImmutableJsonToken(JsonTokenType.VALUE_FALSE, "", 1, 11, 5),
            ImmutableJsonToken(JsonTokenType.ARRAY_END, "", 2, 2, 14),
        )
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_array_with_many_false_values() {
        val json = "\t[ \tfalse, false,false \t\n ] \n"
        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.ARRAY_START, "", 1, 5, 2),
            ImmutableJsonToken(JsonTokenType.VALUE_FALSE, "", 1, 11, 5),
            ImmutableJsonToken(JsonTokenType.VALUE_FALSE, "", 1, 18, 12),
            ImmutableJsonToken(JsonTokenType.VALUE_FALSE, "", 1, 24, 18),
            ImmutableJsonToken(JsonTokenType.ARRAY_END, "", 2, 2, 27)
        )
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_array_with_one_null_value() {
        val json = "\t[ \tnull \t\n ] \n"
        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.ARRAY_START, "", 1, 5, 2),
            ImmutableJsonToken(JsonTokenType.VALUE_NULL, "", 1, 11, 5),
            ImmutableJsonToken(JsonTokenType.ARRAY_END, "", 2, 2, 13),
        )
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_array_with_many_null_values() {
        val json = "\t[ \tnull, null,null \t\n ] \n"
        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.ARRAY_START, "", 1, 5, 2),
            ImmutableJsonToken(JsonTokenType.VALUE_NULL, "", 1, 11, 5),
            ImmutableJsonToken(JsonTokenType.VALUE_NULL, "", 1, 17, 11),
            ImmutableJsonToken(JsonTokenType.VALUE_NULL, "", 1, 22, 16),
            ImmutableJsonToken(JsonTokenType.ARRAY_END, "", 2, 2, 24),
        )
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_array_with_one_string_value() {
        val json = "\t[ \t\"hello!\" \t\n ] \n"
        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.ARRAY_START, "", 1, 5, 2),
            ImmutableJsonToken(JsonTokenType.VALUE_STRING, "hello!", 1, 11, 5),
            ImmutableJsonToken(JsonTokenType.ARRAY_END, "", 2, 2, 17),
        )
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_array_with_many_string_values() {
        val json = "\t[ \t\"how\", \"are\",\"you?\" \t\n ] \n"
        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.ARRAY_START, "", 1, 5, 2),
            ImmutableJsonToken(JsonTokenType.VALUE_STRING, "how", 1, 11, 5),
            ImmutableJsonToken(JsonTokenType.VALUE_STRING, "are", 1, 18, 12),
            ImmutableJsonToken(JsonTokenType.VALUE_STRING, "you?", 1, 24, 18),
            ImmutableJsonToken(JsonTokenType.ARRAY_END, "", 2, 2, 28),
        )
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_array_with_one_number_value() {
        val json = "\t[ \t-123.45e+6 \t\n ] \n"
        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.ARRAY_START, "", 1, 5, 2),
            ImmutableJsonToken(JsonTokenType.VALUE_NUMBER, "-123.45e+6", 1, 11, 5),
            ImmutableJsonToken(JsonTokenType.ARRAY_END, "", 2, 2, 19),
        )
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_array_with_many_number_values() {
        val json = "\t[ \t-123.45, 123,-123.45e+6 \t\n ] \n"
        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.ARRAY_START, "", 1, 5, 2),
            ImmutableJsonToken(JsonTokenType.VALUE_NUMBER, "-123.45", 1, 11, 5),
            ImmutableJsonToken(JsonTokenType.VALUE_NUMBER, "123", 1, 20, 14),
            ImmutableJsonToken(JsonTokenType.VALUE_NUMBER, "-123.45e+6", 1, 24, 18),
            ImmutableJsonToken(JsonTokenType.ARRAY_END, "", 2, 2, 32),
        )
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_array_with_one_object() {
        val json = "\t[ \t{\"key\": \"value\"} \t\n ] \n"
        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.ARRAY_START, "", 1, 5, 2),
            ImmutableJsonToken(JsonTokenType.OBJECT_START, "", 1, 11, 5),
            ImmutableJsonToken(JsonTokenType.KEY_NAME, "key", 1, 12, 6),
            ImmutableJsonToken(JsonTokenType.VALUE_STRING, "value", 1, 19, 13),
            ImmutableJsonToken(JsonTokenType.OBJECT_END, "", 1, 26, 20),
            ImmutableJsonToken(JsonTokenType.ARRAY_END, "", 2, 2, 25),
        )
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_array_with_many_objects() {
        val json =
            """
            [
                {"key1": "value1"},
                {"key2": "value2"},
                {"key3": "value3"}
            ]
            """.trimIndent()

        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.ARRAY_START, "", 1, 1, 1),
            ImmutableJsonToken(JsonTokenType.OBJECT_START, "", 2, 5, 7),
            ImmutableJsonToken(JsonTokenType.KEY_NAME, "key1", 2, 6, 8),
            ImmutableJsonToken(JsonTokenType.VALUE_STRING, "value1", 2, 14, 16),
            ImmutableJsonToken(JsonTokenType.OBJECT_END, "", 2, 22, 24),
            ImmutableJsonToken(JsonTokenType.OBJECT_START, "", 3, 5, 31),
            ImmutableJsonToken(JsonTokenType.KEY_NAME, "key2", 3, 6, 32),
            ImmutableJsonToken(JsonTokenType.VALUE_STRING, "value2", 3, 14, 40),
            ImmutableJsonToken(JsonTokenType.OBJECT_END, "", 3, 22, 48),
            ImmutableJsonToken(JsonTokenType.OBJECT_START, "", 4, 5, 55),
            ImmutableJsonToken(JsonTokenType.KEY_NAME, "key3", 4, 6, 56),
            ImmutableJsonToken(JsonTokenType.VALUE_STRING, "value3", 4, 14, 64),
            ImmutableJsonToken(JsonTokenType.OBJECT_END, "", 4, 22, 72),
            ImmutableJsonToken(JsonTokenType.ARRAY_END, "", 5, 1, 74),
        )
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_array_with_one_array() {
        val json = "\t[ \t[\"hello\", \"world\"] \t\n ] \n"
        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.ARRAY_START, "", 1, 5, 2),
            ImmutableJsonToken(JsonTokenType.ARRAY_START, "", 1, 11, 5),
            ImmutableJsonToken(JsonTokenType.VALUE_STRING, "hello", 1, 12, 6),
            ImmutableJsonToken(JsonTokenType.VALUE_STRING, "world", 1, 21, 15),
            ImmutableJsonToken(JsonTokenType.ARRAY_END, "", 1, 28, 22),
            ImmutableJsonToken(JsonTokenType.ARRAY_END, "", 2, 2, 27),
        )
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_array_with_many_arrays() {
        val json =
            """
            [
                [true, false],
                ["hello", "world"],
                [123, null]
            ]
            """.trimIndent()
        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.ARRAY_START, "", 1, 1, 1),

            ImmutableJsonToken(JsonTokenType.ARRAY_START, "", 2, 5, 7),
            ImmutableJsonToken(JsonTokenType.VALUE_TRUE, "", 2, 6, 8),
            ImmutableJsonToken(JsonTokenType.VALUE_FALSE, "", 2, 12, 14),
            ImmutableJsonToken(JsonTokenType.ARRAY_END, "", 2, 17, 19),

            ImmutableJsonToken(JsonTokenType.ARRAY_START, "", 3, 5, 26),
            ImmutableJsonToken(JsonTokenType.VALUE_STRING, "hello", 3, 6, 27),
            ImmutableJsonToken(JsonTokenType.VALUE_STRING, "world", 3, 15, 36),
            ImmutableJsonToken(JsonTokenType.ARRAY_END, "", 3, 22, 43),

            ImmutableJsonToken(JsonTokenType.ARRAY_START, "", 4, 5, 50),
            ImmutableJsonToken(JsonTokenType.VALUE_NUMBER, "123", 4, 6, 51),
            ImmutableJsonToken(JsonTokenType.VALUE_NULL, "", 4, 11, 56),
            ImmutableJsonToken(JsonTokenType.ARRAY_END, "", 4, 15, 60),

            ImmutableJsonToken(JsonTokenType.ARRAY_END, "", 5, 1, 62),
        )
        testSuccess(json, expected)
    }

    @Test
    fun must_tokenize_array_with_mixed_values() {
        val json =
            """
            [
                true, false, null, "hello", 123,
                {"key": "value"},
                [1, 2, 3]
            ]
            """.trimIndent()
        val expected = listOf(
            ImmutableJsonToken(JsonTokenType.ARRAY_START, "", 1, 1, 1),

            ImmutableJsonToken(JsonTokenType.VALUE_TRUE, "", 2, 5, 7),
            ImmutableJsonToken(JsonTokenType.VALUE_FALSE, "", 2, 11, 13),
            ImmutableJsonToken(JsonTokenType.VALUE_NULL, "", 2, 18, 20),
            ImmutableJsonToken(JsonTokenType.VALUE_STRING, "hello", 2, 24, 26),
            ImmutableJsonToken(JsonTokenType.VALUE_NUMBER, "123", 2, 33, 35),

            ImmutableJsonToken(JsonTokenType.OBJECT_START, "", 3, 5, 44),
            ImmutableJsonToken(JsonTokenType.KEY_NAME, "key", 3, 6, 45),
            ImmutableJsonToken(JsonTokenType.VALUE_STRING, "value", 3, 13, 52),
            ImmutableJsonToken(JsonTokenType.OBJECT_END, "", 3, 20, 59),

            ImmutableJsonToken(JsonTokenType.ARRAY_START, "", 4, 5, 66),
            ImmutableJsonToken(JsonTokenType.VALUE_NUMBER, "1", 4, 6, 67),
            ImmutableJsonToken(JsonTokenType.VALUE_NUMBER, "2", 4, 9, 70),
            ImmutableJsonToken(JsonTokenType.VALUE_NUMBER, "3", 4, 12, 73),
            ImmutableJsonToken(JsonTokenType.ARRAY_END, "", 4, 13, 74),

            ImmutableJsonToken(JsonTokenType.ARRAY_END, "", 5, 1, 76),
        )
        testSuccess(json, expected)
    }

    // endregion

    // region Errors

    @Test
    fun must_fail_if_string_value_does_not_end_with_double_quotes() {
        testFailure("\"hello", "Incomplete JSON", 1, 6, 6)
        testFailure("{\n\t\"a\": \"hello}", "Incomplete JSON", 2, 16, 15)
        testFailure("[\r\n\t\"hello]", "Incomplete JSON", 2, 11, 11)
    }

    @Test
    fun must_fail_for_invalid_hex_escape() {
        testFailure("\"\\uC4f\"", "Expected hex digit but found '\"'", 1, 7, 7)
    }

    @Test
    fun must_fail_for_invalid_single_digit_integer() {
        testFailure("-a", "Expected digit", 1, 2, 2)
    }

    @Test
    fun must_fail_for_invalid_multi_digit_integer() {
        testFailure("1a", "Unexpected character", 1, 2, 2)
    }

    @Test
    fun must_fail_for_invalid_single_digit_fraction() {
        testFailure("-12.a", "Expected digit", 1, 5, 5)
    }

    @Test
    fun must_fail_for_invalid_multi_digit_fraction() {
        testFailure("-12.3a", "Unexpected character", 1, 6, 6)
    }

    @Test
    fun must_fail_for_invalid_single_digit_exponent() {
        testFailure("-12.34Ea", "Expected digit", 1, 8, 8)
    }

    @Test
    fun must_fail_for_invalid_multi_digit_exponent() {
        testFailure("-12.34E5a", "Unexpected character", 1, 9, 9)
    }

    @Test
    fun must_fail_for_invalid_true() {
        testFailure("[ tRUE ]", "Expected 'true'", 1, 4, 4)
    }

    @Test
    fun must_fail_for_invalid_false() {
        testFailure("[\nfaLSE\n]", "Expected 'false'", 2, 3, 5)
    }

    @Test
    fun must_fail_for_invalid_null() {
        testFailure("nULL", "Expected 'null'", 1, 2, 2)
    }

    @Test
    fun must_fail_for_invalid_value() {
        testFailure("hello", "Unknown value type", 1, 1, 1)
    }

    @Test
    fun must_fail_if_starts_with_object_end() {
        testFailure("}", "Unexpected '}'", 1, 1, 1)
    }

    @Test
    fun must_fail_if_starts_with_array_end() {
        testFailure("]", "Unexpected ']'", 1, 1, 1)
    }

    @Test
    fun must_fail_if_starts_with_colon() {
        testFailure(":", "Unknown value type", 1, 1, 1)
    }

    @Test
    fun must_fail_if_starts_with_comma() {
        testFailure(",", "Unknown value type", 1, 1, 1)
    }

    @Test
    fun must_fail_for_invalid_value_in_an_object() {
        testFailure("{\"greeting\": hello}", "Unknown value type", 1, 14, 14)
    }

    @Test
    fun must_fail_for_invalid_value_in_an_array() {
        testFailure("[hello]", "Unknown value type", 1, 2, 2)
    }

    @Test
    fun must_fail_if_field_name_does_not_start_with_double_quotes() {
        testFailure("{a: 1}", "Expected '\"'", 1, 2, 2)
    }

    @Test
    fun must_fail_if_field_name_does_not_end_with_double_quotes() {
        testFailure("{\"a: 1}", "Incomplete JSON", 1, 7, 7)
    }

    @Test
    fun must_fail_if_colon_is_missing_after_field_name() {
        testFailure("{\"a\" 1}", "Expected ':'", 1, 6, 6)
    }

    @Test
    fun must_fail_for_trailing_comma_in_object() {
        testFailure("{\"a\": 1,}", "Expected '\"'", 1, 9, 9)
    }

    @Test
    fun must_fail_if_object_is_not_closed() {
        testFailure("{\"a\": 1", "Incomplete JSON", 1, 7, 7)
    }

    @Test
    fun must_fail_if_object_close_is_not_balanced() {
        testFailure("[}", "Unexpected '}'", 1, 2, 2)
    }

    @Test
    fun must_fail_for_trailing_comma_in_array() {
        testFailure("[1,]", "Unsupported trailing comma", 1, 3, 3)
    }

    @Test
    fun must_fail_if_array_is_not_closed() {
        testFailure("[1", "Incomplete JSON", 1, 2, 2)
    }

    @Test
    fun must_fail_if_array_close_is_not_balanced() {
        testFailure("{]", "Expected '}'", 1, 2, 2)
    }

    // endregion

    // region Helper Functions

    private fun testSuccess(json: String, expected: List<JsonToken>) {
        val buffer = Buffer().writeUtf8(json)
        var actualSize = 0
        tokenizeJson(buffer).forEachIndexed { index, actualToken ->
            ++actualSize
            val expectedToken = expected[index]
            assertEquals(expectedToken, actualToken, "Index $index")
        }
        assertEquals(expected.size, actualSize)
    }

    private fun testFailure(json: String, error: String, line: Int, column: Int, charactersFromStart: Int) {
        val buffer = Buffer().writeUtf8(json)
        val exception = assertFailsWith<TokenException> {
            tokenizeJson(buffer).toList()
        }
        assertEquals(error, exception.message)
        assertEquals(line, exception.position.line)
        assertEquals(column, exception.position.column)
        assertEquals(charactersFromStart, exception.position.charactersFromStart)
    }

    // endregion
}
