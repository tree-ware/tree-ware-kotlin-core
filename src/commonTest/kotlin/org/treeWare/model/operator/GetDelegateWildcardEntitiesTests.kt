package org.treeWare.model.operator

import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import org.treeWare.mockk.fieldsWithNames
import org.treeWare.model.*
import org.treeWare.model.core.*
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.operator.get.GetCompositionResult
import org.treeWare.model.operator.get.GetCompositionSetResult
import org.treeWare.model.operator.get.GetDelegate
import kotlin.test.Test
import kotlin.test.assertTrue

class GetDelegateWildcardEntitiesTests {
    @Test
    fun `get() must call its delegate for wildcard entities in a request`() {
        val request = AddressBookMutableEntityModelFactory.create()
        decodeJsonFileIntoEntity(
            "org/treeWare/model/operator/get_request_wildcard_entities.json",
            entity = request
        )

        val delegate = mockk<GetDelegate>()
        every {
            delegate.getRoot("", ofType(), fieldsWithNames("name", "last_updated"), ofType())
        } answers {
            val addressBook = arg<MutableEntityModel>(3)
            setStringSingleField(addressBook, "name", "Super Heroes")
            setTimestampSingleField(addressBook, "last_updated", 1587147731UL)
            GetCompositionResult.Entity(addressBook)
        }
        every {
            delegate.getComposition(
                "/settings",
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
                "/person",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("first_name", "last_name", "hero_name", "picture"),
                ofType()
            )
        } answers {
            val persons = arg<MutableSetFieldModel>(4)

            val clark = getNewMutableSetEntity(persons)
            setUuidSingleField(clark, "id", "cc477201-48ec-4367-83a4-7fdbd92f8a6f")
            setStringSingleField(clark, "first_name", "Clark")
            setStringSingleField(clark, "last_name", "Kent")
            setStringSingleField(clark, "hero_name", "Superman")
            setStringSingleField(clark, "picture", "UGljdHVyZSBvZiBDbGFyayBLZW50")

            val lois = getNewMutableSetEntity(persons)
            setUuidSingleField(lois, "id", "a8aacf55-7810-4b43-afe5-4344f25435fd")
            setStringSingleField(lois, "first_name", "Lois")
            setStringSingleField(lois, "last_name", "Lane")
            setStringSingleField(lois, "picture", "UGljdHVyZSBvZiBMb2lzIExhbmU=")

            GetCompositionSetResult.Entities(listOf(clark, lois))
        }
        every {
            delegate.getCompositionSet(
                "/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("relationship", "person"),
                ofType()
            )
        } answers {
            val clarkRelations = arg<MutableSetFieldModel>(4)
            val clarkRelationToLois = addRelation(
                clarkRelations,
                "05ade278-4b44-43da-a0cc-14463854e397",
                "colleague",
                "a8aacf55-7810-4b43-afe5-4344f25435fd",
                ""
            )
            GetCompositionSetResult.Entities(listOf(clarkRelationToLois))
        }
        every {
            delegate.getCompositionSet(
                "/person/a8aacf55-7810-4b43-afe5-4344f25435fd/relation",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("relationship", "person"),
                ofType()
            )
        } answers {
            val loisRelations = arg<MutableSetFieldModel>(4)
            val loisRelationToClark = addRelation(
                loisRelations,
                "16634916-8f83-4376-ad42-37038e108a0b",
                "colleague",
                "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                ""
            )
            GetCompositionSetResult.Entities(listOf(loisRelationToClark))
        }
        every {
            delegate.getCompositionSet(
                "/city_info",
                ofType(),
                fieldsWithNames("city"),
                fieldsWithNames("info", "related_city_info"),
                ofType()
            )
        } answers {
            val cityInfo = arg<MutableSetFieldModel>(4)

            val newYork = getNewMutableSetEntity(cityInfo)
            addCity(newYork, "New York City", "New York", "United States of America")
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

            val albany = getNewMutableSetEntity(cityInfo)
            addCity(albany, "Albany", "New York", "United States of America")
            setStringSingleField(albany, "info", "Capital of New York state")
            val albanyRelated = getOrNewMutableListField(albany, "related_city_info")
            addRelatedCity(albanyRelated, "New York City", "New York", "United States of America", "")

            GetCompositionSetResult.Entities(listOf(newYork, albany))
        }

        val response = AddressBookMutableEntityModelFactory.create()
        val errors = get(request, delegate, null, null, response)

        verifySequence {
            delegate.getRoot("", ofType(), fieldsWithNames("name", "last_updated"), ofType())
            delegate.getComposition(
                "/settings",
                ofType(),
                fieldsWithNames("last_name_first", "card_colors"),
                ofType()
            )
            delegate.getCompositionSet(
                "/person",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("first_name", "last_name", "hero_name", "picture"),
                ofType()
            )
            delegate.getCompositionSet(
                "/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("relationship", "person"),
                ofType()
            )
            delegate.getCompositionSet(
                "/person/a8aacf55-7810-4b43-afe5-4344f25435fd/relation",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("relationship", "person"),
                ofType()
            )
            delegate.getCompositionSet(
                "/city_info",
                ofType(),
                fieldsWithNames("city"),
                fieldsWithNames("info", "related_city_info"),
                ofType()
            )
        }
        assertTrue(errors is Response.Success)
        assertMatchesJson(
            response,
            "org/treeWare/model/operator/get_response_wildcard_entities.json",
            EncodePasswords.ALL
        )
    }
}