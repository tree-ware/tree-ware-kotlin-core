package org.treeWare.model.operator

import org.treeWare.metaModel.addressBookRootEntityFactory
import org.treeWare.model.core.EntityModel
import org.treeWare.model.decodeJsonStringIntoEntity
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAuxStateMachine
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Validate that DELETE set-requests are not verified for the presence or absence of conditional-fields.
 */
class ValidateSetDeleteFieldExistenceTests {
    // region Missing exists_if fields

    @Test
    fun `validateSet() must not return errors if exists_if fields are not found`() {
        val modelJson = """
            |{
            |  "persons": [
            |    {
            |      "set_": "delete",
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent"
            |    }
            |  ],
            |  "cities": [
            |    {
            |      "set_": "delete",
            |      "city": {
            |        "name": "San Francisco",
            |        "state": "CA",
            |        "country": "USA"
            |      },
            |      "info": "Popular city in northern California"
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = getModel(modelJson)

        val expectedErrors = emptyList<String>()
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if exists_if fields are not found in an ancestor that is not being set`() {
        val modelJson = """
            |{
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "hero_name": "Superman",
            |      "relations": [
            |        {
            |          "set_": "delete",
            |          "id": "05ade278-4b44-43da-a0cc-14463854e397",
            |          "relationship": "colleague"
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = getModel(modelJson)

        val expectedErrors = emptyList<String>()
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    // endregion

    // region Conditions are met

    @Test
    fun `validateSet() must not return errors if conditionally-required fields do not exist when conditions are met`() {
        val modelJson = """
            |{
            |  "cities": [
            |    {
            |      "set_": "delete",
            |      "city": {
            |        "name": "San Francisco",
            |        "state": "CA",
            |        "country": "USA"
            |      },
            |      "info": "Popular city in northern California",
            |      "is_coastal_city": true
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = getModel(modelJson)

        val expectedErrors = emptyList<String>()
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if conditionally-required fields exist when conditions are met`() {
        val modelJson = """
            |{
            |  "cities": [
            |    {
            |      "set_": "delete",
            |      "city": {
            |        "name": "San Francisco",
            |        "state": "CA",
            |        "country": "USA"
            |      },
            |      "info": "Popular city in northern California",
            |      "is_coastal_city": true,
            |      "water_body_name": "Pacific Ocean"
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = getModel(modelJson)

        val expectedErrors = emptyList<String>()
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if conditionally-optional fields do not exist when conditions are met`() {
        val modelJson = """
            |{
            |  "persons": [
            |    {
            |      "set_": "delete",
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "is_hero": true
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = getModel(modelJson)

        val expectedErrors = emptyList<String>()
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if conditionally-optional fields exist when conditions are met`() {
        val modelJson = """
            |{
            |  "persons": [
            |    {
            |      "set_": "delete",
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "is_hero": true,
            |      "hero_name": "Superman"
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = getModel(modelJson)

        val expectedErrors = emptyList<String>()
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if required fields in a conditionally-optional entity do not exist when conditions are met`() {
        val modelJson = """
            |{
            |  "persons": [
            |    {
            |      "set_": "delete",
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "is_hero": true,
            |      "hero_details": {
            |        "set_": "delete"
            |      }
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = getModel(modelJson)

        val expectedErrors = emptyList<String>()
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if required fields in a conditionally-optional entity exist when conditions are met`() {
        val modelJson = """
            |{
            |  "persons": [
            |    {
            |      "set_": "delete",
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "is_hero": true,
            |      "hero_details": {
            |        "set_": "delete",
            |        "strengths": "super-strength",
            |        "weaknesses": "kryptonite"
            |      }
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = getModel(modelJson)

        val expectedErrors = emptyList<String>()
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    // endregion

    // region Conditions are not met

    @Test
    fun `validateSet() must not return errors if conditionally-required fields exist when conditions are not met`() {
        val modelJson = """
            |{
            |  "cities": [
            |    {
            |      "set_": "delete",
            |      "city": {
            |        "name": "Sacramento",
            |        "state": "CA",
            |        "country": "USA"
            |      },
            |      "info": "Capital of California",
            |      "is_coastal_city": false,
            |      "water_body_name": "Pacific Ocean"
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = getModel(modelJson)

        val expectedErrors = emptyList<String>()
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if conditionally-required fields do not exist when conditions are not met`() {
        val modelJson = """
            |{
            |  "cities": [
            |    {
            |      "set_": "delete",
            |      "city": {
            |        "name": "Sacramento",
            |        "state": "CA",
            |        "country": "USA"
            |      },
            |      "info": "Capital of California",
            |      "is_coastal_city": false
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = getModel(modelJson)

        val expectedErrors = emptyList<String>()
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if conditionally-optional fields exist when conditions are not met`() {
        val modelJson = """
            |{
            |  "persons": [
            |    {
            |      "set_": "delete",
            |      "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |      "first_name": "Lois",
            |      "last_name": "Lane",
            |      "is_hero": false,
            |      "hero_name": "SuperLois"
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = getModel(modelJson)

        val expectedErrors = emptyList<String>()
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if conditionally-optional fields do not exist when conditions are not met`() {
        val modelJson = """
            |{
            |  "persons": [
            |    {
            |      "set_": "delete",
            |      "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |      "first_name": "Lois",
            |      "last_name": "Lane",
            |      "is_hero": false
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = getModel(modelJson)

        val expectedErrors = emptyList<String>()
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if required fields in a conditionally-optional entity exist when conditions are not met`() {
        val modelJson = """
            |{
            |  "persons": [
            |    {
            |      "set_": "delete",
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "is_hero": false,
            |      "hero_details": {
            |        "set_": "delete",
            |        "strengths": "super-strength",
            |        "weaknesses": "kryptonite"
            |      }
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = getModel(modelJson)

        val expectedErrors = emptyList<String>()
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if required fields in a conditionally-optional entity do not exist when conditions are not met`() {
        val modelJson = """
            |{
            |  "persons": [
            |    {
            |      "set_": "delete",
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "is_hero": false,
            |      "hero_details": {
            |        "set_": "delete"
            |      }
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = getModel(modelJson)

        val expectedErrors = emptyList<String>()
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    // endregion
}

private val auxDecodingFactory = MultiAuxDecodingStateMachineFactory(SET_AUX_NAME to { SetAuxStateMachine(it) })

private fun getModel(modelJson: String): EntityModel {
    val model = addressBookRootEntityFactory(null)
    decodeJsonStringIntoEntity(
        modelJson,
        multiAuxDecodingStateMachineFactory = auxDecodingFactory,
        entity = model
    )
    return model
}