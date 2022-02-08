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
    |   "name": "root",
    |   "type": "composition",
    |   "composition": {
    |     "name": "valid_entity_name",
    |     "package": "hyphens-not-allowed-for-packages"
    |   }
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
    |           "type": "string",
    |           "is_key": true
    |         },
    |         {
    |           "name": "hyphens-not-allowed-for-primitive-fields",
    |           "type": "string"
    |         }
    |       ]
    |     },
    |     {
    |       "name": "hyphens-not-allowed-for-entities",
    |       "fields": [
    |         {
    |           "name": "dots.not_allowed_for.association_fields",
    |           "type": "association",
    |           "association": [
    |             {
    |               "value": "root"
    |             },
    |             {
    |               "value": "duplicate_field_name_2"
    |             }
    |           ]
    |         },
    |         {
    |           "name": "hyphens-not-allowed-for-association-fields",
    |           "type": "association",
    |           "association": [
    |             {
    |               "value": "root"
    |             },
    |             {
    |               "value": "duplicate_field_name_2"
    |             }
    |           ]
    |         }
    |       ]
    |     },
    |     {
    |       "name": "valid_entity_name",
    |       "fields": [
    |         {
    |           "name": "duplicate_field_name_1",
    |           "type": "string"
    |         },
    |         {
    |           "name": "duplicate_field_name_1",
    |           "type": "association",
    |           "association": [
    |             {
    |               "value": "root"
    |             },
    |             {
    |               "value": "duplicate_field_name_2"
    |             }
    |           ]
    |         },
    |         {
    |           "name": "duplicate_field_name_2",
    |           "type": "composition",
    |           "composition": {
    |             "name": "dots.not_allowed_for.entities",
    |             "package": "hyphens-not-allowed-for-packages"
    |           }
    |         },
    |         {
    |           "name": "duplicate_field_name_2",
    |           "type": "composition",
    |           "composition": {
    |             "name": "hyphens-not-allowed-for-entities",
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
    |           "name": "value1"
    |         },
    |         {
    |           "name": "value2"
    |         }
    |       ]
    |     },
    |     {
    |       "name": "hyphens-not-allowed-for-enumerations",
    |       "values": [
    |         {
    |           "name": "value1"
    |         },
    |         {
    |           "name": "value2"
    |         }
    |       ]
    |     },
    |     {
    |       "name": "duplicate_enumeration_name",
    |       "values": [
    |         {
    |           "name": "value1"
    |         },
    |         {
    |           "name": "value2"
    |         }
    |       ]
    |     },
    |     {
    |       "name": "duplicate_enumeration_name",
    |       "values": [
    |         {
    |           "name": "value1"
    |         },
    |         {
    |           "name": "value2"
    |         }
    |       ]
    |     },
    |     {
    |       "name": "enumeration_with_duplicate_values",
    |       "values": [
    |         {
    |           "name": "value1"
    |         },
    |         {
    |           "name": "duplicate_value"
    |         },
    |         {
    |           "name": "value2"
    |         },
    |         {
    |           "name": "duplicate_value"
    |         }
    |       ]
    |     }
    |   ]
    | }
""".trimMargin()
