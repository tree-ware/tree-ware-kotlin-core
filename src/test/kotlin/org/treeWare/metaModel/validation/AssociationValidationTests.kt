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
            |           "type": "association",
            |           "association": []
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson = newTestMetaModelJson(testHelperRootJson(), testHelperPackageJson(), testPackageJson)
        val expectedErrors = listOf("Package 1 entity 0 field 0 association info is empty")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Association must specify more than a root`() {
        val testPackageJson = """
            | {
            |   "name": "test.main",
            |   "entities": [
            |     {
            |       "name": "test_entity",
            |       "fields": [
            |         {
            |           "name": "test_field",
            |           "type": "association",
            |           "association": [
            |             {
            |               "value": "root"
            |             }
            |           ]
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson = newTestMetaModelJson(testHelperRootJson(), testHelperPackageJson(), testPackageJson)
        val expectedErrors = listOf("Association field /test.main/test_entity/test_field has an insufficient path")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Association must specify a valid root`() {
        val testPackageJson = """
            | {
            |   "name": "test.main",
            |   "entities": [
            |     {
            |       "name": "test_entity",
            |       "fields": [
            |         {
            |           "name": "test_field",
            |           "type": "association",
            |           "association": [
            |             {
            |               "value": "invalid_root"
            |             },
            |             {
            |               "value": "invalid_field_1"
            |             }
            |           ]
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson = newTestMetaModelJson(testHelperRootJson(), testHelperPackageJson(), testPackageJson)
        val expectedErrors = listOf("Association field /test.main/test_entity/test_field has an invalid path root")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Association must specify a valid path`() {
        val testPackageJson = """
            | {
            |   "name": "test.main",
            |   "entities": [
            |     {
            |       "name": "test_entity",
            |       "fields": [
            |         {
            |           "name": "test_field",
            |           "type": "association",
            |           "association": [
            |             {
            |               "value": "root"
            |             },
            |             {
            |               "value": "invalid_field_1"
            |             }
            |           ]
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson = newTestMetaModelJson(testHelperRootJson(), testHelperPackageJson(), testPackageJson)
        val expectedErrors =
            listOf("Association field /test.main/test_entity/test_field has an invalid path element invalid_field_1")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Association must refer to an entity`() {
        val testPackageJson = """
            | {
            |   "name": "test.main",
            |   "entities": [
            |     {
            |       "name": "test_entity",
            |       "fields": [
            |         {
            |           "name": "test_field",
            |           "type": "association",
            |           "association": [
            |             {
            |               "value": "root"
            |             },
            |             {
            |               "value": "entity1_composition_field2"
            |             },
            |             {
            |               "value": "int_field"
            |             }
            |           ]
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson = newTestMetaModelJson(testHelperRootJson(), testHelperPackageJson(), testPackageJson)
        val expectedErrors =
            listOf("Association field /test.main/test_entity/test_field path element int_field is not an entity")
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
            |           "type": "association",
            |           "association": [
            |             {
            |               "value": "root"
            |             },
            |             {
            |               "value": "entity1_composition_field1"
            |             },
            |             {
            |               "value": "entity2_composition_field"
            |             }
            |           ]
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
            |           "type": "association",
            |           "association": [
            |             {
            |               "value": "root"
            |             },
            |             {
            |               "value": "entity1_composition_field1"
            |             }
            |           ],
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

    @Test
    fun `Association list path must have keys`() {
        val testPackageJson = """
            | {
            |   "name": "test.main",
            |   "entities": [
            |     {
            |       "name": "test_entity",
            |       "fields": [
            |         {
            |           "name": "test_field",
            |           "type": "association",
            |           "association": [
            |             {
            |               "value": "root"
            |             },
            |             {
            |               "value": "entity1_composition_field1"
            |             }
            |           ],
            |           "multiplicity": "list"
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson = newTestMetaModelJson(testHelperRootJson(), testHelperPackageJson(), testPackageJson)
        val expectedErrors = listOf(
            "Association list field /test.main/test_entity/test_field path does not have keys"
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Association list must be valid if path has keys`() {
        val testPackageJson = """
            | {
            |   "name": "test.main",
            |   "entities": [
            |     {
            |       "name": "test_entity",
            |       "fields": [
            |         {
            |           "name": "test_field",
            |           "type": "association",
            |           "association": [
            |             {
            |               "value": "root"
            |             },
            |             {
            |               "value": "entity1_composition_field1"
            |             },
            |             {
            |               "value": "entity2_composition_field"
            |             }
            |           ],
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
    |   "type": "composition",
    |   "composition": {
    |     "name": "entity1",
    |     "package": "test.helper"
    |   }
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
    |           "type": "composition",
    |           "composition": {
    |             "name": "entity2",
    |             "package": "test.helper"
    |           }
    |         },
    |         {
    |           "name": "entity1_composition_field2",
    |           "type": "composition",
    |           "composition": {
    |             "name": "entity3",
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
    |           "type": "composition",
    |           "composition": {
    |             "name": "entity3",
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
    |           "type": "int",
    |           "is_key": true
    |         }
    |       ]
    |     }
    |   ]
    | }
""".trimMargin()
