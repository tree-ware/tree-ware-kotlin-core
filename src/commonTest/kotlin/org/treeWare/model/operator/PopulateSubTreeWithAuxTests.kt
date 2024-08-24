package org.treeWare.model.operator

import org.treeWare.model.AddressBookMutableEntityModelFactory
import org.treeWare.model.assertContainsJsonString
import org.treeWare.model.assertMatchesJsonString
import org.treeWare.model.core.*
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.MultiAuxEncoder
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAux
import org.treeWare.model.operator.set.aux.SetAuxEncoder
import org.treeWare.model.operator.set.aux.setSetAux
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private val multiAuxEncoder = MultiAuxEncoder(SET_AUX_NAME to SetAuxEncoder())

class PopulateSubTreeWithAuxTests {
    @Test
    fun `populateSubTree() must return an error if sub-tree root in a single-field has non-key fields`() {
        val model = AddressBookMutableEntityModelFactory.create()
        val settings = getOrNewMutableSingleEntity(model, "settings")
        setBooleanSingleField(settings, "last_name_first", true) // non-key field

        val success = populateSubTree(settings, true) { setSetAux(it, SetAux.DELETE) }
        assertFalse(success)
    }

    @Test
    fun `populateSubTree() must return an error if sub-tree root in a set-field has non-key fields`() {
        val model = AddressBookMutableEntityModelFactory.create()
        val persons = getOrNewMutableSetField(model, "person")
        val clark = getNewMutableSetEntity(persons)
        setUuidSingleField(clark, "id", "cc477201-48ec-4367-83a4-7fdbd92f8a6f")
        setStringSingleField(clark, "first_name", "Clark") // non-key field
        persons.addValue(clark)

        val success = populateSubTree(clark, true) { setSetAux(it, SetAux.DELETE) }
        assertFalse(success)
    }

    @Test
    fun `populateSubTree() must populate all fields in a single-field entity when populateNonKeyNonCompositionFields is true`() {
        val model = AddressBookMutableEntityModelFactory.create()
        val settings = getOrNewMutableSingleEntity(model, "settings")

        val success = populateSubTree(settings, true) { setSetAux(it, SetAux.DELETE) }

        val expectedJson = """
            {
              "settings": {
                "set_": "delete",
                "last_name_first": null,
                "encrypt_hero_name": null,
                "card_colors": [],
                "background_color": null,
                "advanced": {
                  "set_": "delete",
                  "border_color": null
                }
              }
            }
        """.trimIndent()
        assertMatchesJsonString(model, expectedJson, EncodePasswords.NONE, multiAuxEncoder)
        assertTrue(success)
    }

    @Test
    fun `populateSubTree() must populate only composition fields in a single-field entity when populateNonKeyNonCompositionFields is false`() {
        val model = AddressBookMutableEntityModelFactory.create()
        val settings = getOrNewMutableSingleEntity(model, "settings")

        val success = populateSubTree(settings, false) { setSetAux(it, SetAux.DELETE) }

        val expectedJson = """
            {
              "settings": {
                "set_": "delete",
                "advanced": {
                  "set_": "delete"
                }
              }
            }
        """.trimIndent()
        assertMatchesJsonString(model, expectedJson, EncodePasswords.NONE, multiAuxEncoder)
        assertTrue(success)
    }

    @Test
    fun `populateSubTree() must populate all fields in a set-field entity when populateNonKeyNonCompositionFields is true`() {
        val model = AddressBookMutableEntityModelFactory.create()
        val persons = getOrNewMutableSetField(model, "person")
        val clark = getNewMutableSetEntity(persons)
        setUuidSingleField(clark, "id", "cc477201-48ec-4367-83a4-7fdbd92f8a6f")
        persons.addValue(clark)

        val success = populateSubTree(clark, true) { setSetAux(it, SetAux.DELETE) }

        val expectedJson = """
            {
              "person": [
                {
                  "set_": "delete",
                  "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                  "first_name": null,
                  "last_name": null,
                  "hero_name": null,
                  "email": [],
                  "picture": null,
                  "relation": [
                    {
                      "set_": "delete",
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
                    "set_": "delete",
                    "strengths": null,
                    "weaknesses": null
                  }
                }
              ]
            }
        """.trimIndent()
        assertMatchesJsonString(model, expectedJson, EncodePasswords.NONE, multiAuxEncoder)
        assertTrue(success)
    }

    @Test
    fun `populateSubTree() must populate only key & composition fields in a set-field entity when populateNonKeyNonCompositionFields is false`() {
        val model = AddressBookMutableEntityModelFactory.create()
        val persons = getOrNewMutableSetField(model, "person")
        val clark = getNewMutableSetEntity(persons)
        setUuidSingleField(clark, "id", "cc477201-48ec-4367-83a4-7fdbd92f8a6f")
        persons.addValue(clark)

        val success = populateSubTree(clark, false) { setSetAux(it, SetAux.DELETE) }

        val expectedJson = """
            {
              "person": [
                {
                  "set_": "delete",
                  "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                  "relation": [
                    {
                      "set_": "delete",
                      "id": null
                    }
                  ],
                  "hero_details": {
                    "set_": "delete"
                  }
                }
              ]
            }
        """.trimIndent()
        assertMatchesJsonString(model, expectedJson, EncodePasswords.NONE, multiAuxEncoder)
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

        val success = populateSubTree(sanFrancisco, true) { setSetAux(it, SetAux.DELETE) }

        val expectedJson = """
            {
              "city_info": [
                {
                  "set_": "delete",
                  "city": {
                    "set_": "delete",
                    "name": "San Francisco",
                    "state": "California",
                    "country": "USA"
                  },
                  "info": null,
                  "city_center": {
                    "set_": "delete",
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
        assertMatchesJsonString(model, expectedJson, EncodePasswords.NONE, multiAuxEncoder)
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

        val success = populateSubTree(sanFrancisco, false) { setSetAux(it, SetAux.DELETE) }

        val expectedJson = """
            {
              "city_info": [
                {
                  "set_": "delete",
                  "city": {
                    "set_": "delete",
                    "name": "San Francisco",
                    "state": "California",
                    "country": "USA"
                  },
                  "city_center": {
                    "set_": "delete"
                  }
                }
              ]
            }
        """.trimIndent()
        assertMatchesJsonString(model, expectedJson, EncodePasswords.NONE, multiAuxEncoder)
        assertTrue(success)
    }

    @Test
    fun `populateSubTree() must populate sub-trees that have composite-keyed entities in them when populateNonKeyNonCompositionFields is true`() {
        val model = AddressBookMutableEntityModelFactory.create()

        val success = populateSubTree(model, true) { setSetAux(it, SetAux.DELETE) }

        val expectedJsonSnippet = """
            |  "city_info": [
            |    {
            |      "set_": "delete",
            |      "city": {
            |        "set_": "delete",
            |        "name": null,
            |        "state": null,
            |        "country": null
            |      }
        """.trimMargin()
        assertContainsJsonString(model, expectedJsonSnippet, EncodePasswords.NONE, multiAuxEncoder)
        assertTrue(success)
    }

    @Test
    fun `populateSubTree() must populate sub-trees that have composite-keyed entities in them when populateNonKeyNonCompositionFields is false`() {
        val model = AddressBookMutableEntityModelFactory.create()

        val success = populateSubTree(model, false) { setSetAux(it, SetAux.DELETE) }

        val expectedJsonSnippet = """
            |  "city_info": [
            |    {
            |      "set_": "delete",
            |      "city": {
            |        "set_": "delete",
            |        "name": null,
            |        "state": null,
            |        "country": null
            |      }
        """.trimMargin()
        assertContainsJsonString(model, expectedJsonSnippet, EncodePasswords.NONE, multiAuxEncoder)
        assertTrue(success)
    }
}