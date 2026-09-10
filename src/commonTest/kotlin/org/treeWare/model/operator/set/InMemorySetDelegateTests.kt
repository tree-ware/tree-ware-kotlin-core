package org.treeWare.model.operator.set

import org.treeWare.metaModel.addressBookRootEntityFactory
import org.treeWare.model.core.AssociationModel
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.MutableSetFieldModel
import org.treeWare.model.core.SingleFieldModel
import org.treeWare.model.core.getOptionalSingleBoolean
import org.treeWare.model.core.getOptionalSingleEntity
import org.treeWare.model.core.getOptionalSingleString
import org.treeWare.model.decodeJsonStringIntoEntity
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.operator.ErrorCode
import org.treeWare.model.operator.Response
import org.treeWare.model.operator.set
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAuxStateMachine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val auxDecodingFactory = MultiAuxDecodingStateMachineFactory(SET_AUX_NAME to { SetAuxStateMachine(it) })

private const val CLARK_ID = "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
private const val LOIS_ID = "a8aacf55-7810-4b43-afe5-4344f25435fd"

class InMemorySetDelegateTests {
    @Test
    fun `create must add root fields, single entities and set entities`() {
        val target = addressBookRootEntityFactory(null)
        val response = applySet(
            target,
            """
            |{
            |  "set_": "update",
            |  "name": "Super Heroes",
            |  "settings": {
            |    "set_": "create",
            |    "last_name_first": true,
            |    "encrypt_hero_name": false
            |  },
            |  "persons": [
            |    {
            |      "set_": "create",
            |      "id": "$CLARK_ID",
            |      "first_name": "Clark",
            |      "last_name": "Kent"
            |    }
            |  ],
            |  "groups": [
            |    {
            |      "set_": "create",
            |      "name": "DC",
            |      "sub_groups": [
            |        {
            |          "set_": "create",
            |          "name": "Superman"
            |        }
            |      ]
            |    }
            |  ]
            |}
            """.trimMargin()
        )
        assertIs<Response.Success>(response)

        assertEquals("Super Heroes", getOptionalSingleString(target, "name"))

        val settings = getOptionalSingleEntity(target, "settings")
        assertNotNull(settings)
        assertEquals(true, getOptionalSingleBoolean(settings, "last_name_first"))
        assertEquals(false, getOptionalSingleBoolean(settings, "encrypt_hero_name"))

        val clark = findSetEntityByField(target, "persons", "id", CLARK_ID)
        assertNotNull(clark)
        assertEquals("Clark", getOptionalSingleString(clark, "first_name"))
        assertEquals("Kent", getOptionalSingleString(clark, "last_name"))

        val dc = findSetEntityByField(target, "groups", "name", "DC")
        assertNotNull(dc)
        val superman = findSetEntityByField(dc, "sub_groups", "name", "Superman")
        assertNotNull(superman)
    }

    @Test
    fun `update must merge fields and leave other fields intact`() {
        val target = addressBookRootEntityFactory(null)
        assertIs<Response.Success>(
            applySet(
                target,
                """
                |{
                |  "set_": "update",
                |  "name": "Super Heroes",
                |  "persons": [
                |    {
                |      "set_": "create",
                |      "id": "$CLARK_ID",
                |      "first_name": "Clark",
                |      "last_name": "Kent"
                |    }
                |  ]
                |}
                """.trimMargin()
            )
        )

        val response = applySet(
            target,
            """
            |{
            |  "persons": [
            |    {
            |      "set_": "update",
            |      "id": "$CLARK_ID",
            |      "hero_name": "Superman"
            |    }
            |  ]
            |}
            """.trimMargin()
        )
        assertIs<Response.Success>(response)

        val clark = findSetEntityByField(target, "persons", "id", CLARK_ID)
        assertNotNull(clark)
        assertEquals("Clark", getOptionalSingleString(clark, "first_name"))
        assertEquals("Kent", getOptionalSingleString(clark, "last_name"))
        assertEquals("Superman", getOptionalSingleString(clark, "hero_name"))
        assertEquals("Super Heroes", getOptionalSingleString(target, "name"))
    }

    @Test
    fun `create must fail for duplicate entities`() {
        val target = addressBookRootEntityFactory(null)
        val createJson = """
            |{
            |  "persons": [
            |    {
            |      "set_": "create",
            |      "id": "$CLARK_ID",
            |      "first_name": "Clark"
            |    }
            |  ]
            |}
        """.trimMargin()
        assertIs<Response.Success>(applySet(target, createJson))

        val response = applySet(target, createJson)
        assertSetError(response, "/persons/$CLARK_ID", "already exists")

        val persons = target.getField("persons") as MutableSetFieldModel
        assertEquals(1, persons.values.size)
    }

    @Test
    fun `create must fail for duplicate single entities`() {
        val target = addressBookRootEntityFactory(null)
        val createJson = """
            |{
            |  "settings": {
            |    "set_": "create",
            |    "last_name_first": true
            |  }
            |}
        """.trimMargin()
        assertIs<Response.Success>(applySet(target, createJson))

        val response = applySet(target, createJson)
        assertSetError(response, "/settings", "already exists")
    }

    @Test
    fun `update must fail for missing entities`() {
        val target = addressBookRootEntityFactory(null)
        val response = applySet(
            target,
            """
            |{
            |  "persons": [
            |    {
            |      "set_": "update",
            |      "id": "$CLARK_ID",
            |      "first_name": "Clark"
            |    }
            |  ]
            |}
            """.trimMargin()
        )
        assertSetError(response, "/persons/$CLARK_ID", "not found")

        val missingSettingsResponse = applySet(
            target,
            """
            |{
            |  "settings": {
            |    "set_": "update",
            |    "last_name_first": true
            |  }
            |}
            """.trimMargin()
        )
        assertSetError(missingSettingsResponse, "/settings", "not found")
    }

    @Test
    fun `create must fail when the parent entity is missing`() {
        val target = addressBookRootEntityFactory(null)
        val response = applySet(
            target,
            """
            |{
            |  "groups": [
            |    {
            |      "name": "DC",
            |      "sub_groups": [
            |        {
            |          "set_": "create",
            |          "name": "Superman"
            |        }
            |      ]
            |    }
            |  ]
            |}
            """.trimMargin()
        )
        // The sub-group is created, but its parent group "DC" is neither in the
        // request (no set_ aux) nor in the target model.
        assertSetError(response, "/groups/DC/sub_groups/Superman", "parent entity not found")
    }

    @Test
    fun `delete must remove set entities and be idempotent`() {
        val target = addressBookRootEntityFactory(null)
        assertIs<Response.Success>(
            applySet(
                target,
                """
                |{
                |  "set_": "update",
                |  "persons": [
                |    {
                |      "set_": "create",
                |      "id": "$CLARK_ID",
                |      "first_name": "Clark"
                |    },
                |    {
                |      "set_": "create",
                |      "id": "$LOIS_ID",
                |      "first_name": "Lois"
                |    }
                |  ]
                |}
                """.trimMargin()
            )
        )

        val deleteResponse = applySet(
            target,
            """
            |{
            |  "persons": [
            |    {
            |      "set_": "delete",
            |      "id": "$CLARK_ID"
            |    }
            |  ]
            |}
            """.trimMargin()
        )
        assertIs<Response.Success>(deleteResponse)
        assertNull(findSetEntityByField(target, "persons", "id", CLARK_ID))
        val lois = findSetEntityByField(target, "persons", "id", LOIS_ID)
        assertNotNull(lois)
        assertEquals("Lois", getOptionalSingleString(lois, "first_name"))

        // Deleting an already-deleted entity is a no-op.
        val repeatDeleteResponse = applySet(
            target,
            """
            |{
            |  "persons": [
            |    {
            |      "set_": "delete",
            |      "id": "$CLARK_ID"
            |    }
            |  ]
            |}
            """.trimMargin()
        )
        assertIs<Response.Success>(repeatDeleteResponse)
    }

    @Test
    fun `delete must detach single entities and be idempotent`() {
        val target = addressBookRootEntityFactory(null)
        assertIs<Response.Success>(
            applySet(
                target,
                """
                |{
                |  "settings": {
                |    "set_": "create",
                |    "last_name_first": true
                |  }
                |}
                """.trimMargin()
            )
        )

        val deleteResponse = applySet(
            target,
            """
            |{
            |  "settings": {
            |    "set_": "delete"
            |  }
            |}
            """.trimMargin()
        )
        assertIs<Response.Success>(deleteResponse)
        assertNull(target.getField("settings"))

        val repeatDeleteResponse = applySet(
            target,
            """
            |{
            |  "settings": {
            |    "set_": "delete"
            |  }
            |}
            """.trimMargin()
        )
        assertIs<Response.Success>(repeatDeleteResponse)
    }

    @Test
    fun `delete must remove nested entities along with their parent`() {
        val target = addressBookRootEntityFactory(null)
        assertIs<Response.Success>(
            applySet(
                target,
                """
                |{
                |  "groups": [
                |    {
                |      "set_": "create",
                |      "name": "DC",
                |      "sub_groups": [
                |        {
                |          "set_": "create",
                |          "name": "Superman"
                |        }
                |      ]
                |    }
                |  ]
                |}
                """.trimMargin()
            )
        )

        // Deleting the parent group also deletes nested sub-groups, so the
        // follow-up delete of the already-removed sub-group is a no-op.
        val deleteResponse = applySet(
            target,
            """
            |{
            |  "groups": [
            |    {
            |      "set_": "delete",
            |      "name": "DC",
            |      "sub_groups": [
            |        {
            |          "set_": "delete",
            |          "name": "Superman"
            |        }
            |      ]
            |    }
            |  ]
            |}
            """.trimMargin()
        )
        assertIs<Response.Success>(deleteResponse)
        assertNull(findSetEntityByField(target, "groups", "name", "DC"))
    }

    @Test
    fun `delete must clear the root entity`() {
        val target = addressBookRootEntityFactory(null)
        assertIs<Response.Success>(
            applySet(
                target,
                """
                |{
                |  "set_": "update",
                |  "name": "Super Heroes"
                |}
                """.trimMargin()
            )
        )

        val deleteResponse = applySet(target, """{ "set_": "delete" }""")
        assertIs<Response.Success>(deleteResponse)
        assertTrue(target.fields.isEmpty())
    }

    @Test
    fun `create and update must handle entities with composition keys`() {
        val target = addressBookRootEntityFactory(null)
        assertIs<Response.Success>(
            applySet(
                target,
                """
                |{
                |  "set_": "update",
                |  "name": "Super Heroes",
                |  "cities": [
                |    {
                |      "set_": "create",
                |      "city": {
                |        "country": "United States of America",
                |        "state": "New York",
                |        "name": "New York City"
                |      },
                |      "info": "One of the most populous cities in USA"
                |    }
                |  ]
                |}
                """.trimMargin()
            )
        )

        val cities = target.getField("cities") as MutableSetFieldModel
        assertEquals(1, cities.values.size)
        val cityInfo = cities.values.first() as MutableEntityModel
        val city = getOptionalSingleEntity(cityInfo, "city")
        assertNotNull(city)
        assertEquals("New York City", getOptionalSingleString(city, "name"))
        assertEquals(
            "One of the most populous cities in USA",
            getOptionalSingleString(cityInfo, "info")
        )

        // Update the non-key field of the city info.
        val updateResponse = applySet(
            target,
            """
            |{
            |  "cities": [
            |    {
            |      "set_": "update",
            |      "city": {
            |        "country": "United States of America",
            |        "state": "New York",
            |        "name": "New York City"
            |      },
            |      "info": "The Big Apple"
            |    }
            |  ]
            |}
            """.trimMargin()
        )
        assertIs<Response.Success>(updateResponse)
        assertEquals("The Big Apple", getOptionalSingleString(cityInfo, "info"))
        val updatedCity = getOptionalSingleEntity(cityInfo, "city")
        assertNotNull(updatedCity)
        assertEquals("New York City", getOptionalSingleString(updatedCity, "name"))

        // A duplicate create with the same composition keys must fail.
        val duplicateResponse = applySet(
            target,
            """
            |{
            |  "cities": [
            |    {
            |      "set_": "create",
            |      "city": {
            |        "country": "United States of America",
            |        "state": "New York",
            |        "name": "New York City"
            |      },
            |      "info": "Duplicate"
            |    }
            |  ]
            |}
            """.trimMargin()
        )
        assertSetError(
            duplicateResponse,
            "/cities/New York City/New York/United States of America",
            "already exists"
        )
    }

    @Test
    fun `create and update must handle associations`() {
        val target = addressBookRootEntityFactory(null)
        assertIs<Response.Success>(
            applySet(
                target,
                """
                |{
                |  "set_": "create",
                |  "name": "Super Heroes",
                |  "persons": [
                |    {
                |      "id": "$CLARK_ID",
                |      "group": {
                |        "groups": [
                |          {
                |            "name": "DC",
                |            "sub_groups": [
                |              {
                |                "name": "Superman"
                |              }
                |            ]
                |          }
                |        ]
                |      }
                |    }
                |  ]
                |}
                """.trimMargin()
            )
        )

        val clark = findSetEntityByField(target, "persons", "id", CLARK_ID)
        assertNotNull(clark)
        assertAssociationGroup(clark, "DC", "Superman")

        // Updating the association must replace it.
        val updateResponse = applySet(
            target,
            """
            |{
            |  "persons": [
            |    {
            |      "set_": "update",
            |      "id": "$CLARK_ID",
            |      "group": {
            |        "groups": [
            |          {
            |            "name": "Marvel",
            |            "sub_groups": [
            |              {
            |                "name": "Spider-Man"
            |              }
            |            ]
            |          }
            |        ]
            |      }
            |    }
            |  ]
            |}
            """.trimMargin()
        )
        assertIs<Response.Success>(updateResponse)
        assertAssociationGroup(clark, "Marvel", "Spider-Man")
    }

    // Helpers

    private fun applySet(target: MutableEntityModel, requestJson: String): Response {
        val request = addressBookRootEntityFactory(null)
        decodeJsonStringIntoEntity(
            requestJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = request
        )
        return set(request, InMemorySetDelegate(target), null)
    }

    private fun findSetEntityByField(
        parent: EntityModel,
        setFieldName: String,
        keyFieldName: String,
        keyValue: String
    ): MutableEntityModel? {
        val setField = parent.getField(setFieldName) as? MutableSetFieldModel ?: return null
        return setField.values.map { it as MutableEntityModel }.firstOrNull { entity ->
            getOptionalSingleString(entity, keyFieldName) == keyValue
        }
    }

    private fun assertAssociationGroup(
        person: MutableEntityModel,
        groupName: String,
        subGroupName: String
    ) {
        val groupField = person.getField("group") as? SingleFieldModel
        assertNotNull(groupField)
        val association = groupField.value as? AssociationModel
        assertNotNull(association)
        val group = findSetEntityByField(association.value, "groups", "name", groupName)
        assertNotNull(group)
        val subGroup = findSetEntityByField(group, "sub_groups", "name", subGroupName)
        assertNotNull(subGroup)
    }

    private fun assertSetError(response: Response, path: String, messageSnippet: String) {
        assertIs<Response.ErrorList>(response)
        assertEquals(ErrorCode.CLIENT_ERROR, response.errorCode)
        assertTrue(
            response.errorList.any { it.path == path && it.error.contains(messageSnippet) },
            "Expected error with path $path containing \"$messageSnippet\", got: ${response.errorList}"
        )
    }
}
