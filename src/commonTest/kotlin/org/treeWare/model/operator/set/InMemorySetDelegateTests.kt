package org.treeWare.model.operator.set

import org.treeWare.metaModel.addressBookRootEntityFactory
import org.treeWare.model.assertMatchesJsonString
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.decodeJsonStringIntoEntity
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.operator.ErrorCode
import org.treeWare.model.operator.Response
import org.treeWare.model.operator.set
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAuxStateMachine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
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

        val expectedJson = """
            |{
            |  "name": "Super Heroes",
            |  "settings": {
            |    "last_name_first": true,
            |    "encrypt_hero_name": false
            |  },
            |  "persons": [
            |    {
            |      "id": "$CLARK_ID",
            |      "first_name": "Clark",
            |      "last_name": "Kent"
            |    }
            |  ],
            |  "groups": [
            |    {
            |      "name": "DC",
            |      "sub_groups": [
            |        {
            |          "name": "Superman"
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        assertMatchesJsonString(target, expectedJson, EncodePasswords.NONE)
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

        val expectedJson = """
            |{
            |  "name": "Super Heroes",
            |  "persons": [
            |    {
            |      "id": "$CLARK_ID",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "hero_name": "Superman"
            |    }
            |  ]
            |}
        """.trimMargin()
        assertMatchesJsonString(target, expectedJson, EncodePasswords.NONE)
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

        // The failed duplicate create must leave the target unchanged.
        val expectedJson = """
            |{
            |  "persons": [
            |    {
            |      "id": "$CLARK_ID",
            |      "first_name": "Clark"
            |    }
            |  ]
            |}
        """.trimMargin()
        assertMatchesJsonString(target, expectedJson, EncodePasswords.NONE)
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

        // The failed duplicate create must leave the target unchanged.
        val expectedJson = """
            |{
            |  "settings": {
            |    "last_name_first": true
            |  }
            |}
        """.trimMargin()
        assertMatchesJsonString(target, expectedJson, EncodePasswords.NONE)
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

        // The failed updates must leave the target empty.
        assertMatchesJsonString(target, "{}", EncodePasswords.NONE)
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

        // The failed create must leave the target empty.
        assertMatchesJsonString(target, "{}", EncodePasswords.NONE)
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
        val expectedJson = """
            |{
            |  "persons": [
            |    {
            |      "id": "$LOIS_ID",
            |      "first_name": "Lois"
            |    }
            |  ]
            |}
        """.trimMargin()
        assertMatchesJsonString(target, expectedJson, EncodePasswords.NONE)

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
        assertMatchesJsonString(target, expectedJson, EncodePasswords.NONE)
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
        assertMatchesJsonString(target, "{}", EncodePasswords.NONE)

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
        assertMatchesJsonString(target, "{}", EncodePasswords.NONE)
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
        // The emptied groups set-field is still present in the target.
        val expectedJson = """
            |{
            |  "groups": []
            |}
        """.trimMargin()
        assertMatchesJsonString(target, expectedJson, EncodePasswords.NONE)
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
        assertMatchesJsonString(target, "{}", EncodePasswords.NONE)
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

        val expectedJson = """
            |{
            |  "name": "Super Heroes",
            |  "cities": [
            |    {
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
        assertMatchesJsonString(target, expectedJson, EncodePasswords.NONE)

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
        val expectedUpdatedJson = """
            |{
            |  "name": "Super Heroes",
            |  "cities": [
            |    {
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
        assertMatchesJsonString(target, expectedUpdatedJson, EncodePasswords.NONE)

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

        // The failed duplicate create must leave the target unchanged.
        assertMatchesJsonString(target, expectedUpdatedJson, EncodePasswords.NONE)
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

        val expectedJson = """
            |{
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
        assertMatchesJsonString(target, expectedJson, EncodePasswords.NONE)

        // Updating the association merges the new path into the existing one.
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
        val expectedUpdatedJson = """
            |{
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
            |          },
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
        assertMatchesJsonString(target, expectedUpdatedJson, EncodePasswords.NONE)
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

    private fun assertSetError(response: Response, path: String, messageSnippet: String) {
        assertIs<Response.ErrorList>(response)
        assertEquals(ErrorCode.CLIENT_ERROR, response.errorCode)
        assertTrue(
            response.errorList.any { it.path == path && it.error.contains(messageSnippet) },
            "Expected error with path $path containing \"$messageSnippet\", got: ${response.errorList}"
        )
    }
}
