package org.treeWare.model.operator

import org.treeWare.metaModel.addressBookRootEntityFactory
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
        val model = addressBookRootEntityFactory(null)
        val settings = getOrNewMutableSingleEntity(model, "settings")
        setBooleanSingleField(settings, "last_name_first", true) // non-key field

        val success = populateSubTree(settings, true)
        assertFalse(success)
    }

    @Test
    fun `populateSubTree() must not succeed if sub-tree root in a set-field has non-key fields`() {
        val model = addressBookRootEntityFactory(null)
        val persons = getOrNewMutableSetField(model, "persons")
        val clark = getNewMutableSetEntity(persons)
        setUuidSingleField(clark, "id", "cc477201-48ec-4367-83a4-7fdbd92f8a6f")
        setStringSingleField(clark, "first_name", "Clark") // non-key field
        persons.addValue(clark)

        val success = populateSubTree(clark, true)
        assertFalse(success)
    }

    @Test
    fun `populateSubTree() must populate all fields in a single-field entity when populateNonKeyNonCompositionFields is true`() {
        val model = addressBookRootEntityFactory(null)
        val settings = getOrNewMutableSingleEntity(model, "settings")

        val success = populateSubTree(settings, true)

        val expectedJson = """
            {
              "settings": {
                "last_name_first": null,
                "encrypt_hero_name": null,
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
        val model = addressBookRootEntityFactory(null)
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
        val model = addressBookRootEntityFactory(null)
        val persons = getOrNewMutableSetField(model, "persons")
        val clark = getNewMutableSetEntity(persons)
        setUuidSingleField(clark, "id", "cc477201-48ec-4367-83a4-7fdbd92f8a6f")
        persons.addValue(clark)

        val success = populateSubTree(clark, true)

        val expectedJson = """
            {
              "persons": [
                {
                  "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                  "first_name": null,
                  "last_name": null,
                  "hero_name": null,
                  "picture": null,
                  "relations": [
                    {
                      "id": null,
                      "relationship": null,
                      "person": null
                    }
                  ],
                  "password": null,
                  "main_secret": null,
                  "group": null,
                  "is_hero": null,
                  "hero_details": {
                    "strengths": null,
                    "weaknesses": null
                  },
                  "keyless": {
                    "name": null,
                    "keyless_child": {
                      "name": null
                    },
                    "keyed_child": {
                      "name": null,
                      "other": null
                    }
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
        val model = addressBookRootEntityFactory(null)
        val persons = getOrNewMutableSetField(model, "persons")
        val clark = getNewMutableSetEntity(persons)
        setUuidSingleField(clark, "id", "cc477201-48ec-4367-83a4-7fdbd92f8a6f")
        persons.addValue(clark)

        val success = populateSubTree(clark, false)

        val expectedJson = """
            {
              "persons": [
                {
                  "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                  "relations": [
                    {
                      "id": null
                    }
                  ],
                  "hero_details": {},
                  "keyless": {
                    "keyless_child": {},
                    "keyed_child": {
                      "name": null
                    }
                  }
                }
              ]
            }
        """.trimIndent()
        assertMatchesJsonString(model, expectedJson, EncodePasswords.NONE)
        assertTrue(success)
    }

    @Test
    fun `populateSubTree() must populate all fields in a composite-keyed sub-tree root when populateNonKeyNonCompositionFields is true`() {
        val model = addressBookRootEntityFactory(null)
        val cities = getOrNewMutableSetField(model, "cities")
        val sanFrancisco = getNewMutableSetEntity(cities)
        val keys = getOrNewMutableSingleEntity(sanFrancisco, "city")
        setStringSingleField(keys, "name", "San Francisco")
        setStringSingleField(keys, "state", "California")
        setStringSingleField(keys, "country", "USA")
        cities.addValue(sanFrancisco)

        val success = populateSubTree(sanFrancisco, true)

        val expectedJson = """
            {
              "cities": [
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
                  "keyless": {
                    "name": null,
                    "keyless_child": {
                      "name": null
                    },
                    "keyed_child": {
                      "name": null,
                      "other": null
                    }
                  }
                }
              ]
            }
        """.trimIndent()
        assertMatchesJsonString(model, expectedJson, EncodePasswords.NONE)
        assertTrue(success)
    }

    @Test
    fun `populateSubTree() must populate only key & composition fields in a composite-keyed sub-tree root when populateNonKeyNonCompositionFields is false`() {
        val model = addressBookRootEntityFactory(null)
        val cities = getOrNewMutableSetField(model, "cities")
        val sanFrancisco = getNewMutableSetEntity(cities)
        val keys = getOrNewMutableSingleEntity(sanFrancisco, "city")
        setStringSingleField(keys, "name", "San Francisco")
        setStringSingleField(keys, "state", "California")
        setStringSingleField(keys, "country", "USA")
        cities.addValue(sanFrancisco)

        val success = populateSubTree(sanFrancisco, false)

        val expectedJson = """
            {
              "cities": [
                {
                  "city": {
                    "name": "San Francisco",
                    "state": "California",
                    "country": "USA"
                  },
                  "city_center": {},
                  "keyless": {
                    "keyless_child": {},
                    "keyed_child": {
                      "name": null
                    }
                  }
                }
              ]
            }
        """.trimIndent()
        assertMatchesJsonString(model, expectedJson, EncodePasswords.NONE)
        assertTrue(success)
    }

    @Test
    fun `populateSubTree() must populate sub-trees that have composite-keyed entities in them when populateNonKeyNonCompositionFields is true`() {
        val model = addressBookRootEntityFactory(null)

        val success = populateSubTree(model, true)

        val expectedJsonSnippet = """
            |  "cities": [
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
        val model = addressBookRootEntityFactory(null)

        val success = populateSubTree(model, false)

        val expectedJsonSnippet = """
            |  "cities": [
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