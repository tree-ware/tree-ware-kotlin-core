package org.treeWare.model.operator

import org.treeWare.metaModel.addressBookRootEntityFactory
import org.treeWare.model.core.EntityModel
import org.treeWare.model.decodeJsonStringIntoEntity
import org.treeWare.util.readFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class EqualsTests {
    // region Primitive single-fields

    @Test
    fun `identical single-fields must be equal`() {
        verifyEquals(
            first = """
            |{
            |  "name": "Test Book",
            |  "last_updated": "1587147731"
            |}""".trimMargin(),
            second = """
            |{
            |  "name": "Test Book",
            |  "last_updated": "1587147731"
            |}""".trimMargin(),
            expected = true
        )
    }

    @Test
    fun `updated primitive single-fields must not be equal`() {
        verifyEquals(
            first = """
            |{
            |  "name": "Test Book",
            |  "last_updated": "1587147731"
            |}""".trimMargin(),
            second = """
            |{
            |  "name": "Test Book 1",
            |  "last_updated": "1587147733"
            |}""".trimMargin(),
            expected = false
        )
    }

    @Test
    fun `created primitive single-fields must not be equal`() {
        verifyEquals(
            first = "{}",
            second = """
            |{
            |  "name": "Test Book",
            |  "last_updated": "1587147731"
            |}""".trimMargin(),
            expected = false
        )
    }

    @Test
    fun `deleted primitive single-fields must not be equal`() {
        verifyEquals(
            first = """
            |{
            |  "name": "Test Book",
            |  "last_updated": "1587147731"
            |}""".trimMargin(),
            second = "{}",
            expected = false
        )
    }

    // endregion

    // region Composition single-field

    @Test
    fun `identical composition single-field must be equal`() {
        verifyEquals(
            first = """
            |{
            |    "settings": {
            |      "last_name_first": true,
            |      "encrypt_hero_name": false
            |    }
            |}""".trimMargin(),
            second = """
            |{
            |    "settings": {
            |      "last_name_first": true,
            |      "encrypt_hero_name": false
            |    }
            |}""".trimMargin(),
            expected = true
        )
    }

    @Test
    fun `updated composition single-field must not be equal`() {
        verifyEquals(
            first = """
            |{
            |  "settings": {
            |    "last_name_first": true,
            |    "encrypt_hero_name": false
            |  }
            |}""".trimMargin(),
            second = """
            |{
            |  "settings": {
            |    "last_name_first": false,
            |    "encrypt_hero_name": true
            |  }
            |}""".trimMargin(),
            expected = false
        )
    }

    // endregion

    // region Composition set-field

    @Test
    fun `identical composition set-field must be equal`() {
        verifyEquals(
            first = """
            |{
            |    "persons": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark"
            |      }
            |    ]
            |}""".trimMargin(),
            second = """
            |{
            |    "persons": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark"
            |      }
            |    ]
            |}""".trimMargin(),
            expected = true
        )
    }

    @Test
    fun `reordered composition set-field must be equal`() {
        verifyEquals(
            first = """
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
            second = """
            |{
            |  "persons": [
            |    {
            |      "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |      "first_name": "Lois"
            |    },
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark"
            |    }
            |  ]
            |}""".trimMargin(),
            expected = true
        )
    }

    @Test
    fun `created composition set-field entity must not be equal`() {
        verifyEquals(
            first = """
            |{
            |  "persons": [
            |  ]
            |}""".trimMargin(),
            second = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark"
            |    }
            |  ]
            |}""".trimMargin(),
            expected = false
        )
    }

    @Test
    fun `deleted composition set-field entity must not be equal`() {
        verifyEquals(
            first = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark"
            |    }
            |  ]
            |}""".trimMargin(),
            second = """
            |{
            |  "persons": [
            |  ]
            |}""".trimMargin(),
            expected = false
        )
    }

    @Test
    fun `updated composition set-field entity must not be equal`() {
        verifyEquals(
            first = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark"
            |    }
            |  ]
            |}""".trimMargin(),
            second = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Superman"
            |    }
            |  ]
            |}""".trimMargin(),
            expected = false
        )
    }

    @Test
    fun `missing and empty composition set-field must not be equal`() {
        verifyEquals(
            first = """
            |{
            |}""".trimMargin(),
            second = """
            |{
            |  "persons": []
            |}""".trimMargin(),
            expected = false
        )
    }

    // endregion

    // region Association single-field

    @Test
    fun `identical association single-field must be equal`() {
        verifyEquals(
            first = """
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
            second = """
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
            expected = true
        )
    }

    @Test
    fun `updated association single-field must not be equal`() {
        verifyEquals(
            first = """
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
            second = """
            |{
            |    "persons": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "group": {
            |          "groups": [
            |            {
            |              "name": "Group 2",
            |              "sub_groups": [
            |                {
            |                  "name": "Group 2 sub 2"
            |                }
            |              ]
            |            }
            |          ]
            |        }
            |      }
            |    ]
            |}""".trimMargin(),
            expected = false
        )
    }

    // endregion

    // region Non-root elements

    @Test
    fun `identical non-root elements must be equal`() {
        val first = decode(
            """
            |{
            |  "name": "Test Book",
            |  "last_updated": "1587147731"
            |}""".trimMargin()
        )
        val second = decode(
            """
            |{
            |  "name": "Test Book",
            |  "last_updated": "1587147731"
            |}""".trimMargin()
        )
        val firstField = first.getField("name") ?: throw IllegalStateException("Field name is missing")
        val secondField = second.getField("name") ?: throw IllegalStateException("Field name is missing")
        assertTrue(equals(firstField, secondField))
        assertTrue(equals(firstField, firstField))
    }

    @Test
    fun `different non-root elements must not be equal`() {
        val first = decode(
            """
            |{
            |  "name": "Test Book",
            |  "last_updated": "1587147731"
            |}""".trimMargin()
        )
        val second = decode(
            """
            |{
            |  "name": "Test Book 1",
            |  "last_updated": "1587147731"
            |}""".trimMargin()
        )
        val firstField = first.getField("name") ?: throw IllegalStateException("Field name is missing")
        val secondField = second.getField("name") ?: throw IllegalStateException("Field name is missing")
        assertFalse(equals(firstField, secondField))
        assertFalse(equals(firstField, second.getField("last_updated")
            ?: throw IllegalStateException("Field last_updated is missing")))
    }

    // endregion

    // region Full models

    @Test
    fun `identical full models must be equal`() {
        val one = readFile("model/operator/difference/mini_test_book_1.json")
        verifyEquals(first = one, second = one, expected = true)
    }

    @Test
    fun `different full models must not be equal`() {
        val one = readFile("model/operator/difference/mini_test_book_1.json")
        val two = readFile("model/operator/difference/mini_test_book_2.json")
        // Ensure inputs are all different so that the test is not trivial.
        assertNotEquals(one, two)

        verifyEquals(first = one, second = two, expected = false)
    }

    // endregion
}

private fun decode(json: String): EntityModel {
    val model = addressBookRootEntityFactory(null)
    decodeJsonStringIntoEntity(json, entity = model)
    return model
}

private fun verifyEquals(first: String, second: String, expected: Boolean) {
    val firstModel = decode(first)
    val secondModel = decode(second)

    assertEquals(expected, equals(firstModel, secondModel))
    // Equality must be symmetric.
    assertEquals(expected, equals(secondModel, firstModel))
}
