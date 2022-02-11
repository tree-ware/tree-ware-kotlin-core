package org.treeWare.metaModel.validation

import org.treeWare.metaModel.assertJsonStringValidationErrors
import org.treeWare.metaModel.newTestMetaModelJson
import kotlin.test.Test

class CompositionValidationTests {
    @Test
    fun `Composition must have info`() {
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
            |           "type": "composition"
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson = newTestMetaModelJson(testHelperRootJson(), testHelperPackageJson(), testPackageJson)
        val expectedErrors = listOf("Package 1 entity 0 field 0 composition info is missing")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Composition must have non-empty info`() {
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
            |           "type": "composition",
            |           "composition": {}
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson = newTestMetaModelJson(testHelperRootJson(), testHelperPackageJson(), testPackageJson)
        val expectedErrors = listOf(
            "Package 1 entity 0 field 0 composition info name is missing",
            "Package 1 entity 0 field 0 composition info package is missing"
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Composition must refer to a defined entity`() {
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
            |           "type": "composition",
            |           "composition": {
            |             "name": "undefined_entity",
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
    fun `Composition must be valid if info can be resolved`() {
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
            |           "type": "composition",
            |           "composition": {
            |             "name": "entity_with_no_keys",
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
    fun `Composition key target entity must have keys`() {
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
            |           "type": "composition",
            |           "composition": {
            |             "name": "entity_with_no_keys",
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
        val expectedErrors = listOf(
            "Composition key field /test.main/test_entity/test_field target entity does not have only primitive keys"
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Composition key target entity must not have composition keys`() {
        val testPackageJson = """
            | {
            |   "name": "test.main",
            |   "entities": [
            |     {
            |       "name": "test_entity",
            |       "fields": [
            |         {
            |           "name": "test_field_1",
            |           "number": 1,
            |           "type": "composition",
            |           "composition": {
            |             "name": "entity_with_composition_key",
            |             "package": "test.helper"
            |           },
            |           "is_key": true
            |         },
            |         {
            |           "name": "test_field_2",
            |           "number": 2,
            |           "type": "composition",
            |           "composition": {
            |             "name": "entity_with_primitive_and_composition_keys",
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
        val expectedErrors = listOf(
            "Composition key field /test.main/test_entity/test_field_1 target entity does not have only primitive keys",
            "Composition key field /test.main/test_entity/test_field_2 target entity does not have only primitive keys"
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Composition key target entity must not have non-key fields`() {
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
            |           "type": "composition",
            |           "composition": {
            |             "name": "entity_with_primitive_keys_and_non_keys",
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
        val expectedErrors = listOf(
            "Composition key field /test.main/test_entity/test_field target entity does not have only primitive keys"
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Composition key is valid if target entity has only primitive keys`() {
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
            |           "type": "composition",
            |           "composition": {
            |             "name": "entity_with_only_primitive_keys",
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
        val expectedErrors = listOf<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Composition set target entity must have keys`() {
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
            |           "type": "composition",
            |           "composition": {
            |             "name": "entity_with_no_keys",
            |             "package": "test.helper"
            |           },
            |           "multiplicity": "set"
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson = newTestMetaModelJson(testHelperRootJson(), testHelperPackageJson(), testPackageJson)
        val expectedErrors = listOf(
            "Composition set field /test.main/test_entity/test_field target entity does not have keys"
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Composition set is valid if target entity has keys`() {
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
            |           "type": "composition",
            |           "composition": {
            |             "name": "entity_with_primitive_and_composition_keys",
            |             "package": "test.helper"
            |           },
            |           "multiplicity": "set"
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
}

private fun testHelperRootJson() = """
    | "root": {
    |   "name": "root",
    |   "type": "composition",
    |   "composition": {
    |     "name": "entity_with_no_keys",
    |     "package": "test.helper"
    |   }
    | }
""".trimMargin()

private fun testHelperPackageJson() = """
    | {
    |   "name": "test.helper",
    |   "entities": [
    |     {
    |       "name": "entity_with_no_keys",
    |       "fields": [
    |         {
    |           "name": "boolean_field",
    |           "number": 1,
    |           "type": "boolean"
    |         }
    |       ]
    |     },
    |     {
    |       "name": "entity_with_composition_key",
    |       "fields": [
    |         {
    |           "name": "composition_field",
    |           "number": 1,
    |           "type": "composition",
    |           "composition": {
    |             "name": "entity_with_only_primitive_keys",
    |             "package": "test.helper"
    |           }
    |         }
    |       ]
    |     },
    |     {
    |       "name": "entity_with_primitive_and_composition_keys",
    |       "fields": [
    |         {
    |           "name": "boolean_field",
    |           "number": 1,
    |           "type": "boolean",
    |           "is_key": true
    |         },
    |         {
    |           "name": "composition_field",
    |           "number": 2,
    |           "type": "composition",
    |           "composition": {
    |             "name": "entity_with_only_primitive_keys",
    |             "package": "test.helper"
    |           },
    |           "is_key": true
    |         }
    |       ]
    |     },
    |     {
    |       "name": "entity_with_primitive_keys_and_non_keys",
    |       "fields": [
    |         {
    |           "name": "key_string_field",
    |           "number": 1,
    |           "type": "string",
    |           "is_key": true
    |         },
    |         {
    |           "name": "non_key_string_field",
    |           "number": 2,
    |           "type": "string"
    |         },
    |         {
    |           "name": "non_key_boolean_field",
    |           "number": 3,
    |           "type": "boolean"
    |         },
    |         {
    |           "name": "non_key_composition_field",
    |           "number": 4,
    |           "type": "composition",
    |           "composition": {
    |             "name": "entity_with_only_primitive_keys",
    |             "package": "test.helper"
    |           }
    |         }
    |       ]
    |     },
    |     {
    |       "name": "entity_with_only_primitive_keys",
    |       "fields": [
    |         {
    |           "name": "key_string_field",
    |           "number": 1,
    |           "type": "string",
    |           "is_key": true
    |         },
    |         {
    |           "name": "key_boolean_field",
    |           "number": 2,
    |           "type": "boolean",
    |           "is_key": true
    |         }
    |       ]
    |     }
    |   ]
    | }
""".trimMargin()
