package org.treeWare.model.metaModel.validation

import org.treeWare.metaModel.*
import kotlin.test.Test

private const val FIELD_COUNT = 3

class KeyValidationTests {
    @Test
    fun `Multiplicity must not be 'optional' for key fields`() {
        val metaModelJson = getMetaModelJson("optional")
        val expectedErrors =
            0.until(FIELD_COUNT).map { "Package 1 entity 0 field $it is a key but not defined as required" }
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Multiplicity must not be 'list' for key fields`() {
        val metaModelJson = getMetaModelJson("list")
        val expectedErrors =
            0.until(FIELD_COUNT).map { "Package 1 entity 0 field $it is a key but not defined as required" }
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Multiplicity may be unspecified for key fields`() {
        val metaModelJson = getMetaModelJson(null)
        val expectedErrors = listOf<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Multiplicity may be 'required' for key fields`() {
        val metaModelJson = getMetaModelJson("required")
        val expectedErrors = listOf<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }
}

private fun getMetaModelJson(multiplicity: String?): String {
    val multiplicityJson = getMultiplicityJson(multiplicity)
    // NOTE: association fields cannot be keys, so they are not included below.
    // AssociationValidationTests ensures that associations cannot be keys.
    val mainPackageJson = """
        | {
        |   "name": "test.main",
        |   "entities": [
        |     {
        |       "name": "main_entity1",
        |       "fields": [
        |         {
        |           "name": "primitive_field",
        |           "type": "string",
        |           "is_key": true
        |           $multiplicityJson
        |         },
        |         {
        |           "name": "enumeration_field",
        |           "type": "enumeration",
        |           "enumeration": {
        |             "name": "enumeration1",
        |             "package": "test.common"
        |           },
        |           "is_key": true
        |           $multiplicityJson
        |         },
        |         {
        |           "name": "composition_field",
        |           "type": "entity",
        |           "entity": {
        |             "name": "entity3",
        |             "package": "test.common"
        |           },
        |           "is_key": true
        |           $multiplicityJson
        |         }
        |       ]
        |     }
        |   ]
        | }
    """.trimMargin()
    return newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, mainPackageJson)
}
