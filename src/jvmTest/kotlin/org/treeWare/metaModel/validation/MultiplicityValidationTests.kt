package org.treeWare.metaModel.validation

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
    fun `Multiplicity must not be 'list' for compositions`() {
        val metaModelJson = getMetaModelJson("list")
        val expectedErrors = listOf("Package 1 entity 0 field 3 is a composition field and they cannot be lists")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Multiplicity must be specified with a valid value`() {
        val metaModelJson = getMetaModelJson("invalid")
        val expectedDecodeErrors = listOf("JSON decoding failed at: (line no=98, column no=10, offset=1898)")
        val expectedValidationErrors = listOf("Meta-model decoding failed")
        assertJsonStringValidationErrors(metaModelJson, expectedValidationErrors, expectedDecodeErrors)
    }

    @Test
    fun `Multiplicity may be 'set' only for compositions`() {
        val metaModelJson = getMetaModelJson("set")
        val expectedErrors = listOf(
            "Package 1 entity 0 field 0 cannot be a 'set'. Only compositions can be sets.",
            "Package 1 entity 0 field 1 cannot be a 'set'. Only compositions can be sets.",
            "Package 1 entity 0 field 2 cannot be a 'set'. Only compositions can be sets."
        )
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
        |           "number": 1,
        |           "type": "string"
        |           $multiplicityJson
        |         },
        |         {
        |           "name": "enumeration_field",
        |           "number": 2,
        |           "type": "enumeration",
        |           "enumeration": {
        |             "name": "enumeration1",
        |             "package": "test.common"
        |           }
        |           $multiplicityJson
        |         },
        |         {
        |           "name": "association_field",
        |           "number": 3,
        |           "type": "association",
        |           "association": {
        |             "entity": "entity2",
        |             "package": "test.common"
        |           }
        |           $multiplicityJson
        |         },
        |         {
        |           "name": "composition_field",
        |           "number": 4,
        |           "type": "composition",
        |           "composition": {
        |             "entity": "entity3",
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
