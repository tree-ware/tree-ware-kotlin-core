package org.treeWare.model.operator

import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import org.treeWare.metaModel.addressBookRootEntityFactory
import org.treeWare.mockk.fieldsWithNames
import org.treeWare.model.addCity
import org.treeWare.model.addRelation
import org.treeWare.model.assertMatchesJson
import org.treeWare.model.core.*
import org.treeWare.model.decodeJsonFileIntoEntity
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.operator.get.GetCompositionResult
import org.treeWare.model.operator.get.GetCompositionSetResult
import org.treeWare.model.operator.get.GetDelegate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetDelegateMultipleSpecificEntitiesTests {
    @Test
    fun `get() must call its delegate for multiple specific entities in a request`() {
        val request = addressBookRootEntityFactory(null)
        decodeJsonFileIntoEntity(
            "org/treeWare/model/operator/get_request_multiple_specific_entities.json",
            entity = request
        )

        val delegate = mockk<GetDelegate>()
        every {
            delegate.getRoot("/", ofType(), fieldsWithNames(), ofType())
        } answers {
            val responseRootEntity = arg<MutableEntityModel>(3)
            GetCompositionResult.Entity(responseRootEntity)
        }
        every {
            delegate.getCompositionSet(
                "/persons",
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
                "/persons",
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
                "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relations",
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
                "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relations",
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
            val clarkRelationToJimmy = addRelation(
                clarkRelations,
                idInRequest,
                "colleague",
                null,
                ""
            )
            GetCompositionSetResult.Entities(listOf(clarkRelationToJimmy))
        }
        every {
            delegate.getCompositionSet(
                "/cities",
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
            val newYork = getNewMutableSetEntity(cityInfo)
            addCity(newYork, name, state, country)
            setStringSingleField(
                newYork,
                "info",
                "One of the most populous and most densely populated major city in USA"
            )

            GetCompositionSetResult.Entities(listOf(newYork))
        } andThenAnswer {
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

        val response = addressBookRootEntityFactory(null)
        val errors = get(request, delegate, null, null, response)
        verifySequence {
            delegate.getRoot("/", ofType(), fieldsWithNames(), ofType())
            delegate.getCompositionSet(
                "/persons",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("first_name", "last_name", "hero_name"),
                ofType()
            )
            delegate.getCompositionSet(
                "/persons",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("first_name", "last_name"),
                ofType()
            )
            delegate.getCompositionSet(
                "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relations",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("relationship", "person"),
                ofType()
            )
            delegate.getCompositionSet(
                "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relations",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("relationship"),
                ofType()
            )
            delegate.getCompositionSet(
                "/cities",
                ofType(),
                fieldsWithNames("city"),
                fieldsWithNames("info"),
                ofType()
            )
            delegate.getCompositionSet(
                "/cities",
                ofType(),
                fieldsWithNames("city"),
                fieldsWithNames("info"),
                ofType()
            )
        }
        assertTrue(errors is Response.Success)
        assertMatchesJson(
            response,
            "org/treeWare/model/operator/get_response_multiple_specific_entities.json",
            EncodePasswords.ALL
        )
    }
}