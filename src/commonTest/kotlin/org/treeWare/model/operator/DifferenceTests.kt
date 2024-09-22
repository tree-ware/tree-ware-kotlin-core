package org.treeWare.model.operator

import org.treeWare.model.AddressBookMutableEntityModelFactory
import org.treeWare.model.assertMatchesJson
import org.treeWare.model.assertMatchesJsonString
import org.treeWare.model.core.EntityModel
import org.treeWare.model.decodeJsonStringIntoEntity
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.MultiAuxEncoder
import org.treeWare.util.readFile
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DifferenceTests {
    // region Primitive single-fields

    @Test
    fun must_not_detect_differences_for_identical_single_fields() {
        verifyDifference(
            old = """
            |{
            |  "name": "Test Book",
            |  "last_updated": "1587147731"
            |}""".trimMargin(),
            new = """
            |{
            |  "name": "Test Book",
            |  "last_updated": "1587147731"
            |}""".trimMargin(),
            expectedCreate = null,
            expectedDelete = null,
            expectedUpdate = null,
        )
    }

    @Test
    fun must_detect_created_primitive_single_fields() {
        verifyDifference(
            old = "{}",
            new = """
            |{
            |  "name": "Test Book",
            |  "last_updated": "1587147731"
            |}""".trimMargin(),
            expectedCreate = """
            |{
            |  "name": "Test Book",
            |  "last_updated": "1587147731"
            |}""".trimMargin(),
            expectedDelete = null,
            expectedUpdate = null,
        )
    }

    @Test
    fun must_detect_created_primitive_single_fields_amongst_unchanged_fields() {
        verifyDifference(
            old = """
            |{
            |  "name": "Test Book"
            |}""".trimMargin(),
            new = """
            |{
            |  "name": "Test Book",
            |  "last_updated": "1587147731"
            |}""".trimMargin(),
            expectedCreate = """
            |{
            |  "last_updated": "1587147731"
            |}""".trimMargin(),
            expectedDelete = null,
            expectedUpdate = null,
        )
    }

    @Test
    fun must_detect_deleted_primitive_single_fields() {
        verifyDifference(
            old = """
            |{
            |  "name": "Test Book",
            |  "last_updated": "1587147731"
            |}""".trimMargin(),
            new = "{}",
            expectedCreate = null,
            expectedDelete = """
            |{
            |  "name": "Test Book",
            |  "last_updated": "1587147731"
            |}""".trimMargin(),
            expectedUpdate = null,
        )
    }

    @Test
    fun must_detect_deleted_primitive_single_fields_amongst_unchanged_fields() {
        verifyDifference(
            old = """
            |{
            |  "name": "Test Book",
            |  "last_updated": "1587147731"
            |}""".trimMargin(),
            new = """
            |{
            |  "name": "Test Book"
            |}""".trimMargin(),
            expectedCreate = null,
            expectedDelete = """
            |{
            |  "last_updated": "1587147731"
            |}""".trimMargin(),
            expectedUpdate = null,
        )
    }

    @Test
    fun must_detect_updated_primitive_single_fields() {
        verifyDifference(
            old = """
            |{
            |  "name": "Test Book",
            |  "last_updated": "1587147731"
            |}""".trimMargin(),
            new = """
            |{
            |  "name": "Test Book 1",
            |  "last_updated": "1587147733"
            |}""".trimMargin(),
            expectedCreate = null,
            expectedDelete = null,
            expectedUpdate = """
            |{
            |  "name": "Test Book 1",
            |  "last_updated": "1587147733"
            |}""".trimMargin(),
        )
    }

    @Test
    fun must_detect_updated_primitive_single_fields_amongst_unchanged_fields() {
        verifyDifference(
            old = """
            |{
            |  "name": "Test Book",
            |  "last_updated": "1587147731"
            |}""".trimMargin(),
            new = """
            |{
            |  "name": "Test Book",
            |  "last_updated": "1587147733"
            |}""".trimMargin(),
            expectedCreate = null,
            expectedDelete = null,
            expectedUpdate = """
            |{
            |  "last_updated": "1587147733"
            |}""".trimMargin(),
        )
    }

    // endregion

    // region Composition single-field

    @Test
    fun must_not_detect_differences_for_identical_composition_single_field() {
        verifyDifference(
            old = """
            |{
            |    "settings": {
            |      "last_name_first": true,
            |      "encrypt_hero_name": false
            |    }
            |}""".trimMargin(),
            new = """
            |{
            |    "settings": {
            |      "last_name_first": true,
            |      "encrypt_hero_name": false
            |    }
            |}""".trimMargin(),
            expectedCreate = null,
            expectedDelete = null,
            expectedUpdate = null,
        )
    }

    @Test
    fun must_detect_created_composition_single_field() {
        verifyDifference(
            old = """
            |{
            |}""".trimMargin(),
            new = """
            |{
            |  "settings": {
            |    "last_name_first": true,
            |    "encrypt_hero_name": false
            |  }
            |}""".trimMargin(),
            expectedCreate = """
            |{
            |  "settings": {
            |    "last_name_first": true,
            |    "encrypt_hero_name": false
            |  }
            |}""".trimMargin(),
            expectedDelete = null,
            expectedUpdate = null,
        )
    }

    @Test
    fun must_detect_created_composition_single_field_amongst_unchanged_fields() {
        verifyDifference(
            old = """
            |{
            |  "last_updated": "1587147731"
            |}""".trimMargin(),
            new = """
            |{
            |  "last_updated": "1587147731",
            |  "settings": {
            |    "last_name_first": true,
            |    "encrypt_hero_name": false
            |  }
            |}""".trimMargin(),
            expectedCreate = """
            |{
            |  "settings": {
            |    "last_name_first": true,
            |    "encrypt_hero_name": false
            |  }
            |}""".trimMargin(),
            expectedDelete = null,
            expectedUpdate = null,
        )
    }

    @Test
    fun must_detect_deleted_composition_single_field() {
        verifyDifference(
            old = """
            |{
            |  "settings": {
            |    "last_name_first": true,
            |    "encrypt_hero_name": false
            |  }
            |}""".trimMargin(),
            new = """
            |{
            |}""".trimMargin(),
            expectedCreate = null,
            expectedDelete = """
            |{
            |  "settings": {
            |    "last_name_first": true,
            |    "encrypt_hero_name": false
            |  }
            |}""".trimMargin(),
            expectedUpdate = null,
        )
    }

    @Test
    fun must_detect_deleted_composition_single_field_amongst_unchanged_fields() {
        verifyDifference(
            old = """
            |{
            |  "last_updated": "1587147731",
            |  "settings": {
            |    "last_name_first": true,
            |    "encrypt_hero_name": false
            |  }
            |}""".trimMargin(),
            new = """
            |{
            |  "last_updated": "1587147731"
            |}""".trimMargin(),
            expectedCreate = null,
            expectedDelete = """
            |{
            |  "settings": {
            |    "last_name_first": true,
            |    "encrypt_hero_name": false
            |  }
            |}""".trimMargin(),
            expectedUpdate = null,
        )
    }

    @Test
    fun must_detect_updated_composition_single_field() {
        verifyDifference(
            old = """
            |{
            |  "settings": {
            |    "last_name_first": true,
            |    "encrypt_hero_name": false
            |  }
            |}""".trimMargin(),
            new = """
            |{
            |  "settings": {
            |    "last_name_first": false,
            |    "encrypt_hero_name": true
            |  }
            |}""".trimMargin(),
            expectedCreate = null,
            expectedDelete = null,
            expectedUpdate = """
            |{
            |  "settings": {
            |    "last_name_first": false,
            |    "encrypt_hero_name": true
            |  }
            |}""".trimMargin(),
        )
    }

    @Test
    fun must_detect_updated_composition_single_field_amongst_unchanged_fields() {
        verifyDifference(
            old = """
            |{
            |  "last_updated": "1587147731",
            |  "settings": {
            |    "last_name_first": true,
            |    "encrypt_hero_name": false
            |  }
            |}""".trimMargin(),
            new = """
            |{
            |  "last_updated": "1587147731",
            |  "settings": {
            |    "last_name_first": false,
            |    "encrypt_hero_name": true
            |  }
            |}""".trimMargin(),
            expectedCreate = null,
            expectedDelete = null,
            expectedUpdate = """
            |{
            |  "settings": {
            |    "last_name_first": false,
            |    "encrypt_hero_name": true
            |  }
            |}""".trimMargin(),
        )
    }

    // endregion

    // region Composition set-field

    @Test
    fun must_not_detect_differences_for_identical_composition_set_field() {
        verifyDifference(
            old = """
            |{
            |    "persons": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark"
            |      }
            |    ]
            |}""".trimMargin(),
            new = """
            |{
            |    "persons": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark"
            |      }
            |    ]
            |}""".trimMargin(),
            expectedCreate = null,
            expectedDelete = null,
            expectedUpdate = null,
        )
    }

    @Test
    fun must_detect_created_composition_set_field() {
        verifyDifference(
            old = """
            |{
            |}""".trimMargin(),
            new = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark"
            |    }
            |  ]
            |}""".trimMargin(),
            expectedCreate = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark"
            |    }
            |  ]
            |}""".trimMargin(),
            expectedDelete = null,
            expectedUpdate = null,
        )
    }

    @Test
    fun must_detect_created_composition_set_field_entity() {
        verifyDifference(
            old = """
            |{
            |  "persons": [
            |  ]
            |}""".trimMargin(),
            new = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark"
            |    }
            |  ]
            |}""".trimMargin(),
            expectedCreate = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark"
            |    }
            |  ]
            |}""".trimMargin(),
            expectedDelete = null,
            expectedUpdate = null,
        )
    }

    @Test
    fun must_detect_created_empty_composition_set_field() {
        verifyDifference(
            old = """
            |{
            |}""".trimMargin(),
            new = """
            |{
            |  "persons": []
            |}""".trimMargin(),
            expectedCreate = """
            |{
            |  "persons": []
            |}""".trimMargin(),
            expectedDelete = null,
            expectedUpdate = null,
        )
    }

    @Test
    fun must_detect_created_composition_set_field_entity_amongst_unchanged_entities() {
        verifyDifference(
            old = """
            |{
            |  "persons": [
            |    {
            |      "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |      "first_name": "Lois"
            |    }
            |  ]
            |}""".trimMargin(),
            new = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark"
            |    },
            |    {
            |      "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |      "first_name": "Lois"
            |    }
            |  ]
            |}""".trimMargin(),
            expectedCreate = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark"
            |    }
            |  ]
            |}""".trimMargin(),
            expectedDelete = null,
            expectedUpdate = null,
        )
    }

    @Test
    fun must_detect_deleted_composition_set_field() {
        verifyDifference(
            old = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark"
            |    }
            |  ]
            |}""".trimMargin(),
            new = """
            |{
            |}""".trimMargin(),
            expectedCreate = null,
            expectedDelete = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark"
            |    }
            |  ]
            |}""".trimMargin(),
            expectedUpdate = null,
        )
    }

    @Test
    fun must_detect_deleted_composition_set_field_entity() {
        verifyDifference(
            old = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark"
            |    }
            |  ]
            |}""".trimMargin(),
            new = """
            |{
            |  "persons": [
            |  ]
            |}""".trimMargin(),
            expectedCreate = null,
            expectedDelete = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark"
            |    }
            |  ]
            |}""".trimMargin(),
            expectedUpdate = null,
        )
    }

    @Test
    fun must_detect_deleted_empty_composition_set_field() {
        verifyDifference(
            old = """
            |{
            |  "persons": []
            |}""".trimMargin(),
            new = """
            |{
            |}""".trimMargin(),
            expectedCreate = null,
            expectedDelete = """
            |{
            |  "persons": []
            |}""".trimMargin(),
            expectedUpdate = null,
        )
    }

    @Test
    fun must_detect_deleted_composition_set_field_entity_amongst_unchanged_entities() {
        verifyDifference(
            old = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark"
            |    },
            |    {
            |      "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |      "first_name": "Lois"
            |    }
            |  ]
            |}""".trimMargin(),
            new = """
            |{
            |  "persons": [
            |    {
            |      "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |      "first_name": "Lois"
            |    }
            |  ]
            |}""".trimMargin(),
            expectedCreate = null,
            expectedDelete = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark"
            |    }
            |  ]
            |}""".trimMargin(),
            expectedUpdate = null,
        )
    }

    @Test
    fun must_detect_updated_composition_set_field_entity() {
        verifyDifference(
            old = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark"
            |    }
            |  ]
            |}""".trimMargin(),
            new = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clarke"
            |    }
            |  ]
            |}""".trimMargin(),
            expectedCreate = null,
            expectedDelete = null,
            expectedUpdate = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clarke"
            |    }
            |  ]
            |}""".trimMargin(),
        )
    }

    @Test
    fun must_detect_updated_composition_set_field_entity_amongst_unchanged_entities() {
        verifyDifference(
            old = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark"
            |    },
            |    {
            |      "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |      "first_name": "Lois"
            |    }
            |  ]
            |}""".trimMargin(),
            new = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clarke"
            |    },
            |    {
            |      "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |      "first_name": "Lois"
            |    }
            |  ]
            |}""".trimMargin(),
            expectedCreate = null,
            expectedDelete = null,
            expectedUpdate = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clarke"
            |    }
            |  ]
            |}""".trimMargin(),
        )
    }

    // endregion

    // region Composite-key composition

    @Test
    fun must_not_detect_differences_for_identical_composite_key_composition() {
        verifyDifference(
            old = """
            |{
            |    "cities": [
            |      {
            |        "city": {
            |          "name": "Test City B",
            |          "state": "Test State B",
            |          "country": "Test Country B"
            |        }
            |      }
            |    ]
            |}""".trimMargin(),
            new = """
            |{
            |    "cities": [
            |      {
            |        "city": {
            |          "name": "Test City B",
            |          "state": "Test State B",
            |          "country": "Test Country B"
            |        }
            |      }
            |    ]
            |}""".trimMargin(),
            expectedCreate = null,
            expectedDelete = null,
            expectedUpdate = null,
        )
    }

    @Test
    fun must_detect_created_composite_key_composition() {
        verifyDifference(
            old = """
            |{
            |  "cities": [
            |  ]
            |}""".trimMargin(),
            new = """
            |{
            |  "cities": [
            |    {
            |      "city": {
            |        "name": "Test City B",
            |        "state": "Test State B",
            |        "country": "Test Country B"
            |      }
            |    }
            |  ]
            |}""".trimMargin(),
            expectedCreate = """
            |{
            |  "cities": [
            |    {
            |      "city": {
            |        "name": "Test City B",
            |        "state": "Test State B",
            |        "country": "Test Country B"
            |      }
            |    }
            |  ]
            |}""".trimMargin(),
            expectedDelete = null,
            expectedUpdate = null,
        )
    }

    @Test
    fun must_detect_deleted_composite_key_composition() {
        verifyDifference(
            old = """
            |{
            |  "cities": [
            |    {
            |      "city": {
            |        "name": "Test City B",
            |        "state": "Test State B",
            |        "country": "Test Country B"
            |      }
            |    }
            |  ]
            |}""".trimMargin(),
            new = """
            |{
            |  "cities": [
            |  ]
            |}""".trimMargin(),
            expectedCreate = null,
            expectedDelete = """
            |{
            |  "cities": [
            |    {
            |      "city": {
            |        "name": "Test City B",
            |        "state": "Test State B",
            |        "country": "Test Country B"
            |      }
            |    }
            |  ]
            |}""".trimMargin(),
            expectedUpdate = null,
        )
    }

    // endregion

    // region Association single-field

    @Test
    fun must_not_detect_differences_for_identical_association_single_field() {
        verifyDifference(
            old = """
            |{
            |    "persons": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "group": {
            |          "groups": [
            |            {
            |              "name": "Group 1",
            |              "sub_groups": [
            |                {
            |                  "name": "Group 1 sub 1"
            |                }
            |              ]
            |            }
            |          ]
            |        }
            |      }
            |    ]
            |}""".trimMargin(),
            new = """
            |{
            |    "persons": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "group": {
            |          "groups": [
            |            {
            |              "name": "Group 1",
            |              "sub_groups": [
            |                {
            |                  "name": "Group 1 sub 1"
            |                }
            |              ]
            |            }
            |          ]
            |        }
            |      }
            |    ]
            |}""".trimMargin(),
            expectedCreate = null,
            expectedDelete = null,
            expectedUpdate = null,
        )
    }

    @Test
    fun must_detect_created_association_single_field() {
        verifyDifference(
            old = """
            |{
            |}""".trimMargin(),
            new = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "group": {
            |        "groups": [
            |          {
            |            "name": "Group 1",
            |            "sub_groups": [
            |              {
            |                "name": "Group 1 sub 1"
            |              }
            |            ]
            |          }
            |        ]
            |      }
            |    }
            |  ]
            |}""".trimMargin(),
            expectedCreate = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "group": {
            |        "groups": [
            |          {
            |            "name": "Group 1",
            |            "sub_groups": [
            |              {
            |                "name": "Group 1 sub 1"
            |              }
            |            ]
            |          }
            |        ]
            |      }
            |    }
            |  ]
            |}""".trimMargin(),
            expectedDelete = null,
            expectedUpdate = null,
        )
    }

    @Test
    fun must_detect_deleted_association_single_field() {
        verifyDifference(
            old = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "group": {
            |        "groups": [
            |          {
            |            "name": "Group 1",
            |            "sub_groups": [
            |              {
            |                "name": "Group 1 sub 1"
            |              }
            |            ]
            |          }
            |        ]
            |      }
            |    }
            |  ]
            |}""".trimMargin(),
            new = """
            |{
            |}""".trimMargin(),
            expectedCreate = null,
            expectedDelete = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "group": {
            |        "groups": [
            |          {
            |            "name": "Group 1",
            |            "sub_groups": [
            |              {
            |                "name": "Group 1 sub 1"
            |              }
            |            ]
            |          }
            |        ]
            |      }
            |    }
            |  ]
            |}""".trimMargin(),
            expectedUpdate = null,
        )
    }

    @Test
    fun must_detect_updated_association_single_field() {
        verifyDifference(
            old = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "group": {
            |        "groups": [
            |          {
            |            "name": "Group 1",
            |            "sub_groups": [
            |              {
            |                "name": "Group 1 sub 1"
            |              }
            |            ]
            |          }
            |        ]
            |      }
            |    }
            |  ]
            |}""".trimMargin(),
            new = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "group": {
            |        "groups": [
            |          {
            |            "name": "Group 2",
            |            "sub_groups": [
            |              {
            |                "name": "Group 2 sub 2"
            |              }
            |            ]
            |          }
            |        ]
            |      }
            |    }
            |  ]
            |}""".trimMargin(),
            expectedCreate = null,
            expectedDelete = null,
            expectedUpdate = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "group": {
            |        "groups": [
            |          {
            |            "name": "Group 2",
            |            "sub_groups": [
            |              {
            |                "name": "Group 2 sub 2"
            |              }
            |            ]
            |          }
            |        ]
            |      }
            |    }
            |  ]
            |}""".trimMargin(),
        )
    }

    // endregion

    // region Full models

    @Test
    fun must_return_difference_of_its_inputs() {
        val one = readFile("model/operator/difference/mini_test_book_1.json")
        val two = readFile("model/operator/difference/mini_test_book_2.json")
        // Ensure inputs are all different so that the test is not trivial.
        assertNotEquals(one, two)

        verifyDifference(
            old = one,
            new = two,
            expectedCreate = readFile("model/operator/difference/mini_test_book_difference_create_1_2.json"),
            expectedDelete = null,
            expectedUpdate = readFile("model/operator/difference/mini_test_book_difference_update_1_2.json"),
        )
    }

    @Test
    fun union_of_old_with_create_and_update_models_must_result_in_the_new_model() {
        val jsonInput1 = readFile("model/operator/difference/mini_test_book_1.json")
        val jsonInput2 = readFile("model/operator/difference/mini_test_book_2.json")
        val expectedMergeTestResult = "model/operator/difference/mini_test_book_2_reordered.json"

        val input1 = AddressBookMutableEntityModelFactory.create()
        decodeJsonStringIntoEntity(jsonInput1, entity = input1)
        val input2 = AddressBookMutableEntityModelFactory.create()
        decodeJsonStringIntoEntity(jsonInput2, entity = input2)
        val output = difference(input1, input2, AddressBookMutableEntityModelFactory)
        val createOutput = output.createModel
        val updateOutput = output.updateModel

        assertFalse(createOutput.isEmpty())
        assertFalse(updateOutput.isEmpty())

        val mergeCreateOutput = AddressBookMutableEntityModelFactory.create()
        val mergeUpdateOutput = AddressBookMutableEntityModelFactory.create()
        union(listOf(input1, createOutput), mergeCreateOutput)
        union(listOf(mergeCreateOutput, updateOutput), mergeUpdateOutput)
        assertMatchesJson(
            mergeUpdateOutput, expectedMergeTestResult, EncodePasswords.ALL, MultiAuxEncoder()
        )
    }

    @Test
    fun switching_difference_operator_inputs_must_produce_flipped_add_and_delete_outputs() {
        // Ensure inputs are all different so that the test is not trivial.
        val jsonInput1 = readFile("model/operator/difference/mini_test_book_1.json")
        val jsonInput2 = readFile("model/operator/difference/mini_test_book_2.json")
        val expectedCreateTestOutput = "model/operator/difference/mini_test_book_difference_create_1_2.json"
        assertNotEquals(jsonInput1, jsonInput2)


        val input1 = AddressBookMutableEntityModelFactory.create()
        decodeJsonStringIntoEntity(jsonInput1, entity = input1)
        val input2 = AddressBookMutableEntityModelFactory.create()
        decodeJsonStringIntoEntity(jsonInput2, entity = input2)
        val output = difference(input2, input1, AddressBookMutableEntityModelFactory)
        val createOutput = output.createModel
        val deleteOutput = output.deleteModel

        assertFalse(deleteOutput.isEmpty())
        assertMatchesJson(deleteOutput, expectedCreateTestOutput, EncodePasswords.ALL)

        assertTrue(createOutput.isEmpty())
    }

    @Test
    fun identical_inputs_must_return_all_null_outputs() {
        val one = readFile("model/operator/difference/mini_test_book_1.json")
        verifyDifference(
            old = one,
            new = one,
            expectedCreate = null,
            expectedDelete = null,
            expectedUpdate = null,
        )
    }

    @Test
    fun must_produce_three_correct_non_null_models() {
        val old = readFile("model/operator/difference/mini_test_book_2.json")
        val new = readFile("model/operator/difference/mini_test_book_3.json")
        // Ensure inputs are all different so that the test is not trivial.
        assertNotEquals(old, new)

        verifyDifference(
            old = old,
            new = new,
            expectedCreate = readFile("model/operator/difference/mini_test_book_difference_create_2_3.json"),
            expectedDelete = readFile("model/operator/difference/mini_test_book_difference_delete_2_3.json"),
            expectedUpdate = readFile("model/operator/difference/mini_test_book_difference_update_2_3.json"),
        )
    }

    // endregion
}

private fun verifyDifference(
    old: String,
    new: String,
    expectedCreate: String?,
    expectedDelete: String?,
    expectedUpdate: String?
) {
    val oldModel = AddressBookMutableEntityModelFactory.create()
    decodeJsonStringIntoEntity(old, entity = oldModel)
    val newModel = AddressBookMutableEntityModelFactory.create()
    decodeJsonStringIntoEntity(new, entity = newModel)

    val difference = difference(oldModel, newModel, AddressBookMutableEntityModelFactory)

    verifyDifferenceModel("create", expectedCreate, difference.createModel)
    verifyDifferenceModel("delete", expectedDelete, difference.deleteModel)
    verifyDifferenceModel("update", expectedUpdate, difference.updateModel)
}

private fun verifyDifferenceModel(differenceType: String, expected: String?, actual: EntityModel) {
    if (expected == null) {
        if (!actual.isEmpty()) {
            println("Expected $differenceType model to be empty, but was:")
            logModel("", actual)
            assertTrue(actual.isEmpty())
        }
    } else assertMatchesJsonString(actual, expected, EncodePasswords.ALL)
}
