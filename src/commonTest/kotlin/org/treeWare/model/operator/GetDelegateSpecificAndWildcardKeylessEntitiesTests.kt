package org.treeWare.model.operator

import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import org.treeWare.metaModel.addressBookRootEntityFactory
import org.treeWare.mockk.fieldsWithNames
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
    fun entitiesWithMultipleKeysAndKeylessChildren() {
        val request = addressBookRootEntityFactory(null)
        decodeJsonFileIntoEntity(
            "org/treeWare/model/operator/get_request_entities_with_multiple_keys_and_keyless_children.json",
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
                "/clubs",
                ofType(),
                fieldsWithNames("name", "sub_category", "category"),
                fieldsWithNames("info"),
                ofType()
            )
        } answers {
            val keys = arg<List<SingleFieldModel>>(2)
            assertEquals(3, keys.size)
            assertEquals("Super Sport", (keys[0].value as? PrimitiveModel)?.value)
            assertEquals("Gym", (keys[1].value as? PrimitiveModel)?.value)
            assertEquals("Health", (keys[2].value as? PrimitiveModel)?.value)
            val club = arg<MutableSetFieldModel>(4)

            val superSport = getNewMutableSetEntity(club)
            setStringSingleField(superSport, "name", "Super Sport")
            setStringSingleField(superSport, "sub_category", "Gym")
            setStringSingleField(superSport, "category", "Health")
            setStringSingleField(superSport, "info", "A super gym")
            GetCompositionSetResult.Entities(listOf(superSport))
        }
        every {
            delegate.getCompositionSet(
                "/clubs",
                ofType(),
                fieldsWithNames("name", "sub_category", "category"),
                fieldsWithNames("info", "phone"),
                ofType()
            )
        } answers {
            val keys = arg<List<SingleFieldModel>>(2)
            assertEquals(3, keys.size)
            assertEquals(null, (keys[0].value as? PrimitiveModel)?.value)
            assertEquals("Gym", (keys[1].value as? PrimitiveModel)?.value)
            assertEquals("Health", (keys[2].value as? PrimitiveModel)?.value)
            val club = arg<MutableSetFieldModel>(4)

            val bestGym = getNewMutableSetEntity(club)
            setStringSingleField(bestGym, "name", "Best Gym")
            setStringSingleField(bestGym, "sub_category", "Gym")
            setStringSingleField(bestGym, "category", "Health")
            setStringSingleField(bestGym, "info", "The best gym")
            setStringSingleField(bestGym, "phone", "1-800-gym-best")

            val superSport = getNewMutableSetEntity(club)
            setStringSingleField(superSport, "name", "Super Sport")
            setStringSingleField(superSport, "sub_category", "Gym")
            setStringSingleField(superSport, "category", "Health")
            setStringSingleField(superSport, "info", "A super gym")
            setStringSingleField(superSport, "phone", "1-800-gym-supe")

            GetCompositionSetResult.Entities(listOf(bestGym, superSport))
        }
        every {
            delegate.getComposition(
                "/clubs/Super Sport/Gym/Health/keyless",
                ofType(),
                fieldsWithNames("name"),
                ofType()
            )
        } answers {
            val keylessField = arg<MutableSingleFieldModel>(3)
            val keyless = keylessField.getOrNewValue() as MutableEntityModel
            setStringSingleField(keyless, "name", "Super Sport keyless")
            GetCompositionResult.Entity(keyless)
        }
        every {
            delegate.getComposition(
                "/clubs/Best Gym/Gym/Health/keyless",
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
                "/clubs/Best Gym/Gym/Health/keyless/keyless_child",
                ofType(),
                fieldsWithNames("name"),
                ofType()
            )
        } answers {
            val keylessChildField = arg<MutableSingleFieldModel>(3)
            val keylessChild = keylessChildField.getOrNewValue() as MutableEntityModel
            setStringSingleField(keylessChild, "name", "Best Gym keyless child")
            GetCompositionResult.Entity(keylessChild)
        }

        val response = addressBookRootEntityFactory(null)
        val errors = get(request, delegate, null, null, response)
        verifySequence {
            delegate.getRoot("/", ofType(), listOf(), ofType())
            delegate.getCompositionSet(
                "/clubs",
                ofType(),
                fieldsWithNames("name", "sub_category", "category"),
                fieldsWithNames("info"),
                ofType()
            )
            delegate.getCompositionSet(
                "/clubs",
                ofType(),
                fieldsWithNames("name", "sub_category", "category"),
                fieldsWithNames("info", "phone"),
                ofType()
            )
            delegate.getComposition(
                "/clubs/Super Sport/Gym/Health/keyless",
                ofType(),
                fieldsWithNames("name"),
                ofType()
            )
            delegate.getComposition(
                "/clubs/Best Gym/Gym/Health/keyless",
                ofType(),
                fieldsWithNames(),
                ofType()
            )
            delegate.getComposition(
                "/clubs/Best Gym/Gym/Health/keyless/keyless_child",
                ofType(),
                fieldsWithNames("name"),
                ofType()
            )
        }
        assertTrue(errors is Response.Success)
        assertMatchesJson(
            response,
            "org/treeWare/model/operator/get_response_entities_with_multiple_keys_and_keyless_children.json",
            EncodePasswords.ALL
        )
    }

    @Test
    fun entitiesWithCompositeKeysAndKeylessChildren() {
        val request = addressBookRootEntityFactory(null)
        decodeJsonFileIntoEntity(
            "org/treeWare/model/operator/get_request_entities_with_composite_keys_and_keyless_children.json",
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
        }
        every {
            delegate.getCompositionSet(
                "/cities",
                ofType(),
                fieldsWithNames("city"),
                fieldsWithNames("info", "is_coastal_city"),
                ofType()
            )
        } answers {
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
            setBooleanSingleField(nyc, "is_coastal_city", true)

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
            setBooleanSingleField(albany, "is_coastal_city", true)

            GetCompositionSetResult.Entities(listOf(albany, nyc))
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

        val response = addressBookRootEntityFactory(null)
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
                fieldsWithNames("info", "is_coastal_city"),
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
            "org/treeWare/model/operator/get_response_entities_with_composite_keys_and_keyless_children.json",
            EncodePasswords.ALL
        )
    }
}