package org.treeWare.model.metaModel.validation

import org.treeWare.metaModel.assertJsonStringValidationErrors
import org.treeWare.metaModel.newTestMetaModelJson
import kotlin.test.Test

class CompositionValidationTests {
    @Test
    fun `Composition must have entity info`() {
        val testPackageJson = """
            | {
            |   "name": "test.main",
            |   "entities": [
            |     {
            |       "name": "test_entity",
            |       "fields": [
            |         {
            |           "name": "test_field",
            |           "type": "entity"
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson = newTestMetaModelJson(testHelperRootJson(), testHelperPackageJson(), testPackageJson)
        val expectedErrors = listOf("Package 1 entity 0 field 0 entity info is missing")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Composition must have non-empty entity info`() {
        val testPackageJson = """
            | {
            |   "name": "test.main",
            |   "entities": [
            |     {
            |       "name": "test_entity",
            |       "fields": [
            |         {
            |           "name": "test_field",
            |           "type": "entity",
            |           "entity": {}
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson = newTestMetaModelJson(testHelperRootJson(), testHelperPackageJson(), testPackageJson)
        val expectedErrors = listOf(
            "Package 1 entity 0 field 0 entity info name is missing",
            "Package 1 entity 0 field 0 entity info package is missing"
        )
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
            |           "type": "entity",
            |           "entity": {
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
            "Target of composition key does not have only primitive keys: /test.package/test_entity/test_field"
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
            |           "type": "entity",
            |           "entity": {
            |             "name": "entity_with_composition_key",
            |             "package": "test.helper"
            |           },
            |           "is_key": true
            |         },
            |         {
            |           "name": "test_field_2",
            |           "type": "entity",
            |           "entity": {
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
            "Target of composition key does not have only primitive keys: /test.package/test_entity/test_field_1",
            "Target of composition key does not have only primitive keys: /test.package/test_entity/test_field_2"
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
            |           "type": "entity",
            |           "entity": {
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
    fun `Composition list target entity must have keys`() {
        val testPackageJson = """
            | {
            |   "name": "test.main",
            |   "entities": [
            |     {
            |       "name": "test_entity",
            |       "fields": [
            |         {
            |           "name": "test_field",
            |           "type": "entity",
            |           "entity": {
            |             "name": "entity_with_no_keys",
            |             "package": "test.helper"
            |           },
            |           "multiplicity": "list"
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson = newTestMetaModelJson(testHelperRootJson(), testHelperPackageJson(), testPackageJson)
        val expectedErrors = listOf(
            "Target of composition list does not have keys: /test.package/test_entity/test_field"
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Composition list is valid if target entity has keys`() {
        val testPackageJson = """
            | {
            |   "name": "test.main",
            |   "entities": [
            |     {
            |       "name": "test_entity",
            |       "fields": [
            |         {
            |           "name": "test_field",
            |           "type": "entity",
            |           "entity": {
            |             "name": "entity_with_primitive_and_composition_keys",
            |             "package": "test.helper"
            |           },
            |           "multiplicity": "list"
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
    |   "entity": "entity_with_no_keys",
    |   "package": "test.helper"
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
    |           "type": "boolean"
    |         }
    |       ]
    |     },
    |     {
    |       "name": "entity_with_composition_key",
    |       "fields": [
    |         {
    |           "name": "composition_field",
    |           "type": "entity",
    |           "entity": {
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
    |           "type": "boolean",
    |           "is_key": true
    |         },
    |         {
    |           "name": "composition_field",
    |           "type": "entity",
    |           "entity": {
    |             "name": "entity_with_only_primitive_keys",
    |             "package": "test.helper"
    |           },
    |           "is_key": true
    |         }
    |       ]
    |     },
    |     {
    |       "name": "entity_with_only_primitive_keys",
    |       "fields": [
    |         {
    |           "name": "key_string_field",
    |           "type": "string",
    |           "is_key": true
    |         },
    |         {
    |           "name": "non_key_string_field",
    |           "type": "string"
    |         },
    |         {
    |           "name": "non_key_boolean_field",
    |           "type": "boolean"
    |         }
    |       ]
    |     }
    |   ]
    | }
""".trimMargin()
