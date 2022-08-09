package org.treeWare.model.operator

import org.treeWare.metaModel.addressBookMetaModel
import org.treeWare.model.core.MainModel
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.getMainModelFromJsonString
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAuxStateMachine
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Validate that UPDATE set-requests are verified for the absence of conditional-fields.
 * Presence of conditional fields are not verified for UPDATE requests because even non-conditional
 * required fields are optional in UPDATE requests.
 */
class ValidateSetUpdateFieldExistenceTests {
    // region Missing exists_if fields

    @Test
    fun `validateSet() must return errors if exists_if fields are not found`() {
        val modelJson = """
            |{
            |  "address_book": {
            |    "person": [
            |      {
            |        "set_": "update",
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent"
            |      }
            |    ],
            |    "city_info": [
            |      {
            |        "set_": "update",
            |        "city": {
            |          "name": "San Francisco",
            |          "state": "CA",
            |          "country": "USA"
            |        },
            |        "info": "Popular city in northern California"
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val model = getModel(modelJson)

        val expectedErrors = listOf(
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]: field is_hero in exists_if not found",
            "/address_book/city_info[San Francisco,CA,USA]: field is_coastal_city in exists_if not found",
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if exists_if fields are not found in an ancestor that is not being set`() {
        val modelJson = """
            |{
            |  "address_book": {
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "hero_name": "Superman",
            |        "relation": [
            |          {
            |            "set_": "update",
            |            "id": "05ade278-4b44-43da-a0cc-14463854e397",
            |            "relationship": "colleague"
            |          }
            |        ]
            |      }
            |    ]
            |  }
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
            |  "address_book": {
            |    "city_info": [
            |      {
            |        "set_": "update",
            |        "city": {
            |          "name": "San Francisco",
            |          "state": "CA",
            |          "country": "USA"
            |        },
            |        "info": "Popular city in northern California",
            |        "is_coastal_city": true
            |      }
            |    ]
            |  }
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
            |  "address_book": {
            |    "city_info": [
            |      {
            |        "set_": "update",
            |        "city": {
            |          "name": "San Francisco",
            |          "state": "CA",
            |          "country": "USA"
            |        },
            |        "info": "Popular city in northern California",
            |        "is_coastal_city": true,
            |        "water_body_name": "Pacific Ocean"
            |      }
            |    ]
            |  }
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
            |  "address_book": {
            |    "person": [
            |      {
            |        "set_": "update",
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent",
            |        "is_hero": true
            |      }
            |    ]
            |  }
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
            |  "address_book": {
            |    "person": [
            |      {
            |        "set_": "update",
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent",
            |        "is_hero": true,
            |        "hero_name": "Superman"
            |      }
            |    ]
            |  }
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
            |  "address_book": {
            |    "person": [
            |      {
            |        "set_": "update",
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent",
            |        "is_hero": true,
            |        "hero_details": {}
            |      }
            |    ]
            |  }
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
            |  "address_book": {
            |    "person": [
            |      {
            |        "set_": "update",
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent",
            |        "is_hero": true,
            |        "hero_details": {
            |          "strengths": "super-strength",
            |          "weaknesses": "kryptonite"
            |        }
            |      }
            |    ]
            |  }
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
            |  "address_book": {
            |    "city_info": [
            |      {
            |        "set_": "update",
            |        "city": {
            |          "name": "Sacramento",
            |          "state": "CA",
            |          "country": "USA"
            |        },
            |        "info": "Capital of California",
            |        "is_coastal_city": false,
            |        "water_body_name": "Pacific Ocean"
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val model = getModel(modelJson)

        val expectedErrors = listOf(
            "/address_book/city_info[Sacramento,CA,USA]: conditions are not met for conditional-field water_body_name, but field is found",
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if conditionally-required fields do not exist when conditions are not met`() {
        val modelJson = """
            |{
            |  "address_book": {
            |    "city_info": [
            |      {
            |        "set_": "update",
            |        "city": {
            |          "name": "Sacramento",
            |          "state": "CA",
            |          "country": "USA"
            |        },
            |        "info": "Capital of California",
            |        "is_coastal_city": false
            |      }
            |    ]
            |  }
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
            |  "address_book": {
            |    "person": [
            |      {
            |        "set_": "update",
            |        "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |        "first_name": "Lois",
            |        "last_name": "Lane",
            |        "is_hero": false,
            |        "hero_name": "SuperLois"
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val model = getModel(modelJson)

        val expectedErrors = listOf(
            "/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]: conditions are not met for conditional-field hero_name, but field is found",
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if conditionally-optional fields do not exist when conditions are not met`() {
        val modelJson = """
            |{
            |  "address_book": {
            |    "person": [
            |      {
            |        "set_": "update",
            |        "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |        "first_name": "Lois",
            |        "last_name": "Lane",
            |        "is_hero": false
            |      }
            |    ]
            |  }
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
            |  "address_book": {
            |    "person": [
            |      {
            |        "set_": "update",
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent",
            |        "is_hero": false,
            |        "hero_details": {
            |          "strengths": "super-strength",
            |          "weaknesses": "kryptonite"
            |        }
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val model = getModel(modelJson)

        val expectedErrors = listOf(
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]: conditions are not met for conditional-field hero_details, but field is found",
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if required fields in a conditionally-optional entity do not exist when conditions are not met`() {
        val modelJson = """
            |{
            |  "address_book": {
            |    "person": [
            |      {
            |        "set_": "update",
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent",
            |        "is_hero": false,
            |        "hero_details": {}
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val model = getModel(modelJson)

        val expectedErrors = listOf(
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]: conditions are not met for conditional-field hero_details, but field is found",
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    // endregion
}

private val auxDecodingFactory = MultiAuxDecodingStateMachineFactory(SET_AUX_NAME to { SetAuxStateMachine(it) })

private fun getModel(modelJson: String): MainModel = getMainModelFromJsonString(
    addressBookMetaModel,
    modelJson,
    multiAuxDecodingStateMachineFactory = auxDecodingFactory
)