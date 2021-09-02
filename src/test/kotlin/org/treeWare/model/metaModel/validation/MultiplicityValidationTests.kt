package org.treeWare.model.metaModel.validation

import org.treeWare.metaModel.assertJsonStringValidationErrors
import org.treeWare.metaModel.newTestMetaModelJson
import org.treeWare.metaModel.testMetaModelCommonPackageJson
import org.treeWare.metaModel.testMetaModelCommonRootJson
import kotlin.test.Test

private const val FIELD_COUNT = 4

class MultiplicityValidationTests {
    @Test
    fun `Multiplicity is valid if not specified`() {
        val metaModelJson = getMetaModelJson(null, null)
        val expectedErrors = listOf<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Multiplicity is valid if specified as 'required'`() {
        val metaModelJson = getMetaModelJson("required", null)
        val expectedErrors = listOf<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Multiplicity is valid if specified as 'optional'`() {
        val metaModelJson = getMetaModelJson("optional", null)
        val expectedErrors = listOf<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Multiplicity is valid if specified as 'list'`() {
        val metaModelJson = getMetaModelJson("list", null)
        val expectedErrors = listOf<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Multiplicity must be specified with a valid value`() {
        val metaModelJson = getMetaModelJson("invalid", null)
        val expectedErrors = listOf("Meta-model decoding failed")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Multiplicity must not be 'optional' for key fields`() {
        val metaModelJson = getMetaModelJson("optional", true)
        val expectedErrors =
            0.until(FIELD_COUNT).map { "Package 1 entity 0 field $it is a key but not defined as required" }
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Multiplicity must not be 'list' for key fields`() {
        val metaModelJson = getMetaModelJson("list", true)
        val expectedErrors =
            0.until(FIELD_COUNT).map { "Package 1 entity 0 field $it is a key but not defined as required" }
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Multiplicity can be unspecified for key fields`() {
        val metaModelJson = getMetaModelJson(null, true)
        val expectedErrors = listOf<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Multiplicity can be 'required' for key fields`() {
        val metaModelJson = getMetaModelJson("required", true)
        val expectedErrors = listOf<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }
}

private fun getMetaModelJson(multiplicity: String?, isKey: Boolean?): String {
    val multiplicityJson = getMultiplicityJson(multiplicity)
    val isKeyJson = getIsKeyJson(isKey)
    val mainPackageJson = """
        | {
        |   "name": "test.main",
        |   "entities": [
        |     {
        |       "name": "main_entity1",
        |       "fields": [
        |         {
        |           "name": "primitive_field",
        |           "type": "string"
        |           $multiplicityJson
        |           $isKeyJson
        |         },
        |         {
        |           "name": "enumeration_field",
        |           "type": "enumeration",
        |           "enumeration": {
        |             "name": "enumeration1",
        |             "package": "test.common"
        |           }
        |           $multiplicityJson
        |           $isKeyJson
        |         },
        |         {
        |           "name": "association_field",
        |           "type": "association",
        |           "association": [
        |             {
        |               "value": "root"
        |             },
        |             {
        |               "value": "entity1_composition_field"
        |             }
        |           ]
        |           $multiplicityJson
        |           $isKeyJson
        |         },
        |         {
        |           "name": "composition_field",
        |           "type": "entity",
        |           "entity": {
        |             "name": "entity3",
        |             "package": "test.common"
        |           }
        |           $multiplicityJson
        |           $isKeyJson
        |         }
        |       ]
        |     }
        |   ]
        | }
    """.trimMargin()
    return newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, mainPackageJson)
}

private fun getMultiplicityJson(multiplicity: String?): String =
    if (multiplicity == null) ""
    else """, "multiplicity": "$multiplicity""""

private fun getIsKeyJson(isKey: Boolean?): String =
    if (isKey == null) ""
    else """, "is_key": $isKey"""
