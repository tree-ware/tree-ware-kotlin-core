package org.treeWare.model.operator

import org.treeWare.model.AddressBookMutableEntityModelFactory
import org.treeWare.model.assertContainsJsonString
import org.treeWare.model.assertMatchesJsonString
import org.treeWare.model.core.*
import org.treeWare.model.encoder.EncodePasswords
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PopulateSubTreeTests {
    @Test
    fun `populateSubTree() must not succeed if sub-tree root in a single-field has non-key fields`() {
        val model = AddressBookMutableEntityModelFactory.create()
        val settings = getOrNewMutableSingleEntity(model, "settings")
        setBooleanSingleField(settings, "last_name_first", true) // non-key field

        val success = populateSubTree(settings, true)
        assertFalse(success)
    }

    @Test
    fun `populateSubTree() must not succeed if sub-tree root in a set-field has non-key fields`() {
        val model = AddressBookMutableEntityModelFactory.create()
        val persons = getOrNewMutableSetField(model, "person")
        val clark = getNewMutableSetEntity(persons)
        setUuidSingleField(clark, "id", "cc477201-48ec-4367-83a4-7fdbd92f8a6f")
        setStringSingleField(clark, "first_name", "Clark") // non-key field
        persons.addValue(clark)

        val success = populateSubTree(clark, true)
        assertFalse(success)
    }

    @Test
    fun `populateSubTree() must populate all fields in a single-field entity when populateNonKeyNonCompositionFields is true`() {
        val model = AddressBookMutableEntityModelFactory.create()
        val settings = getOrNewMutableSingleEntity(model, "settings")

        val success = populateSubTree(settings, true)

        val expectedJson = """
            {
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
        """.trimIndent()
        assertMatchesJsonString(model, expectedJson, EncodePasswords.NONE)
        assertTrue(success)
    }

    @Test
    fun `populateSubTree() must populate only composition fields in a single-field entity when populateNonKeyNonCompositionFields is false`() {
        val model = AddressBookMutableEntityModelFactory.create()
        val settings = getOrNewMutableSingleEntity(model, "settings")

        val success = populateSubTree(settings, false)

        val expectedJson = """
            {
              "settings": {
                "advanced": {}
              }
            }
        """.trimIndent()
        assertMatchesJsonString(model, expectedJson, EncodePasswords.NONE)
        assertTrue(success)
    }

    @Test
    fun `populateSubTree() must populate all fields in a set-field entity when populateNonKeyNonCompositionFields is true`() {
        val model = AddressBookMutableEntityModelFactory.create()
        val persons = getOrNewMutableSetField(model, "person")
        val clark = getNewMutableSetEntity(persons)
        setUuidSingleField(clark, "id", "cc477201-48ec-4367-83a4-7fdbd92f8a6f")
        persons.addValue(clark)

        val success = populateSubTree(clark, true)

        val expectedJson = """
            {
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
        """.trimIndent()
        assertMatchesJsonString(model, expectedJson, EncodePasswords.NONE)
        assertTrue(success)
    }

    @Test
    fun `populateSubTree() must populate only key & composition fields in a set-field entity when populateNonKeyNonCompositionFields is false`() {
        val model = AddressBookMutableEntityModelFactory.create()
        val persons = getOrNewMutableSetField(model, "person")
        val clark = getNewMutableSetEntity(persons)
        setUuidSingleField(clark, "id", "cc477201-48ec-4367-83a4-7fdbd92f8a6f")
        persons.addValue(clark)

        val success = populateSubTree(clark, false)

        val expectedJson = """
            {
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
        """.trimIndent()
        assertMatchesJsonString(model, expectedJson, EncodePasswords.NONE)
        assertTrue(success)
    }

    @Test
    fun `populateSubTree() must populate all fields in a composite-keyed sub-tree root when populateNonKeyNonCompositionFields is true`() {
        val model = AddressBookMutableEntityModelFactory.create()
        val cities = getOrNewMutableSetField(model, "city_info")
        val sanFrancisco = getNewMutableSetEntity(cities)
        val keys = getOrNewMutableSingleEntity(sanFrancisco, "city")
        setStringSingleField(keys, "name", "San Francisco")
        setStringSingleField(keys, "state", "California")
        setStringSingleField(keys, "country", "USA")
        cities.addValue(sanFrancisco)

        val success = populateSubTree(sanFrancisco, true)

        val expectedJson = """
            {
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
        """.trimIndent()
        assertMatchesJsonString(model, expectedJson, EncodePasswords.NONE)
        assertTrue(success)
    }

    @Test
    fun `populateSubTree() must populate only key & composition fields in a composite-keyed sub-tree root when populateNonKeyNonCompositionFields is false`() {
        val model = AddressBookMutableEntityModelFactory.create()
        val cities = getOrNewMutableSetField(model, "city_info")
        val sanFrancisco = getNewMutableSetEntity(cities)
        val keys = getOrNewMutableSingleEntity(sanFrancisco, "city")
        setStringSingleField(keys, "name", "San Francisco")
        setStringSingleField(keys, "state", "California")
        setStringSingleField(keys, "country", "USA")
        cities.addValue(sanFrancisco)

        val success = populateSubTree(sanFrancisco, false)

        val expectedJson = """
            {
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
        """.trimIndent()
        assertMatchesJsonString(model, expectedJson, EncodePasswords.NONE)
        assertTrue(success)
    }

    @Test
    fun `populateSubTree() must populate sub-trees that have composite-keyed entities in them when populateNonKeyNonCompositionFields is true`() {
        val model = AddressBookMutableEntityModelFactory.create()

        val success = populateSubTree(model, true)

        val expectedJsonSnippet = """
            |  "city_info": [
            |    {
            |      "city": {
            |        "name": null,
            |        "state": null,
            |        "country": null
            |      }
        """.trimMargin()
        assertContainsJsonString(model, expectedJsonSnippet, EncodePasswords.NONE)
        assertTrue(success)
    }

    @Test
    fun `populateSubTree() must populate sub-trees that have composite-keyed entities in them when populateNonKeyNonCompositionFields is false`() {
        val model = AddressBookMutableEntityModelFactory.create()

        val success = populateSubTree(model, false)

        val expectedJsonSnippet = """
            |  "city_info": [
            |    {
            |      "city": {
            |        "name": null,
            |        "state": null,
            |        "country": null
            |      }
        """.trimMargin()
        assertContainsJsonString(model, expectedJsonSnippet, EncodePasswords.NONE)
        assertTrue(success)
    }
}