package org.treeWare.metaModel.validation

import org.treeWare.metaModel.assertJsonStringValidationErrors
import org.treeWare.metaModel.newTestMetaModelJson
import kotlin.test.Test

class NameValidationTests {
    @Test
    fun `Names must be valid`() {
        val metaModelJson = newTestMetaModelJson(testHelperRootJson(), testHelperPackageJson())
        val expectedDecodeErrors = listOf(
            "Entity with duplicate keys: /tree_ware_meta_model.main/field: [duplicate_field_name_1]",
            "Entity with duplicate keys: /tree_ware_meta_model.main/field: [duplicate_field_name_2]",
            "Entity with duplicate keys: /tree_ware_meta_model.main/enumeration: [duplicate_enumeration_name]",
            "Entity with duplicate keys: /tree_ware_meta_model.main/enumeration_value: [duplicate_value]"
        )
        val expectedErrors = listOf(
            "Invalid name: /hyphens-not-allowed-for-packages",
            "Invalid name: /hyphens-not-allowed-for-packages/dots.not_allowed_for.enumerations",
            "Invalid name: /hyphens-not-allowed-for-packages/hyphens-not-allowed-for-enumerations",
            "Invalid name: /hyphens-not-allowed-for-packages/dots.not_allowed_for.entities",
            "Invalid name: /hyphens-not-allowed-for-packages/dots.not_allowed_for.entities/dots.not_allowed_for.primitive_fields",
            "Invalid name: /hyphens-not-allowed-for-packages/dots.not_allowed_for.entities/hyphens-not-allowed-for-primitive-fields",
            "Invalid name: /hyphens-not-allowed-for-packages/hyphens-not-allowed-for-entities",
            "Invalid name: /hyphens-not-allowed-for-packages/hyphens-not-allowed-for-entities/dots.not_allowed_for.association_fields",
            "Invalid name: /hyphens-not-allowed-for-packages/hyphens-not-allowed-for-entities/hyphens-not-allowed-for-association-fields"
        )
        assertJsonStringValidationErrors(metaModelJson, expectedErrors, expectedDecodeErrors)
    }
}

private fun testHelperRootJson() = """
    | "root": {
    |   "entity": "valid_entity_name",
    |   "package": "hyphens-not-allowed-for-packages"
    | }
""".trimMargin()

private fun testHelperPackageJson() = """
    | {
    |   "name": "hyphens-not-allowed-for-packages",
    |   "entities": [
    |     {
    |       "name": "dots.not_allowed_for.entities",
    |       "fields": [
    |         {
    |           "name": "dots.not_allowed_for.primitive_fields",
    |           "number": 1,
    |           "type": "string",
    |           "is_key": true
    |         },
    |         {
    |           "name": "hyphens-not-allowed-for-primitive-fields",
    |           "number": 2,
    |           "type": "string"
    |         }
    |       ]
    |     },
    |     {
    |       "name": "hyphens-not-allowed-for-entities",
    |       "fields": [
    |         {
    |           "name": "dots.not_allowed_for.association_fields",
    |           "number": 1,
    |           "type": "association",
    |           "association": {
    |             "entity": "dots.not_allowed_for.entities",
    |             "package": "hyphens-not-allowed-for-packages"
    |           }
    |         },
    |         {
    |           "name": "hyphens-not-allowed-for-association-fields",
    |           "number": 2,
    |           "type": "association",
    |           "association": {
    |             "entity": "dots.not_allowed_for.entities",
    |             "package": "hyphens-not-allowed-for-packages"
    |           }
    |         }
    |       ]
    |     },
    |     {
    |       "name": "valid_entity_name",
    |       "fields": [
    |         {
    |           "name": "duplicate_field_name_1",
    |           "number": 1,
    |           "type": "string"
    |         },
    |         {
    |           "name": "duplicate_field_name_1",
    |           "number": 2,
    |           "type": "association",
    |           "association": {
    |             "entity": "dots.not_allowed_for.entities",
    |             "package": "hyphens-not-allowed-for-packages"
    |           }
    |         },
    |         {
    |           "name": "duplicate_field_name_2",
    |           "number": 3,
    |           "type": "composition",
    |           "composition": {
    |             "entity": "dots.not_allowed_for.entities",
    |             "package": "hyphens-not-allowed-for-packages"
    |           }
    |         },
    |         {
    |           "name": "duplicate_field_name_2",
    |           "number": 4,
    |           "type": "composition",
    |           "composition": {
    |             "entity": "hyphens-not-allowed-for-entities",
    |             "package": "hyphens-not-allowed-for-packages"
    |           }
    |         }
    |       ]
    |     }
    |   ],
    |   "enumerations": [
    |     {
    |       "name": "dots.not_allowed_for.enumerations",
    |       "values": [
    |         {
    |           "name": "value1",
    |           "number": 0
    |         },
    |         {
    |           "name": "value2",
    |           "number": 1
    |         }
    |       ]
    |     },
    |     {
    |       "name": "hyphens-not-allowed-for-enumerations",
    |       "values": [
    |         {
    |           "name": "value1",
    |           "number": 0
    |         },
    |         {
    |           "name": "value2",
    |           "number": 1
    |         }
    |       ]
    |     },
    |     {
    |       "name": "duplicate_enumeration_name",
    |       "values": [
    |         {
    |           "name": "value1",
    |           "number": 0
    |         },
    |         {
    |           "name": "value2",
    |           "number": 1
    |         }
    |       ]
    |     },
    |     {
    |       "name": "duplicate_enumeration_name",
    |       "values": [
    |         {
    |           "name": "value1",
    |           "number": 0
    |         },
    |         {
    |           "name": "value2",
    |           "number": 1
    |         }
    |       ]
    |     },
    |     {
    |       "name": "enumeration_with_duplicate_values",
    |       "values": [
    |         {
    |           "name": "value1",
    |           "number": 0
    |         },
    |         {
    |           "name": "duplicate_value",
    |           "number": 1
    |         },
    |         {
    |           "name": "value2",
    |           "number": 2
    |         },
    |         {
    |           "name": "duplicate_value",
    |           "number": 3
    |         }
    |       ]
    |     }
    |   ]
    | }
""".trimMargin()
