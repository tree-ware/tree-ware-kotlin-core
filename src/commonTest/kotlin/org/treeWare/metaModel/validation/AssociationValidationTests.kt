package org.treeWare.metaModel.validation

import org.treeWare.metaModel.assertJsonStringValidationErrors
import org.treeWare.metaModel.newTestMetaModelJson
import kotlin.test.Test

class AssociationValidationTests {
    @Test
    fun `Association must have info`() {
        val testPackageJson = """
            | {
            |   "name": "test.main",
            |   "entities": [
            |     {
            |       "name": "test_entity",
            |       "fields": [
            |         {
            |           "name": "test_field",
            |           "number": 1,
            |           "type": "association"
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson = newTestMetaModelJson(testHelperRootJson(), testHelperPackageJson(), testPackageJson)
        val expectedErrors = listOf("Package 1 entity 0 field 0 association info is missing")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Association must have non-empty info`() {
        val testPackageJson = """
            | {
            |   "name": "test.main",
            |   "entities": [
            |     {
            |       "name": "test_entity",
            |       "fields": [
            |         {
            |           "name": "test_field",
            |           "number": 1,
            |           "type": "association",
            |           "association": {}
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson = newTestMetaModelJson(testHelperRootJson(), testHelperPackageJson(), testPackageJson)
        val expectedErrors = listOf(
            "Package 1 entity 0 field 0 association info entity is missing",
            "Package 1 entity 0 field 0 association info package is missing"
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Association must refer to a defined entity`() {
        val testPackageJson = """
            | {
            |   "name": "test.main",
            |   "entities": [
            |     {
            |       "name": "test_entity",
            |       "fields": [
            |         {
            |           "name": "test_field",
            |           "number": 1,
            |           "type": "association",
            |           "association": {
            |             "entity": "undefined_entity",
            |             "package": "test.helper"
            |           }
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson = newTestMetaModelJson(testHelperRootJson(), testHelperPackageJson(), testPackageJson)
        val expectedErrors = listOf("Entity /test.helper/undefined_entity cannot be resolved")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Association must be valid if info can be resolved`() {
        val testPackageJson = """
            | {
            |   "name": "test.main",
            |   "entities": [
            |     {
            |       "name": "test_entity",
            |       "fields": [
            |         {
            |           "name": "test_field",
            |           "number": 1,
            |           "type": "association",
            |           "association": {
            |             "entity": "entity3",
            |             "package": "test.helper"
            |           }
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson = newTestMetaModelJson(testHelperRootJson(), testHelperPackageJson(), testPackageJson)
        val expectedErrors = listOf<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Association must not be a key`() {
        val testPackageJson = """
            | {
            |   "name": "test.main",
            |   "entities": [
            |     {
            |       "name": "test_entity",
            |       "fields": [
            |         {
            |           "name": "test_field",
            |           "number": 1,
            |           "type": "association",
            |           "association": {
            |             "entity": "entity3",
            |             "package": "test.helper"
            |           },
            |           "is_key": true
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson = newTestMetaModelJson(testHelperRootJson(), testHelperPackageJson(), testPackageJson)
        val expectedErrors = listOf("Package 1 entity 0 field 0 is an association field and they cannot be keys")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }
}

private fun testHelperRootJson() = """
    | "root": {
    |   "entity": "entity1",
    |   "package": "test.helper"
    | }
""".trimMargin()

private fun testHelperPackageJson() = """
    | {
    |   "name": "test.helper",
    |   "entities": [
    |     {
    |       "name": "entity1",
    |       "fields": [
    |         {
    |           "name": "entity1_composition_field1",
    |           "number": 1,
    |           "type": "composition",
    |           "composition": {
    |             "entity": "entity2",
    |             "package": "test.helper"
    |           }
    |         },
    |         {
    |           "name": "entity1_composition_field2",
    |           "number": 2,
    |           "type": "composition",
    |           "composition": {
    |             "entity": "entity3",
    |             "package": "test.helper"
    |           },
    |           "multiplicity": "set"
    |         }
    |       ]
    |     },
    |     {
    |       "name": "entity2",
    |       "fields": [
    |         {
    |           "name": "entity2_composition_field",
    |           "number": 1,
    |           "type": "composition",
    |           "composition": {
    |             "entity": "entity3",
    |             "package": "test.helper"
    |           },
    |           "multiplicity": "set"
    |         }
    |       ]
    |     },
    |     {
    |       "name": "entity3",
    |       "fields": [
    |         {
    |           "name": "int_field",
    |           "number": 1,
    |           "type": "int32",
    |           "is_key": true
    |         }
    |       ]
    |     }
    |   ]
    | }
""".trimMargin()
