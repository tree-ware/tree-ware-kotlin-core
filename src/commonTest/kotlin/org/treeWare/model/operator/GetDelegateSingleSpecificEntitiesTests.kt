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

class GetDelegateSingleSpecificEntitiesTests {
    @Test
    fun `get() must call its delegate for single specific entities in a request`() {
        val request =
            getMainModelFromJsonFile(
                addressBookMetaModel,
                "org/treeWare/model/operator/get_request_single_specific_entities.json"
            )

        val delegate = mockk<GetDelegate>()
        every {
            delegate.getComposition("/address_book", ofType(), fieldsWithNames(), ofType())
        } answers {
            val addressBookField = arg<MutableSingleFieldModel>(3)
            val addressBook = addressBookField.getOrNewValue() as MutableEntityModel
            GetCompositionResult.Entity(addressBook)
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
                "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("relationship", "person"),
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
                "a8aacf55-7810-4b43-afe5-4344f25435fd",
                ""
            )
            GetCompositionSetResult.Entities(listOf(clarkRelationToLois))
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

        val response = AddressBookMutableMainModelFactory.getNewInstance()
        val errors = get(request, delegate, null, null, response)
        verifySequence {
            delegate.getComposition("/address_book", ofType(), fieldsWithNames(), ofType())
            delegate.getCompositionSet(
                "/address_book/person",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("first_name", "last_name", "hero_name"),
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
        }
        assertTrue(errors is Response.Success)
        assertMatchesJson(
            response,
            "org/treeWare/model/operator/get_response_single_specific_entities.json",
            EncodePasswords.ALL
        )
    }
}