package org.treeWare.model.operator

import org.treeWare.model.AddressBookMutableEntityModelFactory
import org.treeWare.model.decodeJsonStringIntoEntity
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
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
            |  "name": "Super Heroes",
            |  "last_updated": "1587147731",
            |  "settings": {
            |    "last_name_first": true,
            |    "encrypt_hero_name": false
            |  },
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent"
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = AddressBookMutableEntityModelFactory.create()
        decodeJsonStringIntoEntity(
            modelJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = model
        )

        val expectedErrors = listOf("/: set_ aux not attached to any composition field or entity")
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors for non-composition-field elements that have set_ aux`() {
        val modelJson = """
            |{
            |  "name__set_": "update",
            |  "name": "Super Heroes",
            |  "last_updated": "1587147731",
            |  "settings": {
            |    "last_name_first__set_": "create",
            |    "last_name_first": true,
            |    "encrypt_hero_name__set_": "create",
            |    "encrypt_hero_name": false
            |  },
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name__set_": "delete",
            |      "first_name": "Clark",
            |      "last_name__set_": "delete",
            |      "last_name": "Kent"
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = AddressBookMutableEntityModelFactory.create()
        decodeJsonStringIntoEntity(
            modelJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = model
        )

        val expectedErrors = listOf("/: set_ aux not attached to any composition field or entity")
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if a create sub-tree contains non-create set_ aux`() {
        val modelJson = """
            |{
            |  "set_": "create",
            |  "name": "Super Heroes",
            |  "last_updated": "1587147731",
            |  "settings__set_": "delete",
            |  "settings": {
            |    "last_name_first": true,
            |    "encrypt_hero_name": false
            |  },
            |  "persons__set_": "update",
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "is_hero": true
            |    },
            |    {
            |      "set_": "delete",
            |      "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = AddressBookMutableEntityModelFactory.create()
        decodeJsonStringIntoEntity(
            modelJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = model
        )

        val expectedErrors = listOf(
            "/settings: `delete` must not be in the subtree of a `create`",
            "/persons: `update` must not be in the subtree of a `create`",
            "/persons/a8aacf55-7810-4b43-afe5-4344f25435fd: `delete` must not be in the subtree of a `create`"
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if entities in a delete sub-tree do not have a set_ aux`() {
        val modelJson = """
            |{
            |  "set_": "delete",
            |  "name": "Super Heroes",
            |  "last_updated": "1587147731",
            |  "settings": {
            |    "last_name_first": true,
            |    "encrypt_hero_name": false
            |  },
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent"
            |    },
            |    {
            |      "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = AddressBookMutableEntityModelFactory.create()
        decodeJsonStringIntoEntity(
            modelJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = model
        )

        val expectedErrors = listOf(
            "/settings: entity without `delete` must not be in the subtree of a `delete`",
            "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f: entity without `delete` must not be in the subtree of a `delete`",
            "/persons/a8aacf55-7810-4b43-afe5-4344f25435fd: entity without `delete` must not be in the subtree of a `delete`"
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if a delete sub-tree contains non-delete set_ aux`() {
        val modelJson = """
            |{
            |  "set_": "delete",
            |  "name": "Super Heroes",
            |  "last_updated": "1587147731",
            |  "settings__set_": "update",
            |  "settings": {
            |    "last_name_first": true,
            |    "encrypt_hero_name": false
            |  },
            |  "persons__set_": "create",
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent"
            |    },
            |    {
            |      "set_": "create",
            |      "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = AddressBookMutableEntityModelFactory.create()
        decodeJsonStringIntoEntity(
            modelJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = model
        )

        val expectedErrors = listOf(
            "/settings: `update` must not be in the subtree of a `delete`",
            "/settings: entity without `delete` must not be in the subtree of a `delete`",
            "/persons: `create` must not be in the subtree of a `delete`",
            "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f: entity without `delete` must not be in the subtree of a `delete`",
            "/persons/a8aacf55-7810-4b43-afe5-4344f25435fd: `create` must not be in the subtree of a `delete`"
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if a delete sub-tree contains only delete entities`() {
        val modelJson = """
            |{
            |  "set_": "delete",
            |  "name": "Super Heroes",
            |  "last_updated": "1587147731",
            |  "settings": {
            |    "set_": "delete",
            |    "last_name_first": true,
            |    "encrypt_hero_name": false
            |  },
            |  "persons": [
            |    {
            |      "set_": "delete",
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent"
            |    },
            |    {
            |      "set_": "delete",
            |      "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = AddressBookMutableEntityModelFactory.create()
        decodeJsonStringIntoEntity(
            modelJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = model
        )

        val expectedErrors = emptyList<String>()
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if a sub-tree-granularity sub-tree contains set_ aux below the root`() {
        val modelJson = """
            |{
            |  "sub_tree_persons": [
            |    {
            |      "set_": "update",
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "is_hero": true,
            |      "hero_details": {
            |        "set_": "create",
            |        "strengths": "super-strength",
            |        "weaknesses": "kryptonite"
            |      }
            |    },
            |    {
            |      "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |      "is_hero": false,
            |      "hero_details": {
            |        "set_": "delete"
            |      }
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = AddressBookMutableEntityModelFactory.create()
        decodeJsonStringIntoEntity(
            modelJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = model
        )

        val expectedErrors = listOf(
            "/sub_tree_persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/hero_details: set_ aux is not valid inside a sub-tree with sub_tree granularity",
            "/sub_tree_persons/a8aacf55-7810-4b43-afe5-4344f25435fd/hero_details: set_ aux is not valid inside a sub-tree with sub_tree granularity",
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if a sub-tree-granularity sub-tree has set_ aux only at the root`() {
        val modelJson = """
            |{
            |  "sub_tree_persons": [
            |    {
            |      "set_": "update",
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "is_hero": true,
            |      "hero_details": {
            |        "strengths": "super-strength",
            |        "weaknesses": "kryptonite"
            |      }
            |    },
            |    {
            |      "set_": "delete",
            |      "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |      "is_hero": false,
            |      "hero_details": {
            |      }
            |    }
            |  ]
            |}
        """.trimMargin()
        val model = AddressBookMutableEntityModelFactory.create()
        decodeJsonStringIntoEntity(
            modelJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = model
        )

        val expectedErrors = emptyList<String>()
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }
}