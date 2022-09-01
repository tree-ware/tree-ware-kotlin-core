package org.treeWare.model.operator

import org.treeWare.metaModel.addressBookMetaModel
import org.treeWare.model.assertContainsJsonString
import org.treeWare.model.assertMatchesJsonString
import org.treeWare.model.core.*
import org.treeWare.model.encoder.EncodePasswords
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PopulateSubTreeTests {
    @Test
    fun `populateSubTree() must return an error if sub-tree root in a single-field has non-key fields`() {
        val model = MutableMainModel(addressBookMetaModel)
        val root = model.getOrNewRoot()
        val settings = getOrNewMutableSingleEntity(root, "settings")
        setBooleanSingleField(settings, "last_name_first", true) // non-key field

        val error = populateSubTree(settings, true)

        assertEquals("Sub-tree root is not empty", error)
    }

    @Test
    fun `populateSubTree() must return an error if sub-tree root in a set-field has non-key fields`() {
        val model = MutableMainModel(addressBookMetaModel)
        val root = model.getOrNewRoot()
        val persons = getOrNewMutableSetField(root, "person")
        val clark = getNewMutableSetEntity(persons)
        setUuidSingleField(clark, "id", "cc477201-48ec-4367-83a4-7fdbd92f8a6f")
        setStringSingleField(clark, "first_name", "Clark") // non-key field
        persons.addValue(clark)

        val error = populateSubTree(clark, true)

        assertEquals("Sub-tree root is not empty", error)
    }

    @Test
    fun `populateSubTree() must populate all fields in a single-field entity when populateNonKeyNonCompositionFields is true`() {
        val model = MutableMainModel(addressBookMetaModel)
        val root = model.getOrNewRoot()
        val settings = getOrNewMutableSingleEntity(root, "settings")

        val error = populateSubTree(settings, true)

        val expectedJson = """
            {
              "address_book": {
                "settings": {
                  "last_name_first": null,
                  "encrypt_hero_name": null,
                  "card_colors": [],
                  "background_color": null,
                  "advanced": {
                    "border_color": null
                  }
                }
              }
            }
        """.trimIndent()
        assertMatchesJsonString(model, expectedJson, EncodePasswords.NONE)
        assertNull(error)
    }

    @Test
    fun `populateSubTree() must populate only composition fields in a single-field entity when populateNonKeyNonCompositionFields is false`() {
        val model = MutableMainModel(addressBookMetaModel)
        val root = model.getOrNewRoot()
        val settings = getOrNewMutableSingleEntity(root, "settings")

        val error = populateSubTree(settings, false)

        val expectedJson = """
            {
              "address_book": {
                "settings": {
                  "advanced": {}
                }
              }
            }
        """.trimIndent()
        assertMatchesJsonString(model, expectedJson, EncodePasswords.NONE)
        assertNull(error)
    }

    @Test
    fun `populateSubTree() must populate all fields in a set-field entity when populateNonKeyNonCompositionFields is true`() {
        val model = MutableMainModel(addressBookMetaModel)
        val root = model.getOrNewRoot()
        val persons = getOrNewMutableSetField(root, "person")
        val clark = getNewMutableSetEntity(persons)
        setUuidSingleField(clark, "id", "cc477201-48ec-4367-83a4-7fdbd92f8a6f")
        persons.addValue(clark)

        val error = populateSubTree(clark, true)

        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                    "first_name": null,
                    "last_name": null,
                    "hero_name": null,
                    "email": [],
                    "picture": null,
                    "relation": [
                      {
                        "id": null,
                        "relationship": null,
                        "person": null
                      }
                    ],
                    "password": null,
                    "previous_passwords": [],
                    "main_secret": null,
                    "other_secrets": [],
                    "group": null,
                    "is_hero": null,
                    "hero_details": {
                      "strengths": null,
                      "weaknesses": null
                    }
                  }
                ]
              }
            }
        """.trimIndent()
        assertMatchesJsonString(model, expectedJson, EncodePasswords.NONE)
        assertNull(error)
    }

    @Test
    fun `populateSubTree() must populate only key & composition fields in a set-field entity when populateNonKeyNonCompositionFields is false`() {
        val model = MutableMainModel(addressBookMetaModel)
        val root = model.getOrNewRoot()
        val persons = getOrNewMutableSetField(root, "person")
        val clark = getNewMutableSetEntity(persons)
        setUuidSingleField(clark, "id", "cc477201-48ec-4367-83a4-7fdbd92f8a6f")
        persons.addValue(clark)

        val error = populateSubTree(clark, false)

        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                    "relation": [
                      {
                        "id": null
                      }
                    ],
                    "hero_details": {}
                  }
                ]
              }
            }
        """.trimIndent()
        assertMatchesJsonString(model, expectedJson, EncodePasswords.NONE)
        assertNull(error)
    }

    @Test
    fun `populateSubTree() must populate all fields in a composite-keyed sub-tree root when populateNonKeyNonCompositionFields is true`() {
        val model = MutableMainModel(addressBookMetaModel)
        val root = model.getOrNewRoot()
        val cities = getOrNewMutableSetField(root, "city_info")
        val sanFrancisco = getNewMutableSetEntity(cities)
        val keys = getOrNewMutableSingleEntity(sanFrancisco, "city")
        setStringSingleField(keys, "name", "San Francisco")
        setStringSingleField(keys, "state", "California")
        setStringSingleField(keys, "country", "USA")
        cities.addValue(sanFrancisco)

        val error = populateSubTree(sanFrancisco, true)

        val expectedJson = """
            {
              "address_book": {
                "city_info": [
                  {
                    "city": {
                      "name": "San Francisco",
                      "state": "California",
                      "country": "USA"
                    },
                    "info": null,
                    "city_center": {
                      "latitude": null,
                      "longitude": null
                    },
                    "is_coastal_city": null,
                    "water_body_name": null,
                    "related_city_info": []
                  }
                ]
              }
            }
        """.trimIndent()
        assertMatchesJsonString(model, expectedJson, EncodePasswords.NONE)
        assertNull(error)
    }

    @Test
    fun `populateSubTree() must populate only key & composition fields in a composite-keyed sub-tree root when populateNonKeyNonCompositionFields is false`() {
        val model = MutableMainModel(addressBookMetaModel)
        val root = model.getOrNewRoot()
        val cities = getOrNewMutableSetField(root, "city_info")
        val sanFrancisco = getNewMutableSetEntity(cities)
        val keys = getOrNewMutableSingleEntity(sanFrancisco, "city")
        setStringSingleField(keys, "name", "San Francisco")
        setStringSingleField(keys, "state", "California")
        setStringSingleField(keys, "country", "USA")
        cities.addValue(sanFrancisco)

        val error = populateSubTree(sanFrancisco, false)

        val expectedJson = """
            {
              "address_book": {
                "city_info": [
                  {
                    "city": {
                      "name": "San Francisco",
                      "state": "California",
                      "country": "USA"
                    },
                    "city_center": {}
                  }
                ]
              }
            }
        """.trimIndent()
        assertMatchesJsonString(model, expectedJson, EncodePasswords.NONE)
        assertNull(error)
    }

    @Test
    fun `populateSubTree() must populate sub-trees that have composite-keyed entities in them when populateNonKeyNonCompositionFields is true`() {
        val model = MutableMainModel(addressBookMetaModel)
        val root = model.getOrNewRoot()

        val error = populateSubTree(root, true)

        val expectedJsonSnippet = """
            |    "city_info": [
            |      {
            |        "city": {
            |          "name": null,
            |          "state": null,
            |          "country": null
            |        }
        """.trimMargin()
        assertContainsJsonString(model, expectedJsonSnippet, EncodePasswords.NONE)
        assertNull(error)
    }

    @Test
    fun `populateSubTree() must populate sub-trees that have composite-keyed entities in them when populateNonKeyNonCompositionFields is false`() {
        val model = MutableMainModel(addressBookMetaModel)
        val root = model.getOrNewRoot()

        val error = populateSubTree(root, false)

        val expectedJsonSnippet = """
            |    "city_info": [
            |      {
            |        "city": {
            |          "name": null,
            |          "state": null,
            |          "country": null
            |        }
        """.trimMargin()
        assertContainsJsonString(model, expectedJsonSnippet, EncodePasswords.NONE)
        assertNull(error)
    }
}