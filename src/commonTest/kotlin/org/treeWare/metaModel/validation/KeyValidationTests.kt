package org.treeWare.metaModel.validation

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
    fun `Multiplicity must not be 'set' for key fields`() {
        val metaModelJson = getCompositionSetMetaModelJson()
        val expectedErrors = listOf("Package 1 entity 0 field 0 is a key but not defined as required")
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

    @Test
    fun `Associations must not be key fields`() {
        val metaModelJson = getAssociationMetaModelJson(true)
        val expectedErrors = listOf("Package 1 entity 0 field 0 is an association field and they cannot be keys")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Passwords must not be key fields`() {
        val metaModelJson = getPasswordsMetaModelJson(true)
        val expectedErrors = listOf(
            "Package 1 entity 0 field 0 is a password field and they cannot be keys",
            "Package 1 entity 0 field 1 is a password field and they cannot be keys"
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }
}

// NOTE: association fields cannot be keys, so they are not included below.
// AssociationValidationTests ensures that associations cannot be keys.
private fun getMetaModelJson(multiplicity: String?): String {
    val multiplicityJson = getMultiplicityJson(multiplicity)
    val mainPackageJson = """
        | {
        |   "name": "org.tree_ware.test.main",
        |   "entities": [
        |     {
        |       "name": "main_entity1",
        |       "fields": [
        |         {
        |           "name": "primitive_field",
        |           "number": 1,
        |           "type": "string",
        |           "is_key": true
        |           $multiplicityJson
        |         },
        |         {
        |           "name": "enumeration_field",
        |           "number": 2,
        |           "type": "enumeration",
        |           "enumeration": {
        |             "name": "enumeration1",
        |             "package": "org.tree_ware.test.common"
        |           },
        |           "is_key": true
        |           $multiplicityJson
        |         },
        |         {
        |           "name": "composition_field",
        |           "number": 3,
        |           "type": "composition",
        |           "composition": {
        |             "entity": "entity3",
        |             "package": "org.tree_ware.test.common"
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

// NOTE: Currently only compositions can be sets, hence getMetaModelJson() cannot be used in some tests.
private fun getCompositionSetMetaModelJson(): String {
    val multiplicityJson = getMultiplicityJson("set")
    val mainPackageJson = """
        | {
        |   "name": "org.tree_ware.test.main",
        |   "entities": [
        |     {
        |       "name": "main_entity1",
        |       "fields": [
        |         {
        |           "name": "composition_field",
        |           "number": 1,
        |           "type": "composition",
        |           "composition": {
        |             "entity": "entity3",
        |             "package": "org.tree_ware.test.common"
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

private fun getAssociationMetaModelJson(isKey: Boolean): String {
    val mainPackageJson = """
        | {
        |   "name": "org.tree_ware.test.main",
        |   "entities": [
        |     {
        |       "name": "main_entity1",
        |       "fields": [
        |         {
        |           "name": "association_field",
        |           "number": 1,
        |           "type": "association",
        |           "association": {
        |             "entity": "entity3",
        |             "package": "org.tree_ware.test.common"
        |           },
        |           "is_key": $isKey
        |         }
        |       ]
        |     }
        |   ]
        | }
    """.trimMargin()
    return newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, mainPackageJson)
}

private fun getPasswordsMetaModelJson(isKey: Boolean): String {
    val mainPackageJson = """
        | {
        |   "name": "org.tree_ware.test.main",
        |   "entities": [
        |     {
        |       "name": "main_entity1",
        |       "fields": [
        |         {
        |           "name": "password1way_field",
        |           "number": 1,
        |           "type": "password1way",
        |           "is_key": $isKey
        |         },
        |         {
        |           "name": "password2way_field",
        |           "number": 2,
        |           "type": "password2way",
        |           "is_key": $isKey
        |         }
        |       ]
        |     }
        |   ]
        | }
    """.trimMargin()
    return newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, mainPackageJson)
}
