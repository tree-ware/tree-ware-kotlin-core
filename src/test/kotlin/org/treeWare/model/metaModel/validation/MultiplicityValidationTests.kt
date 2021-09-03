package org.treeWare.model.metaModel.validation

import org.treeWare.metaModel.*
import kotlin.test.Test

class MultiplicityValidationTests {
    @Test
    fun `Multiplicity must be valid if not specified`() {
        val metaModelJson = getMetaModelJson(null)
        val expectedErrors = listOf<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Multiplicity must be valid if specified as 'required'`() {
        val metaModelJson = getMetaModelJson("required")
        val expectedErrors = listOf<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Multiplicity must be valid if specified as 'optional'`() {
        val metaModelJson = getMetaModelJson("optional")
        val expectedErrors = listOf<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Multiplicity must be valid if specified as 'list'`() {
        val metaModelJson = getMetaModelJson("list")
        val expectedErrors = listOf<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Multiplicity must be specified with a valid value`() {
        val metaModelJson = getMetaModelJson("invalid")
        val expectedErrors = listOf("Meta-model decoding failed")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }
}

private fun getMetaModelJson(multiplicity: String?): String {
    val multiplicityJson = getMultiplicityJson(multiplicity)
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
        |         },
        |         {
        |           "name": "enumeration_field",
        |           "type": "enumeration",
        |           "enumeration": {
        |             "name": "enumeration1",
        |             "package": "test.common"
        |           }
        |           $multiplicityJson
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
        |         },
        |         {
        |           "name": "composition_field",
        |           "type": "composition",
        |           "composition": {
        |             "name": "entity3",
        |             "package": "test.common"
        |           }
        |           $multiplicityJson
        |         }
        |       ]
        |     }
        |   ]
        | }
    """.trimMargin()
    return newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, mainPackageJson)
}
