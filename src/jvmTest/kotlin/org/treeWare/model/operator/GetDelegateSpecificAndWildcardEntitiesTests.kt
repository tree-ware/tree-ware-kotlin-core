package org.treeWare.model.operator

import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import org.treeWare.metaModel.newAddressBookMetaModel
import org.treeWare.mockk.fieldsWithNames
import org.treeWare.model.*
import org.treeWare.model.core.*
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.operator.get.GetDelegate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val metaModel = newAddressBookMetaModel(null, null).metaModel
    ?: throw IllegalStateException("Meta-model has validation errors")

class GetDelegateSpecificAndWildcardEntitiesTests {
    @Test
    fun `get() must call its delegate for specific and wildcard entities in a request`() {
        val request =
            getMainModelFromJsonFile(
                metaModel,
                "org/treeWare/model/operator/get_request_specific_and_wildcard_entities.json"
            )

        val delegate = mockk<GetDelegate>()
        every {
            delegate.fetchComposition("/", ofType(), fieldsWithNames("name", "last_updated"), ofType())
        } answers {
            val addressBookField = arg<MutableSingleFieldModel>(3)
            val addressBook = addressBookField.getOrNewValue() as MutableEntityModel
            setStringSingleField(addressBook, "name", "Super Heroes")
            setTimestampSingleField(addressBook, "last_updated", 1587147731L)
            addressBook
        }
        every {
            delegate.fetchComposition(
                "/address_book",
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
            settings
        }
        every {
            delegate.fetchCompositionSet(
                "/address_book",
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

            listOf(person)
        }
        every {
            delegate.fetchCompositionSet(
                "/address_book",
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

            listOf(person)
        }
        every {
            delegate.fetchCompositionSet(
                "/address_book",
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

            listOf(person)
        }
        every {
            delegate.fetchCompositionSet(
                "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]",
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
            listOf(clarkRelationToLois)
        }
        every {
            delegate.fetchCompositionSet(
                "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]",
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
            listOf(clarkRelationToJimmy)
        }
        every {
            delegate.fetchCompositionSet(
                "/address_book",
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

            listOf(newYork)
        }
        every {
            delegate.fetchCompositionSet(
                "/address_book",
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

            listOf(albany)
        }
        every {
            delegate.fetchCompositionSet(
                "/address_book",
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

            listOf(princeton, sanFrancisco)
        }

        val response = get(request, delegate, null)
        verifySequence {
            delegate.fetchComposition("/", ofType(), fieldsWithNames("name", "last_updated"), ofType())
            delegate.fetchComposition(
                "/address_book",
                ofType(),
                fieldsWithNames("last_name_first", "card_colors"),
                ofType()
            )
            delegate.fetchCompositionSet(
                "/address_book",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("first_name", "last_name", "hero_name"),
                ofType()
            )
            delegate.fetchCompositionSet(
                "/address_book",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("first_name", "last_name"),
                ofType()
            )
            delegate.fetchCompositionSet(
                "/address_book",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("last_name"),
                ofType()
            )
            delegate.fetchCompositionSet(
                "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("relationship"),
                ofType()
            )
            delegate.fetchCompositionSet(
                "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("relationship", "person"),
                ofType()
            )
            delegate.fetchCompositionSet(
                "/address_book",
                ofType(),
                fieldsWithNames("city"),
                fieldsWithNames("info", "related_city_info"),
                ofType()
            )
            delegate.fetchCompositionSet(
                "/address_book",
                ofType(),
                fieldsWithNames("city"),
                fieldsWithNames("info"),
                ofType()
            )
            delegate.fetchCompositionSet(
                "/address_book",
                ofType(),
                fieldsWithNames("city"),
                fieldsWithNames(),
                ofType()
            )
        }
        assertTrue(response is GetResponse.Model)
        assertMatchesJson(
            response.model,
            "org/treeWare/model/operator/get_response_specific_and_wildcard_entities.json",
            EncodePasswords.ALL
        )
    }
}