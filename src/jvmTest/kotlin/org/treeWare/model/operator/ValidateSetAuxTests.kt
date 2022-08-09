package org.treeWare.model.operator

import org.treeWare.metaModel.addressBookMetaModel
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.getMainModelFromJsonString
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAuxStateMachine
import kotlin.test.Test
import kotlin.test.assertEquals

private val auxDecodingFactory = MultiAuxDecodingStateMachineFactory(SET_AUX_NAME to { SetAuxStateMachine(it) })

class ValidateSetAuxTests {
    @Test
    fun `validateSet() must return errors if no composition field or entity has set_ aux`() {
        val modelJson = """
            |{
            |  "address_book": {
            |    "name": "Super Heroes",
            |    "last_updated": "1587147731",
            |    "settings": {
            |      "last_name_first": true,
            |      "encrypt_hero_name": false
            |    },
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent"
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val model =
            getMainModelFromJsonString(
                addressBookMetaModel,
                modelJson,
                multiAuxDecodingStateMachineFactory = auxDecodingFactory
            )

        val expectedErrors = listOf("/: set_ aux not attached to any composition field or entity")
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors for non-composition-field elements that have set_ aux`() {
        val modelJson = """
            |{
            |  "address_book": {
            |    "name__set_": "update",
            |    "name": "Super Heroes",
            |    "last_updated": "1587147731",
            |    "settings": {
            |      "last_name_first__set_": "create",
            |      "last_name_first": true,
            |      "encrypt_hero_name__set_": "create",
            |      "encrypt_hero_name": false
            |    },
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name__set_": "delete",
            |        "first_name": "Clark",
            |        "last_name__set_": "delete",
            |        "last_name": "Kent"
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val model =
            getMainModelFromJsonString(
                addressBookMetaModel,
                modelJson,
                multiAuxDecodingStateMachineFactory = auxDecodingFactory
            )

        val expectedErrors = listOf("/: set_ aux not attached to any composition field or entity")
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if a create sub-tree contains non-create set_ aux`() {
        val modelJson = """
            |{
            |  "address_book__set_": "create",
            |  "address_book": {
            |    "name": "Super Heroes",
            |    "last_updated": "1587147731",
            |    "settings__set_": "delete",
            |    "settings": {
            |      "last_name_first": true,
            |      "encrypt_hero_name": false
            |    },
            |    "person__set_": "update",
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent",
            |        "is_hero": true
            |      },
            |      {
            |        "set_": "delete",
            |        "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val model =
            getMainModelFromJsonString(
                addressBookMetaModel,
                modelJson,
                multiAuxDecodingStateMachineFactory = auxDecodingFactory
            )

        val expectedErrors = listOf(
            "/address_book/settings: `delete` must not be in the subtree of a `create`",
            "/address_book/person: `update` must not be in the subtree of a `create`",
            "/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]: `delete` must not be in the subtree of a `create`"
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if entities in a delete sub-tree do not have a set_ aux`() {
        val modelJson = """
            |{
            |  "address_book": {
            |    "set_": "delete",
            |    "name": "Super Heroes",
            |    "last_updated": "1587147731",
            |    "settings": {
            |      "last_name_first": true,
            |      "encrypt_hero_name": false
            |    },
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent"
            |      },
            |      {
            |        "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val model =
            getMainModelFromJsonString(
                addressBookMetaModel,
                modelJson,
                multiAuxDecodingStateMachineFactory = auxDecodingFactory
            )

        val expectedErrors = listOf(
            "/address_book/settings: entity without `delete` must not be in the subtree of a `delete`",
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]: entity without `delete` must not be in the subtree of a `delete`",
            "/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]: entity without `delete` must not be in the subtree of a `delete`"
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if a delete sub-tree contains non-delete set_ aux`() {
        val modelJson = """
            |{
            |  "address_book": {
            |    "set_": "delete",
            |    "name": "Super Heroes",
            |    "last_updated": "1587147731",
            |    "settings__set_": "update",
            |    "settings": {
            |      "last_name_first": true,
            |      "encrypt_hero_name": false
            |    },
            |    "person__set_": "create",
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent"
            |      },
            |      {
            |        "set_": "create",
            |        "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val model =
            getMainModelFromJsonString(
                addressBookMetaModel,
                modelJson,
                multiAuxDecodingStateMachineFactory = auxDecodingFactory
            )

        val expectedErrors = listOf(
            "/address_book/settings: `update` must not be in the subtree of a `delete`",
            "/address_book/settings: entity without `delete` must not be in the subtree of a `delete`",
            "/address_book/person: `create` must not be in the subtree of a `delete`",
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]: entity without `delete` must not be in the subtree of a `delete`",
            "/address_book/person[a8aacf55-7810-4b43-afe5-4344f25435fd]: `create` must not be in the subtree of a `delete`"
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if a delete sub-tree contains only delete entities`() {
        val modelJson = """
            |{
            |  "address_book": {
            |    "set_": "delete",
            |    "name": "Super Heroes",
            |    "last_updated": "1587147731",
            |    "settings": {
            |      "set_": "delete",
            |      "last_name_first": true,
            |      "encrypt_hero_name": false
            |    },
            |    "person": [
            |      {
            |        "set_": "delete",
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent"
            |      },
            |      {
            |        "set_": "delete",
            |        "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val model =
            getMainModelFromJsonString(
                addressBookMetaModel,
                modelJson,
                multiAuxDecodingStateMachineFactory = auxDecodingFactory
            )

        val expectedErrors = emptyList<String>()
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }
}