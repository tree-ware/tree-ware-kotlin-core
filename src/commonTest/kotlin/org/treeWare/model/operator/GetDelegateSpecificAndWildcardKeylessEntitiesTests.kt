package org.treeWare.model.operator

import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import org.treeWare.mockk.fieldsWithNames
import org.treeWare.model.AddressBookMutableEntityModelFactory
import org.treeWare.model.addCity
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

class GetDelegateSpecificAndWildcardKeylessEntitiesTests {
    @Test
    fun `get() must call its delegate for specific and wildcard entities in a request`() {
        val request = AddressBookMutableEntityModelFactory.create()
        decodeJsonFileIntoEntity(
            "org/treeWare/model/operator/get_request_specific_and_wildcard_keyless_entities.json",
            entity = request
        )

        val delegate = mockk<GetDelegate>()
        every {
            delegate.getRoot("/", ofType(), listOf(), ofType())
        } answers {
            val addressBook = arg<MutableEntityModel>(3)
            GetCompositionResult.Entity(addressBook)
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

            val nycCompositeKey = keys.first().value as EntityModel
            val nycKeys = nycCompositeKey.getKeyValues()
            val nycName = nycKeys[0] as String
            val nycState = nycKeys[1] as String
            val nycCountry = nycKeys[2] as String
            val nyc = getNewMutableSetEntity(cityInfo)
            addCity(nyc, nycName, nycState, nycCountry)
            setStringSingleField(
                nyc,
                "info",
                "One of the most populous and most densely populated major city in USA"
            )

            GetCompositionSetResult.Entities(listOf(nyc))
        } andThenAnswer {
            val keys = arg<List<SingleFieldModel>>(2)
            assertEquals(1, keys.size)
            val cityInfo = arg<MutableSetFieldModel>(4)

            // Since city/name is a wildcard in the request, both NYC and Albany match and are returned.

            val nycCompositeKey = keys.first().value as EntityModel
            val nycKeys = nycCompositeKey.getKeyValues()
            val nycName = "New York City"
            val nycState = nycKeys[1] as String
            val nycCountry = nycKeys[2] as String
            val nyc = getNewMutableSetEntity(cityInfo)
            addCity(nyc, nycName, nycState, nycCountry)
            setStringSingleField(
                nyc,
                "info",
                "One of the most populous and most densely populated major city in USA"
            )

            val albanyCompositeKey = keys.first().value as EntityModel
            val albanyKeys = albanyCompositeKey.getKeyValues()
            val albanyName = "Albany"
            val albanyState = albanyKeys[1] as String
            val albanyCountry = albanyKeys[2] as String
            val albany = getNewMutableSetEntity(cityInfo)
            addCity(albany, albanyName, albanyState, albanyCountry)
            setStringSingleField(
                albany,
                "info",
                "Capital of New York state"
            )

            GetCompositionSetResult.Entities(listOf(nyc, albany))
        }
        every {
            delegate.getComposition(
                "/cities/New York City/New York/United States of America/keyless",
                ofType(),
                fieldsWithNames("name"),
                ofType()
            )
        } answers {
            val keylessField = arg<MutableSingleFieldModel>(3)
            val keyless = keylessField.getOrNewValue() as MutableEntityModel
            setStringSingleField(keyless, "name", "NYC keyless")
            GetCompositionResult.Entity(keyless)
        }
        every {
            delegate.getComposition(
                "/cities/Albany/New York/United States of America/keyless",
                ofType(),
                fieldsWithNames(),
                ofType()
            )
        } answers {
            val keylessField = arg<MutableSingleFieldModel>(3)
            val keyless = keylessField.getOrNewValue() as MutableEntityModel
            GetCompositionResult.Entity(keyless)
        }
        every {
            delegate.getComposition(
                "/cities/Albany/New York/United States of America/keyless/keyless_child",
                ofType(),
                fieldsWithNames("name"),
                ofType()
            )
        } answers {
            val keylessChildField = arg<MutableSingleFieldModel>(3)
            val keylessChild = keylessChildField.getOrNewValue() as MutableEntityModel
            setStringSingleField(keylessChild, "name", "Albany keyless child")
            GetCompositionResult.Entity(keylessChild)
        }

        val response = AddressBookMutableEntityModelFactory.create()
        val errors = get(request, delegate, null, null, response)
        verifySequence {
            delegate.getRoot("/", ofType(), listOf(), ofType())
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
            delegate.getComposition(
                "/cities/New York City/New York/United States of America/keyless",
                ofType(),
                fieldsWithNames("name"),
                ofType()
            )
            delegate.getComposition(
                "/cities/Albany/New York/United States of America/keyless",
                ofType(),
                fieldsWithNames(),
                ofType()
            )
            delegate.getComposition(
                "/cities/Albany/New York/United States of America/keyless/keyless_child",
                ofType(),
                fieldsWithNames("name"),
                ofType()
            )
        }
        assertTrue(errors is Response.Success)
        assertMatchesJson(
            response,
            "org/treeWare/model/operator/get_response_specific_and_wildcard_keyless_entities.json",
            EncodePasswords.ALL
        )
    }
}