package org.treeWare.model.operator

import org.treeWare.model.AddressBookMutableEntityModelFactory
import org.treeWare.model.core.EntityModel
import org.treeWare.model.decodeJsonStringIntoEntity
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAuxStateMachine
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Validate that CREATE set-requests are verified for the presence & absence of conditional-fields.
 */
class ValidateSetCreateFieldExistenceTests {
    // region Missing exists_if fields

    @Test
    fun `validateSet() must return errors if exists_if fields are not found`() {
        val modelJson = """
            |{
            |  "persons": [
            |    {
            |      "set_": "create",
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent"
            |    }
            |  ],
            |  "cities": [
            |    {
            |      "set_": "create",
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

        val expectedErrors = listOf(
            "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f: field is_hero not found; it is needed for validating other fields",
            "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f: required field not found: is_hero",
            "/cities/San Francisco/CA/USA: field is_coastal_city not found; it is needed for validating other fields",
        )
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
            |          "set_": "create",
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
    fun `validateSet() must return errors if conditionally-required fields do not exist when conditions are met`() {
        val modelJson = """
            |{
            |  "cities": [
            |    {
            |      "set_": "create",
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

        val expectedErrors = listOf(
            "/cities/San Francisco/CA/USA: conditions are met for required conditional-field water_body_name, but field is not found",
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if conditionally-required fields exist when conditions are met`() {
        val modelJson = """
            |{
            |  "cities": [
            |    {
            |      "set_": "create",
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
            |      "set_": "create",
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
            |      "set_": "create",
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
    fun `validateSet() must return errors if required fields in a conditionally-optional entity do not exist when conditions are met`() {
        val modelJson = """
            |{
            |  "persons": [
            |    {
            |      "set_": "create",
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "is_hero": true,
            |      "hero_details": {}
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = getModel(modelJson)

        val expectedErrors = listOf(
            "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/hero_details: required field not found: strengths",
            "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/hero_details: required field not found: weaknesses",
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if required fields in a conditionally-optional entity exist when conditions are met`() {
        val modelJson = """
            |{
            |  "persons": [
            |    {
            |      "set_": "create",
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "is_hero": true,
            |      "hero_details": {
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
    fun `validateSet() must return errors if conditionally-required fields exist when conditions are not met`() {
        val modelJson = """
            |{
            |  "cities": [
            |    {
            |      "set_": "create",
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

        val expectedErrors = listOf(
            "/cities/Sacramento/CA/USA: conditions are not met for conditional-field water_body_name, but field is found",
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if conditionally-required fields do not exist when conditions are not met`() {
        val modelJson = """
            |{
            |  "cities": [
            |    {
            |      "set_": "create",
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
    fun `validateSet() must return errors if conditionally-optional fields exist when conditions are not met`() {
        val modelJson = """
            |{
            |  "persons": [
            |    {
            |      "set_": "create",
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

        val expectedErrors = listOf(
            "/persons/a8aacf55-7810-4b43-afe5-4344f25435fd: conditions are not met for conditional-field hero_name, but field is found",
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if conditionally-optional fields do not exist when conditions are not met`() {
        val modelJson = """
            |{
            |  "persons": [
            |    {
            |      "set_": "create",
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
    fun `validateSet() must return errors if required fields in a conditionally-optional entity exist when conditions are not met`() {
        val modelJson = """
            |{
            |  "persons": [
            |    {
            |      "set_": "create",
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "is_hero": false,
            |      "hero_details": {
            |        "strengths": "super-strength",
            |        "weaknesses": "kryptonite"
            |      }
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = getModel(modelJson)

        val expectedErrors = listOf(
            "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f: conditions are not met for conditional-field hero_details, but field is found",
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if required fields in a conditionally-optional entity do not exist when conditions are not met`() {
        val modelJson = """
            |{
            |  "persons": [
            |    {
            |      "set_": "create",
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "is_hero": false,
            |      "hero_details": {}
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = getModel(modelJson)

        val expectedErrors = listOf(
            "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f: conditions are not met for conditional-field hero_details, but field is found",
            "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/hero_details: required field not found: strengths",
            "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/hero_details: required field not found: weaknesses",
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    // endregion
}

private val auxDecodingFactory = MultiAuxDecodingStateMachineFactory(SET_AUX_NAME to { SetAuxStateMachine(it) })

private fun getModel(modelJson: String): EntityModel {
    val model = AddressBookMutableEntityModelFactory.create()
    decodeJsonStringIntoEntity(
        modelJson,
        multiAuxDecodingStateMachineFactory = auxDecodingFactory,
        entity = model
    )
    return model
}