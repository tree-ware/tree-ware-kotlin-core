package org.treeWare.metaModel.validation

import org.treeWare.metaModel.assertJsonStringValidationErrors
import org.treeWare.metaModel.newTestMetaModelJson
import org.treeWare.metaModel.testMetaModelCommonPackageJson
import org.treeWare.metaModel.testMetaModelCommonRootJson
import kotlin.test.Test

class ExistsIfValidationTests {
    @Test
    fun `exists_if clause must be optional`() {
        val metaModelJson = getMetaModelJson(null)
        val expectedErrors = emptyList<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `exists_if clause must specify an operator`() {
        val metaModelJson = getMetaModelJson(
            """
            |{}
            """.trimMargin()
        )
        val expectedErrors = listOf("/test.main/main_entity/test_field exists_if operator is missing")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `exists_if clause must specify valid operators`() {
        val metaModelJson = getMetaModelJson(
            """
            |{
            |  "operator": "invalid",
            |  "field": "immaterial",
            |  "value": "unimportant"
            |}
            """.trimMargin()
        )
        val expectedErrors = listOf("Meta-model decoding failed")
        val expectedDecodeErrors = listOf("JSON decoding failed at line 102 column 15, 1957 characters from the start")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors, expectedDecodeErrors)
    }

    @Test
    fun `exists_if EQUALS clause must specify field and value but not arg1 or arg2`() {
        val metaModelJson = getMetaModelJson(
            """
            |{
            |  "operator": "equals",
            |  "arg1": {},
            |  "arg2": {}
            |}
            """.trimMargin()
        )
        val expectedErrors = listOf(
            "/test.main/main_entity/test_field exists_if EQUALS clause field missing",
            "/test.main/main_entity/test_field exists_if EQUALS clause value missing",
            "/test.main/main_entity/test_field exists_if EQUALS clause arg1 must not be specified",
            "/test.main/main_entity/test_field exists_if EQUALS clause arg2 must not be specified",
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `exists_if AND clause must specify arg1 and arg2 but not field and value`() {
        val metaModelJson = getMetaModelJson(
            """
            |{
            |  "operator": "and",
            |  "field": "immaterial",
            |  "value": "unimportant"
            |}
            """.trimMargin()
        )
        val expectedErrors = listOf(
            "/test.main/main_entity/test_field exists_if AND clause field must not be specified",
            "/test.main/main_entity/test_field exists_if AND clause value must not be specified",
            "/test.main/main_entity/test_field exists_if AND clause arg1 missing",
            "/test.main/main_entity/test_field exists_if AND clause arg2 missing",
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `exists_if OR clause must specify arg1 and arg2 but not field and value`() {
        val metaModelJson = getMetaModelJson(
            """
            |{
            |  "operator": "or",
            |  "field": "immaterial",
            |  "value": "unimportant"
            |}
            """.trimMargin()
        )
        val expectedErrors = listOf(
            "/test.main/main_entity/test_field exists_if OR clause field must not be specified",
            "/test.main/main_entity/test_field exists_if OR clause value must not be specified",
            "/test.main/main_entity/test_field exists_if OR clause arg1 missing",
            "/test.main/main_entity/test_field exists_if OR clause arg2 missing",
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `exists_if NOT clause must specify arg1 but not field and value and arg2`() {
        val metaModelJson = getMetaModelJson(
            """
            |{
            |  "operator": "not",
            |  "field": "immaterial",
            |  "value": "unimportant",
            |  "arg2": {}
            |}
            """.trimMargin()
        )
        val expectedErrors = listOf(
            "/test.main/main_entity/test_field exists_if NOT clause field must not be specified",
            "/test.main/main_entity/test_field exists_if NOT clause value must not be specified",
            "/test.main/main_entity/test_field exists_if NOT clause arg1 missing",
            "/test.main/main_entity/test_field exists_if NOT clause arg2 must not be specified",
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `exists_if nested clause must fail for invalid definitions`() {
        val metaModelJson = getMetaModelJson(
            """
            |{
            |  "operator": "not",
            |  "field": "immaterial",
            |  "value": "unimportant",
            |  "arg1": {
            |    "operator": "and",
            |    "field": "immaterial",
            |    "value": "unimportant",
            |    "arg1": {
            |      "operator": "or",
            |      "field": "immaterial",
            |      "value": "unimportant",
            |      "arg2": {
            |        "operator": "equals",
            |        "arg1": {},
            |        "arg2": {}
            |      }
            |    }
            |  },
            |  "arg2": {}
            |}
            """.trimMargin()
        )
        // NOTE: validation is depth-first for nested clauses.
        val expectedErrors = listOf(
            "/test.main/main_entity/test_field exists_if NOT clause field must not be specified",
            "/test.main/main_entity/test_field exists_if NOT clause value must not be specified",

            "/test.main/main_entity/test_field exists_if AND clause field must not be specified",
            "/test.main/main_entity/test_field exists_if AND clause value must not be specified",

            "/test.main/main_entity/test_field exists_if OR clause field must not be specified",
            "/test.main/main_entity/test_field exists_if OR clause value must not be specified",
            "/test.main/main_entity/test_field exists_if OR clause arg1 missing",

            "/test.main/main_entity/test_field exists_if EQUALS clause field missing",
            "/test.main/main_entity/test_field exists_if EQUALS clause value missing",
            "/test.main/main_entity/test_field exists_if EQUALS clause arg1 must not be specified",
            "/test.main/main_entity/test_field exists_if EQUALS clause arg2 must not be specified",

            "/test.main/main_entity/test_field exists_if AND clause arg2 missing",
            "/test.main/main_entity/test_field exists_if NOT clause arg2 must not be specified",
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `exists_if EQUALS clause field must exist in entity`() {
        val metaModelJson = getMetaModelJson(
            """
            |{
            |  "operator": "equals",
            |  "field": "unknown",
            |  "value": "unimportant"
            |}
            """.trimMargin()
        )
        val expectedErrors =
            listOf("/test.main/main_entity/test_field exists_if EQUALS clause field `unknown` is not found")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `exists_if EQUALS clause field must not refer to self`() {
        val metaModelJson = getMetaModelJson(
            """
            |{
            |  "operator": "equals",
            |  "field": "test_field",
            |  "value": "unimportant"
            |}
            """.trimMargin()
        )
        val expectedErrors =
            listOf("/test.main/main_entity/test_field exists_if EQUALS clause field `test_field` refers to self")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `exists_if EQUALS clause field must not be a password1way field`() {
        val metaModelJson = getMetaModelJson(
            """
            |{
            |  "operator": "equals",
            |  "field": "password1way_field",
            |  "value": "unimportant"
            |}
            """.trimMargin()
        )
        val expectedErrors =
            listOf("/test.main/main_entity/test_field exists_if EQUALS clause field `password1way_field` is not a supported field type")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `exists_if EQUALS clause field must not be a password2way field`() {
        val metaModelJson = getMetaModelJson(
            """
            |{
            |  "operator": "equals",
            |  "field": "password2way_field",
            |  "value": "unimportant"
            |}
            """.trimMargin()
        )
        val expectedErrors =
            listOf("/test.main/main_entity/test_field exists_if EQUALS clause field `password2way_field` is not a supported field type")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `exists_if EQUALS clause field must not be an association field`() {
        val metaModelJson = getMetaModelJson(
            """
            |{
            |  "operator": "equals",
            |  "field": "association_field",
            |  "value": "unimportant"
            |}
            """.trimMargin()
        )
        val expectedErrors =
            listOf("/test.main/main_entity/test_field exists_if EQUALS clause field `association_field` is not a supported field type")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `exists_if EQUALS clause field must not be a composition field`() {
        val metaModelJson = getMetaModelJson(
            """
            |{
            |  "operator": "equals",
            |  "field": "composition_field",
            |  "value": "unimportant"
            |}
            """.trimMargin()
        )
        val expectedErrors =
            listOf("/test.main/main_entity/test_field exists_if EQUALS clause field `composition_field` is not a supported field type")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `exists_if EQUALS clause field must not be a list field`() {
        val metaModelJson = getMetaModelJson(
            """
            |{
            |  "operator": "equals",
            |  "field": "list_field",
            |  "value": "unimportant"
            |}
            """.trimMargin()
        )
        val expectedErrors =
            listOf("/test.main/main_entity/test_field exists_if EQUALS clause field `list_field` is not a single field")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `exists_if EQUALS clause field must not be a set field`() {
        val metaModelJson = getMetaModelJson(
            """
            |{
            |  "operator": "equals",
            |  "field": "set_field",
            |  "value": "unimportant"
            |}
            """.trimMargin()
        )
        val expectedErrors = listOf(
            "/test.main/main_entity/test_field exists_if EQUALS clause field `set_field` is not a supported field type",
            "/test.main/main_entity/test_field exists_if EQUALS clause field `set_field` is not a single field"
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `exists_if EQUALS clause enumeration value must be assignable to the field`() {
        val metaModelJson = getMetaModelJson(
            """
            |{
            |  "operator": "equals",
            |  "field": "enumeration_field",
            |  "value": "invalid"
            |}
            """.trimMargin()
        )
        val expectedErrors =
            listOf("/test.main/main_entity/test_field exists_if EQUALS clause value `invalid` is not assignable to field `enumeration_field`")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `exists_if EQUALS clause integer value must be assignable to the field`() {
        val metaModelJson = getMetaModelJson(
            """
            |{
            |  "operator": "equals",
            |  "field": "uint8_field",
            |  "value": "256"
            |}
            """.trimMargin()
        )
        val expectedErrors =
            listOf("/test.main/main_entity/test_field exists_if EQUALS clause value `256` is not assignable to field `uint8_field`")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `exists_if EQUALS clause boolean value must be assignable to the field`() {
        val metaModelJson = getMetaModelJson(
            """
            |{
            |  "operator": "equals",
            |  "field": "boolean_field",
            |  "value": "invalid"
            |}
            """.trimMargin()
        )
        val expectedErrors =
            listOf("/test.main/main_entity/test_field exists_if EQUALS clause value `invalid` is not assignable to field `boolean_field`")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `exists_if clause validation must succeed for valid definitions`() {
        val metaModelJson = getMetaModelJson(
            """
            |{
            |  "operator": "and",
            |  "arg1": {
            |    "operator": "equals",
            |    "field": "enumeration_field",
            |    "value": "value2"
            |  },
            |  "arg2": {
            |    "operator": "not",
            |    "arg1": {
            |      "operator": "equals",
            |      "field": "uint8_field",
            |      "value": "7"
            |    }
            |  }
            |}
            """.trimMargin()
        )
        val expectedErrors = emptyList<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }
}

private fun getMetaModelJson(existsIfJsonValue: String?): String {
    val existsIfJsonFieldValue = if (existsIfJsonValue == null) "" else """, "exists_if": $existsIfJsonValue"""
    val mainPackageJson = """
        | {
        |   "name": "test.main",
        |   "entities": [
        |     {
        |       "name": "main_entity",
        |       "fields": [
        |         {
        |           "name": "test_field",
        |           "number": 1,
        |           "type": "string"
        |           $existsIfJsonFieldValue
        |         },
        |         {
        |           "name": "enumeration_field",
        |           "number": 2,
        |           "type": "enumeration",
        |           "enumeration": {
        |             "name": "enumeration1",
        |             "package": "test.common"
        |           }
        |         },
        |         {
        |           "name": "string_field",
        |           "number": 3,
        |           "type": "string"
        |         },
        |         {
        |           "name": "uint8_field",
        |           "number": 4,
        |           "type": "uint8"
        |         },
        |         {
        |           "name": "boolean_field",
        |           "number": 5,
        |           "type": "boolean"
        |         },
        |         {
        |           "name": "password1way_field",
        |           "number": 6,
        |           "type": "password1way"
        |         },
        |         {
        |           "name": "password2way_field",
        |           "number": 7,
        |           "type": "password2way"
        |         },
        |         {
        |           "name": "association_field",
        |           "number": 8,
        |           "type": "association",
        |           "association": {
        |             "entity": "entity1",
        |             "package": "test.common"
        |           }
        |         },
        |         {
        |           "name": "composition_field",
        |           "number": 9,
        |           "type": "composition",
        |           "composition": {
        |             "entity": "entity1",
        |             "package": "test.common"
        |           }
        |         },
        |         {
        |           "name": "list_field",
        |           "number": 10,
        |           "type": "uint8",
        |           "multiplicity": "list"
        |         },
        |         {
        |           "name": "set_field",
        |           "number": 11,
        |           "type": "composition",
        |           "composition": {
        |             "entity": "entity2",
        |             "package": "test.common"
        |           },
        |           "multiplicity": "set"
        |         }
        |       ]
        |     }
        |   ]
        | }
    """.trimMargin()
    return newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, mainPackageJson)
}