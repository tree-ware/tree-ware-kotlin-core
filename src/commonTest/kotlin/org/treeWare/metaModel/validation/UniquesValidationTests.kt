package org.treeWare.metaModel.validation

import org.treeWare.metaModel.assertJsonStringValidationErrors
import org.treeWare.metaModel.newTestMetaModelJson
import org.treeWare.metaModel.testMetaModelCommonPackageJson
import org.treeWare.metaModel.testMetaModelCommonRootJson
import kotlin.test.Test

class UniquesValidationTests {
    @Test
    fun `Uniques names must be unique`() {
        val testPackageJson = """
            | {
            |   "name": "iot",
            |   "entities": [
            |     {
            |       "name": "device",
            |       "fields": [
            |         {
            |           "name": "id",
            |           "number": 1,
            |           "type": "uuid",
            |           "is_key": true
            |         },
            |         {
            |           "name": "serial_number",
            |           "number": 2,
            |           "type": "string"
            |         },
            |         {
            |           "name": "mac_address",
            |           "number": 3,
            |           "type": "string"
            |         }
            |       ],
            |       "uniques": [
            |         {
            |           "name": "duplicate",
            |           "type": "global",
            |           "fields": [
            |             {
            |               "name": "serial_number"
            |             }
            |           ]
            |         },
            |         {
            |           "name": "duplicate",
            |           "fields": [
            |             {
            |               "name": "mac_address"
            |             }
            |           ]
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson =
            newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, testPackageJson)
        val expectedDecodeErrors =
            listOf("Entity with duplicate keys: /org.tree_ware.meta_model.main/unique: [duplicate]")
        assertJsonStringValidationErrors(metaModelJson, emptyList(), expectedDecodeErrors)
    }

    @Test
    fun `Uniques must have a valid type`() {
        val testPackageJson = """
            | {
            |   "name": "iot",
            |   "entities": [
            |     {
            |       "name": "device",
            |       "fields": [
            |         {
            |           "name": "id",
            |           "number": 1,
            |           "type": "uuid",
            |           "is_key": true
            |         },
            |         {
            |           "name": "serial_number",
            |           "number": 2,
            |           "type": "string"
            |         }
            |       ],
            |       "uniques": [
            |         {
            |           "name": "serial",
            |           "type": "invalid",
            |           "fields": [
            |             {
            |               "name": "serial_number"
            |             }
            |           ]
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson =
            newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, testPackageJson)
        val expectedDecodeErrors = listOf("JSON decoding failed at line 109 column 20, 2145 characters from the start")
        val expectedValidationErrors = listOf("Meta-model decoding failed")
        assertJsonStringValidationErrors(metaModelJson, expectedValidationErrors, expectedDecodeErrors)
    }

    @Test
    fun `Uniques fields must be specified`() {
        val testPackageJson = """
            | {
            |   "name": "iot",
            |   "entities": [
            |     {
            |       "name": "device",
            |       "fields": [
            |         {
            |           "name": "id",
            |           "number": 1,
            |           "type": "uuid",
            |           "is_key": true
            |         }
            |       ],
            |       "uniques": [
            |         {
            |           "name": "serial",
            |           "type": "global"
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson =
            newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, testPackageJson)
        val expectedErrors = listOf("Package 1 entity 0 unique 0 fields are missing")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Uniques fields must not be empty`() {
        val testPackageJson = """
            | {
            |   "name": "iot",
            |   "entities": [
            |     {
            |       "name": "device",
            |       "fields": [
            |         {
            |           "name": "id",
            |           "number": 1,
            |           "type": "uuid",
            |           "is_key": true
            |         }
            |       ],
            |       "uniques": [
            |         {
            |           "name": "serial",
            |           "type": "global",
            |           "fields": []
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson =
            newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, testPackageJson)
        val expectedErrors = listOf("Package 1 entity 0 unique 0 fields are empty")
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Uniques fields must be defined in entity`() {
        val testPackageJson = """
            | {
            |   "name": "iot",
            |   "entities": [
            |     {
            |       "name": "device",
            |       "fields": [
            |         {
            |           "name": "id",
            |           "number": 1,
            |           "type": "uuid",
            |           "is_key": true
            |         }
            |       ],
            |       "uniques": [
            |         {
            |           "name": "serial",
            |           "type": "global",
            |           "fields": [
            |             {
            |               "name": "not_defined_1"
            |             },
            |             {
            |               "name": "not_defined_2"
            |             }
            |           ]
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson =
            newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, testPackageJson)
        val expectedErrors = listOf(
            "Package 1 entity 0 unique 0 field 0 not found: not_defined_1",
            "Package 1 entity 0 unique 0 field 1 not found: not_defined_2",
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Uniques fields must not be collection fields`() {
        val testPackageJson = """
            | {
            |   "name": "iot",
            |   "entities": [
            |     {
            |       "name": "device",
            |       "fields": [
            |         {
            |           "name": "id",
            |           "number": 1,
            |           "type": "uuid",
            |           "is_key": true
            |         },
            |         {
            |           "name": "set_field",
            |           "number": 3,
            |           "type": "composition",
            |           "composition": {
            |             "entity": "entity2",
            |             "package": "org.tree_ware.test.common"
            |           },
            |           "multiplicity": "set"
            |         }
            |       ],
            |       "uniques": [
            |         {
            |           "name": "collections",
            |           "type": "global",
            |           "fields": [
            |             {
            |               "name": "set_field"
            |             }
            |           ]
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson =
            newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, testPackageJson)
        val expectedErrors = listOf(
            "Collection fields are not supported in uniques: Package 1 entity 0 unique 0 field 0: set_field"
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Uniques fields must not be composition fields`() {
        val testPackageJson = """
            | {
            |   "name": "iot",
            |   "entities": [
            |     {
            |       "name": "device",
            |       "fields": [
            |         {
            |           "name": "id",
            |           "number": 1,
            |           "type": "uuid",
            |           "is_key": true
            |         },
            |         {
            |           "name": "composition_field",
            |           "number": 2,
            |           "type": "composition",
            |           "composition": {
            |             "entity": "entity2",
            |             "package": "org.tree_ware.test.common"
            |           }
            |         }
            |       ],
            |       "uniques": [
            |         {
            |           "name": "composition",
            |           "type": "global",
            |           "fields": [
            |             {
            |               "name": "composition_field"
            |             }
            |           ]
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson =
            newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, testPackageJson)
        val expectedErrors = listOf(
            "Composition fields are not supported in uniques: Package 1 entity 0 unique 0 field 0: composition_field"
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }

    @Test
    fun `Uniques validation must pass for a valid definition`() {
        val testPackageJson = """
            | {
            |   "name": "iot",
            |   "entities": [
            |     {
            |       "name": "device",
            |       "fields": [
            |         {
            |           "name": "id",
            |           "number": 1,
            |           "type": "uuid",
            |           "is_key": true
            |         },
            |         {
            |           "name": "serial_number",
            |           "number": 2,
            |           "type": "string"
            |         },
            |         {
            |           "name": "association_field",
            |           "number": 3,
            |           "type": "association",
            |           "association": {
            |             "entity": "entity2",
            |             "package": "org.tree_ware.test.common"
            |           }
            |         },         
            |         {
            |           "name": "make",
            |           "number": 4,
            |           "type": "string"
            |         }
            |       ],
            |       "uniques": [
            |         {
            |           "name": "serial",
            |           "type": "global",
            |           "fields": [
            |             {
            |               "name": "make"
            |             },
            |             {
            |               "name": "serial_number"
            |             }
            |           ]
            |         },
            |         {
            |           "name": "association",
            |           "fields": [
            |             {
            |               "name": "association_field"
            |             }
            |           ]
            |         }
            |       ]
            |     }
            |   ]
            | }
        """.trimMargin()
        val metaModelJson =
            newTestMetaModelJson(testMetaModelCommonRootJson, testMetaModelCommonPackageJson, testPackageJson)
        val expectedErrors = emptyList<String>()
        assertJsonStringValidationErrors(metaModelJson, expectedErrors)
    }
}