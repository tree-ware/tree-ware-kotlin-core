package org.treeWare.model.operator

import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import org.treeWare.metaModel.addressBookMetaModel
import org.treeWare.mockk.fieldsWithNames
import org.treeWare.model.*
import org.treeWare.model.core.*
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.operator.get.GetCompositionResult
import org.treeWare.model.operator.get.GetCompositionSetResult
import org.treeWare.model.operator.get.GetDelegate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetDelegateSpecificAndWildcardEntitiesTests {
    @Test
    fun `get() must call its delegate for specific and wildcard entities in a request`() {
        val request =
            getMainModelFromJsonFile(
                addressBookMetaModel,
                "org/treeWare/model/operator/get_request_specific_and_wildcard_entities.json"
            )

        val delegate = mockk<GetDelegate>()
        every {
            delegate.getComposition("/address_book", ofType(), fieldsWithNames("name", "last_updated"), ofType())
        } answers {
            val addressBookField = arg<MutableSingleFieldModel>(3)
            val addressBook = addressBookField.getOrNewValue() as MutableEntityModel
            setStringSingleField(addressBook, "name", "Super Heroes")
            setTimestampSingleField(addressBook, "last_updated", 1587147731L)
            GetCompositionResult.Entity(addressBook)
        }
        every {
            delegate.getComposition(
                "/address_book/settings",
                ofType(),
                fieldsWithNames("last_name_first", "card_colors"),
                ofType()
            )
        } answers {
            val settingsField = arg<MutableSingleFieldModel>(3)
            val settings = settingsField.getOrNewValue() as MutableEntityModel
            setBooleanSingleField(settings, "last_name_first", true)
            val cardColors = getOrNewMutableListField(settings, "card_colors")
            addEnumerationListFieldElement(cardColors, "orange")
            GetCompositionResult.Entity(settings)
        }
        every {
            delegate.getCompositionSet(
                "/address_book/person",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("first_name", "last_name", "hero_name"),
                ofType()
            )
        } answers {
            val keys = arg<List<SingleFieldModel>>(2)
            assertEquals(1, keys.size)
            val persons = arg<MutableSetFieldModel>(4)

            val idInRequest = (keys.first().value as PrimitiveModel).value as String
            val person = getNewMutableSetEntity(persons)
            setUuidSingleField(person, "id", idInRequest)
            setStringSingleField(person, "first_name", "Clark")
            setStringSingleField(person, "last_name", "Kent")
            setStringSingleField(person, "hero_name", "Superman")

            GetCompositionSetResult.Entities(listOf(person))
        }
        every {
            delegate.getCompositionSet(
                "/address_book/person",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("first_name", "last_name"),
                ofType()
            )
        } answers {
            val keys = arg<List<SingleFieldModel>>(2)
            assertEquals(1, keys.size)
            val persons = arg<MutableSetFieldModel>(4)

            val idInRequest = (keys.first().value as PrimitiveModel).value as String
            val person = getNewMutableSetEntity(persons)
            setUuidSingleField(person, "id", idInRequest)
            setStringSingleField(person, "first_name", "Lois")
            setStringSingleField(person, "last_name", "Lane")

            GetCompositionSetResult.Entities(listOf(person))
        }
        every {
            delegate.getCompositionSet(
                "/address_book/person",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("last_name"),
                ofType()
            )
        } answers {
            val persons = arg<MutableSetFieldModel>(4)

            val person = getNewMutableSetEntity(persons)
            setUuidSingleField(person, "id", "ec983c56-320f-4d66-9dde-f180e8ac3807")
            setStringSingleField(person, "last_name", "Olsen")

            GetCompositionSetResult.Entities(listOf(person))
        }
        every {
            delegate.getCompositionSet(
                "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("relationship"),
                ofType()
            )
        } answers {
            val keys = arg<List<SingleFieldModel>>(2)
            assertEquals(1, keys.size)
            val clarkRelations = arg<MutableSetFieldModel>(4)

            val idInRequest = (keys.first().value as PrimitiveModel).value as String
            val clarkRelationToLois = addRelation(
                clarkRelations,
                idInRequest,
                "colleague",
                null,
                ""
            )
            GetCompositionSetResult.Entities(listOf(clarkRelationToLois))
        }
        every {
            delegate.getCompositionSet(
                "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("relationship", "person"),
                ofType()
            )
        } answers {
            val clarkRelations = arg<MutableSetFieldModel>(4)

            val clarkRelationToJimmy = addRelation(
                clarkRelations,
                "3c71ede8-8ded-4038-b6e9-dcc4a0f3a8ce",
                "colleague",
                "ec983c56-320f-4d66-9dde-f180e8ac3807",
                ""
            )
            GetCompositionSetResult.Entities(listOf(clarkRelationToJimmy))
        }
        every {
            delegate.getCompositionSet(
                "/address_book/city_info",
                ofType(),
                fieldsWithNames("city"),
                fieldsWithNames("info", "related_city_info"),
                ofType()
            )
        } answers {
            val keys = arg<List<SingleFieldModel>>(2)
            assertEquals(1, keys.size)
            val cityInfo = arg<MutableSetFieldModel>(4)

            val city = keys.first().value as EntityModel
            val cityKeys = city.getKeyValues()
            val name = cityKeys[0] as String
            val state = cityKeys[1] as String
            val country = cityKeys[2] as String
            val newYork = getNewMutableSetEntity(cityInfo)
            addCity(newYork, name, state, country)
            setStringSingleField(
                newYork,
                "info",
                "One of the most populous and most densely populated major city in USA"
            )
            val newYorkRelated = getOrNewMutableListField(newYork, "related_city_info")
            addRelatedCity(
                newYorkRelated,
                "Albany",
                "New York",
                "United States of America",
                ""
            )

            GetCompositionSetResult.Entities(listOf(newYork))
        }
        every {
            delegate.getCompositionSet(
                "/address_book/city_info",
                ofType(),
                fieldsWithNames("city"),
                fieldsWithNames("info"),
                ofType()
            )
        } answers {
            val keys = arg<List<SingleFieldModel>>(2)
            assertEquals(1, keys.size)
            val cityInfo = arg<MutableSetFieldModel>(4)

            val city = keys.first().value as EntityModel
            val cityKeys = city.getKeyValues()
            val name = cityKeys[0] as String
            val state = cityKeys[1] as String
            val country = cityKeys[2] as String
            val albany = getNewMutableSetEntity(cityInfo)
            addCity(albany, name, state, country)
            setStringSingleField(albany, "info", "Capital of New York state")

            GetCompositionSetResult.Entities(listOf(albany))
        }
        every {
            delegate.getCompositionSet(
                "/address_book/city_info",
                ofType(),
                fieldsWithNames("city"),
                fieldsWithNames(),
                ofType()
            )
        } answers {
            val cityInfo = arg<MutableSetFieldModel>(4)

            val princeton = getNewMutableSetEntity(cityInfo)
            addCity(princeton, "Princeton", "New Jersey", "United States of America")

            val sanFrancisco = getNewMutableSetEntity(cityInfo)
            addCity(sanFrancisco, "San Francisco", "California", "United States of America")

            GetCompositionSetResult.Entities(listOf(princeton, sanFrancisco))
        }

        val response = AddressBookMutableMainModelFactory.createInstance()
        val errors = get(request, delegate, null, null, response)
        verifySequence {
            delegate.getComposition("/address_book", ofType(), fieldsWithNames("name", "last_updated"), ofType())
            delegate.getComposition(
                "/address_book/settings",
                ofType(),
                fieldsWithNames("last_name_first", "card_colors"),
                ofType()
            )
            delegate.getCompositionSet(
                "/address_book/person",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("first_name", "last_name", "hero_name"),
                ofType()
            )
            delegate.getCompositionSet(
                "/address_book/person",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("first_name", "last_name"),
                ofType()
            )
            delegate.getCompositionSet(
                "/address_book/person",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("last_name"),
                ofType()
            )
            delegate.getCompositionSet(
                "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("relationship"),
                ofType()
            )
            delegate.getCompositionSet(
                "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("relationship", "person"),
                ofType()
            )
            delegate.getCompositionSet(
                "/address_book/city_info",
                ofType(),
                fieldsWithNames("city"),
                fieldsWithNames("info", "related_city_info"),
                ofType()
            )
            delegate.getCompositionSet(
                "/address_book/city_info",
                ofType(),
                fieldsWithNames("city"),
                fieldsWithNames("info"),
                ofType()
            )
            delegate.getCompositionSet(
                "/address_book/city_info",
                ofType(),
                fieldsWithNames("city"),
                fieldsWithNames(),
                ofType()
            )
        }
        assertTrue(errors is Errors.None)
        assertMatchesJson(
            response,
            "org/treeWare/model/operator/get_response_specific_and_wildcard_entities.json",
            EncodePasswords.ALL
        )
    }
}