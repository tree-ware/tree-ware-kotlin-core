package org.treeWare.model.operator

import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import org.treeWare.metaModel.addressBookMetaModel
import org.treeWare.mockk.fieldsWithNames
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.getMainModelFromJsonString
import org.treeWare.model.operator.set.SetDelegate
import org.treeWare.model.operator.set.assertSetResponse
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAux
import org.treeWare.model.operator.set.aux.SetAuxStateMachine
import kotlin.test.Test

private val auxDecodingFactory = MultiAuxDecodingStateMachineFactory(SET_AUX_NAME to { SetAuxStateMachine(it) })

// TODO(deepak-nulu): create and use matches for the ancestorKeys parameter of setEntity()

class SetDelegateTests {
    @Test
    fun `set() must call its delegate`() {
        val modelJson = """
            |{
            |  "address_book__set_": "update",
            |  "address_book": {
            |    "name": "Super Heroes",
            |    "last_updated": "1587147731",
            |    "settings__set_": "create",
            |    "settings": {
            |      "last_name_first": true,
            |      "encrypt_hero_name": false,
            |      "card_colors": [
            |        {
            |          "value": "orange"
            |        },
            |        {
            |          "value": "green"
            |        }
            |      ]
            |    },
            |    "person": [
            |      {
            |        "set_": "create",
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent",
            |        "hero_name": "Superman",
            |        "picture": "UGljdHVyZSBvZiBDbGFyayBLZW50"
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

        val delegate = mockk<SetDelegate>()
        every { delegate.begin() } returns Response.Success
        every {
            delegate.setEntity(
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType()
            )
        } returns Response.Success
        every { delegate.end() } returns Response.Success

        val expectedResponse = Response.Success
        val actualResponse = set(model, delegate, null)

        assertSetResponse(expectedResponse, actualResponse)
        verifySequence {
            delegate.begin()
            delegate.setEntity(
                SetAux.UPDATE,
                ofType(),
                "/address_book",
                "/address_book",
                ofType(),
                fieldsWithNames(),
                fieldsWithNames(),
                fieldsWithNames("name", "last_updated")
            )
            delegate.setEntity(
                SetAux.CREATE,
                ofType(),
                "/address_book/settings",
                "/address_book/settings",
                ofType(),
                fieldsWithNames(),
                fieldsWithNames(),
                fieldsWithNames("last_name_first", "encrypt_hero_name", "card_colors")
            )
            delegate.setEntity(
                SetAux.CREATE,
                ofType(),
                "/address_book/person",
                "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames(),
                fieldsWithNames("first_name", "last_name", "hero_name", "picture")
            )
            delegate.setEntity(
                SetAux.DELETE,
                ofType(),
                "/address_book/person",
                "/address_book/person/a8aacf55-7810-4b43-afe5-4344f25435fd",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames(),
                fieldsWithNames()
            )
            delegate.end()
        }
    }

    @Test
    fun `set() must not call its delegate for entities that do not have an active set_ aux`() {
        val modelJson = """
            |{
            |  "address_book": {
            |    "settings": {
            |      "set_": "create",
            |      "last_name_first": true,
            |      "encrypt_hero_name": false,
            |      "card_colors": [
            |        {
            |          "value": "orange"
            |        },
            |        {
            |          "value": "green"
            |        }
            |      ]
            |    },
            |    "person": [
            |      {
            |        "set_": "create",
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent",
            |        "hero_name": "Superman",
            |        "picture": "UGljdHVyZSBvZiBDbGFyayBLZW50"
            |      },
            |      {
            |        "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |        "first_name": "Lois",
            |        "last_name": "Lane"
            |      },
            |      {
            |        "set_": "delete",
            |        "id": "2260d15f-2cc0-4b04-83fb-c950c18a6629"
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

        val delegate = mockk<SetDelegate>()
        every { delegate.begin() } returns Response.Success
        every {
            delegate.setEntity(
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType()
            )
        } returns Response.Success
        every { delegate.end() } returns Response.Success

        val expectedResponse = Response.Success
        val actualResponse = set(model, delegate, null)

        assertSetResponse(expectedResponse, actualResponse)
        verifySequence {
            delegate.begin()
            delegate.setEntity(
                SetAux.CREATE,
                ofType(),
                "/address_book/settings",
                "/address_book/settings",
                ofType(),
                fieldsWithNames(),
                fieldsWithNames(),
                fieldsWithNames("last_name_first", "encrypt_hero_name", "card_colors")
            )
            delegate.setEntity(
                SetAux.CREATE,
                ofType(),
                "/address_book/person",
                "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames(),
                fieldsWithNames("first_name", "last_name", "hero_name", "picture")
            )
            delegate.setEntity(
                SetAux.DELETE,
                ofType(),
                "/address_book/person",
                "/address_book/person/2260d15f-2cc0-4b04-83fb-c950c18a6629",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames(),
                fieldsWithNames()
            )
            delegate.end()
        }
    }

    @Test
    fun `set() must inherit set_aux for set-field elements that do not specify their own set_ aux`() {
        val modelJson = """
            |{
            |  "address_book": {
            |    "settings": {
            |      "set_": "create",
            |      "last_name_first": true,
            |      "encrypt_hero_name": false,
            |      "card_colors": [
            |        {
            |          "value": "orange"
            |        },
            |        {
            |          "value": "green"
            |        }
            |      ]
            |    },
            |    "person__set_": "update",
            |    "person": [
            |      {
            |        "set_": "create",
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent",
            |        "hero_name": "Superman",
            |        "picture": "UGljdHVyZSBvZiBDbGFyayBLZW50"
            |      },
            |      {
            |        "id": "a8aacf55-7810-4b43-afe5-4344f25435fd",
            |        "first_name": "Lois",
            |        "last_name": "Lane"
            |      },
            |      {
            |        "set_": "delete",
            |        "id": "2260d15f-2cc0-4b04-83fb-c950c18a6629"
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

        val delegate = mockk<SetDelegate>()
        every { delegate.begin() } returns Response.Success
        every {
            delegate.setEntity(
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType()
            )
        } returns Response.Success
        every { delegate.end() } returns Response.Success

        val expectedResponse = Response.Success
        val actualResponse = set(model, delegate, null)

        assertSetResponse(expectedResponse, actualResponse)
        verifySequence {
            delegate.begin()
            delegate.setEntity(
                SetAux.CREATE,
                ofType(),
                "/address_book/settings",
                "/address_book/settings",
                ofType(),
                fieldsWithNames(),
                fieldsWithNames(),
                fieldsWithNames("last_name_first", "encrypt_hero_name", "card_colors")
            )
            delegate.setEntity(
                SetAux.CREATE,
                ofType(),
                "/address_book/person",
                "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames(),
                fieldsWithNames("first_name", "last_name", "hero_name", "picture")
            )
            // NOTE: The following entity inherits its SetAux from its parent set-field.
            delegate.setEntity(
                SetAux.UPDATE,
                ofType(),
                "/address_book/person",
                "/address_book/person/a8aacf55-7810-4b43-afe5-4344f25435fd",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames(),
                fieldsWithNames("first_name", "last_name")
            )
            delegate.setEntity(
                SetAux.DELETE,
                ofType(),
                "/address_book/person",
                "/address_book/person/2260d15f-2cc0-4b04-83fb-c950c18a6629",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames(),
                fieldsWithNames()
            )
            delegate.end()
        }
    }

    @Test
    fun `set() must escape key values in field paths`() {
        val modelJson = """
            |{
            |  "address_book__set_": "create",
            |  "address_book": {
            |    "name": "Super Heroes",
            |    "groups": [
            |      {
            |        "name": "Group[0]",
            |        "sub_groups": [
            |          {
            |            "name": "Group[0,1]"
            |          }
            |        ]
            |      },
            |      {
            |        "name": "Group/1\\",
            |        "sub_groups": [
            |          {
            |            "name": "Group/1\\/1"
            |          }
            |        ]
            |      },
            |      {
            |        "name": "Group[2,1]",
            |        "sub_groups": [
            |          {
            |            "name": "Group[2,2]"
            |          }
            |        ]
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

        val delegate = mockk<SetDelegate>()
        every { delegate.begin() } returns Response.Success
        every {
            delegate.setEntity(
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType()
            )
        } returns Response.Success
        every { delegate.end() } returns Response.Success

        val expectedResponse = Response.Success
        val actualResponse = set(model, delegate, null)

        assertSetResponse(expectedResponse, actualResponse)
        verifySequence {
            delegate.begin()
            delegate.setEntity(
                SetAux.CREATE,
                ofType(),
                "/address_book",
                "/address_book",
                ofType(),
                fieldsWithNames(),
                fieldsWithNames(),
                fieldsWithNames("name")
            )
            delegate.setEntity(
                SetAux.CREATE,
                ofType(),
                "/address_book/groups",
                "/address_book/groups/Group[0]",
                ofType(),
                fieldsWithNames("name"),
                fieldsWithNames(),
                fieldsWithNames()
            )
            delegate.setEntity(
                SetAux.CREATE,
                ofType(),
                "/address_book/groups/Group[0]/sub_groups",
                "/address_book/groups/Group[0]/sub_groups/Group[0,1]",
                ofType(),
                fieldsWithNames("name"),
                fieldsWithNames(),
                fieldsWithNames()
            )
            delegate.setEntity(
                SetAux.CREATE,
                ofType(),
                "/address_book/groups",
                "/address_book/groups/Group\\/1\\\\",
                ofType(),
                fieldsWithNames("name"),
                fieldsWithNames(),
                fieldsWithNames()
            )
            delegate.setEntity(
                SetAux.CREATE,
                ofType(),
                "/address_book/groups/Group\\/1\\\\/sub_groups",
                "/address_book/groups/Group\\/1\\\\/sub_groups/Group\\/1\\\\\\/1",
                ofType(),
                fieldsWithNames("name"),
                fieldsWithNames(),
                fieldsWithNames()
            )
            delegate.setEntity(
                SetAux.CREATE,
                ofType(),
                "/address_book/groups",
                "/address_book/groups/Group[2,1]",
                ofType(),
                fieldsWithNames("name"),
                fieldsWithNames(),
                fieldsWithNames()
            )
            delegate.setEntity(
                SetAux.CREATE,
                ofType(),
                "/address_book/groups/Group[2,1]/sub_groups",
                "/address_book/groups/Group[2,1]/sub_groups/Group[2,2]",
                ofType(),
                fieldsWithNames("name"),
                fieldsWithNames(),
                fieldsWithNames()
            )
            delegate.end()
        }
    }

    @Test
    fun `set() must sort keys by field number and flatten composition keys`() {
        val modelJson = """
            |{
            |  "address_book__set_": "create",
            |  "address_book": {
            |    "name": "Super Heroes",
            |    "city_info": [
            |      {
            |        "city": {
            |          "country": "United States of America",
            |          "state": "New York",
            |          "name": "New York City"
            |        },
            |        "info": "One of the most populous and most densely populated major city in USA"
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

        val delegate = mockk<SetDelegate>()
        every { delegate.begin() } returns Response.Success
        every {
            delegate.setEntity(
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType()
            )
        } returns Response.Success
        every { delegate.end() } returns Response.Success

        val expectedResponse = Response.Success
        val actualResponse = set(model, delegate, null)

        assertSetResponse(expectedResponse, actualResponse)
        verifySequence {
            delegate.begin()
            delegate.setEntity(
                SetAux.CREATE,
                ofType(),
                "/address_book",
                "/address_book",
                ofType(),
                fieldsWithNames(),
                fieldsWithNames(),
                fieldsWithNames("name")
            )
            delegate.setEntity(
                SetAux.CREATE,
                ofType(),
                "/address_book/city_info",
                "/address_book/city_info/New York City/New York/United States of America",
                ofType(),
                fieldsWithNames("name", "state", "country"),
                fieldsWithNames(),
                fieldsWithNames("info")
            )
            delegate.end()
        }
    }

    @Test
    fun `set() must not abort with errors for valid associations`() {
        val modelJson = """
            |{
            |  "address_book__set_": "create",
            |  "address_book": {
            |    "name": "Super Heroes",
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "group": {
            |          "groups": [
            |            {
            |              "name": "DC",
            |              "sub_groups": [
            |                {
            |                  "name": "Superman"
            |                }
            |              ]
            |            }
            |          ]
            |        }
            |      }
            |    ],
            |    "city_info": [
            |      {
            |        "city": {
            |          "name": "New York City",
            |          "state": "New York",
            |          "country": "United States of America"
            |        },
            |        "info": "One of the most populous and most densely populated major city in USA",
            |        "related_city_info": [
            |          {
            |            "city_info": [
            |              {
            |                "city": {
            |                  "name": "Albany",
            |                  "state": "New York",
            |                  "country": "United States of America"
            |                }
            |              }
            |            ]
            |          }
            |        ]
            |      },
            |      {
            |        "related_city_info": [
            |          {
            |            "city_info": [
            |              {
            |                "city": {
            |                  "name": "New York City",
            |                  "state": "New York",
            |                  "country": "United States of America"
            |                }
            |              }
            |            ]
            |          }
            |        ],
            |        "info": "Capital of New York state",
            |        "city": {
            |          "name": "Albany",
            |          "state": "New York",
            |          "country": "United States of America"
            |        }
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

        val delegate = mockk<SetDelegate>()
        every { delegate.begin() } returns Response.Success
        every {
            delegate.setEntity(
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType()
            )
        } returns Response.Success
        every { delegate.end() } returns Response.Success

        val expectedResponse = Response.Success
        val actualResponse = set(model, delegate, null)

        assertSetResponse(expectedResponse, actualResponse)
        verifySequence {
            delegate.begin()
            delegate.setEntity(
                SetAux.CREATE,
                ofType(),
                "/address_book",
                "/address_book",
                ofType(),
                fieldsWithNames(),
                fieldsWithNames(),
                fieldsWithNames("name")
            )
            delegate.setEntity(
                SetAux.CREATE,
                ofType(),
                "/address_book/person",
                "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                ofType(),
                fieldsWithNames("id"),
                fieldsWithNames("group"),
                fieldsWithNames()
            )
            delegate.setEntity(
                SetAux.CREATE,
                ofType(),
                "/address_book/city_info",
                "/address_book/city_info/New York City/New York/United States of America",
                ofType(),
                fieldsWithNames("name", "state", "country"),
                fieldsWithNames("related_city_info"),
                fieldsWithNames("info")
            )
            delegate.setEntity(
                SetAux.CREATE,
                ofType(),
                "/address_book/city_info",
                "/address_book/city_info/Albany/New York/United States of America",
                ofType(),
                fieldsWithNames("name", "state", "country"),
                fieldsWithNames("related_city_info"),
                fieldsWithNames("info")
            )
            delegate.end()
        }
    }

    @Test
    fun `set() must abort with errors if delegate begin() returns errors`() {
        val modelJson = """
            |{
            |  "address_book__set_": "create",
            |  "address_book": {
            |    "name": "Super Heroes",
            |    "last_updated": "1587147731"
            |  }
            |}
        """.trimMargin()
        val model =
            getMainModelFromJsonString(
                addressBookMetaModel,
                modelJson,
                multiAuxDecodingStateMachineFactory = auxDecodingFactory
            )

        val delegate = mockk<SetDelegate>()
        every { delegate.begin() } returns Response.ErrorList(
            ErrorCode.SERVER_ERROR,
            listOf(ElementModelError("/", "delegate begin error"))
        )
        val expectedResponse =
            Response.ErrorList(ErrorCode.SERVER_ERROR, listOf(ElementModelError("/", "delegate begin error")))

        val actualResponse = set(model, delegate, null)

        assertSetResponse(expectedResponse, actualResponse)
        verifySequence {
            delegate.begin()
        }
    }

    @Test
    fun `set() must abort with errors if delegate setEntity() returns errors`() {
        val modelJson = """
            |{
            |  "address_book__set_": "create",
            |  "address_book": {
            |    "name": "Super Heroes",
            |    "last_updated": "1587147731",
            |    "settings": {
            |      "last_name_first": true,
            |      "encrypt_hero_name": false
            |    },
            |    "groups": [
            |      {
            |        "name": "Group-0",
            |        "sub_groups": [
            |          {
            |            "name": "Group-0-1"
            |          }
            |        ]
            |      },
            |      {
            |        "name": "Group-1"
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

        val delegate = mockk<SetDelegate>()
        every { delegate.begin() } returns Response.Success
        every {
            delegate.setEntity(
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType()
            )
        } returnsMany listOf(
            Response.Success,  // address_book entity
            Response.Success,  // settings entity
            Response.ErrorList(
                ErrorCode.CLIENT_ERROR, listOf(
                    ElementModelError("/address_book/groups/Group-0", "delegate error 1"),
                    ElementModelError("/address_book/groups/Group-0", "delegate error 2")
                )
            ), // Group-0 entity
            Response.ErrorList(
                ErrorCode.CLIENT_ERROR,
                listOf(ElementModelError("/address_book/groups/Group-1", "delegate error 3"))
            ) // Group-1 entity
        )
        val expectedResponse = Response.ErrorList(
            ErrorCode.CLIENT_ERROR,
            listOf(
                ElementModelError("/address_book/groups/Group-0", "delegate error 1"),
                ElementModelError("/address_book/groups/Group-0", "delegate error 2"),
                ElementModelError("/address_book/groups/Group-1", "delegate error 3")
            )
        )

        val actualResponse = set(model, delegate, null)

        assertSetResponse(expectedResponse, actualResponse)
        verifySequence {
            delegate.begin()
            delegate.setEntity(
                SetAux.CREATE,
                ofType(),
                "/address_book",
                "/address_book",
                ofType(),
                fieldsWithNames(),
                fieldsWithNames(),
                fieldsWithNames("name", "last_updated")
            )
            delegate.setEntity(
                SetAux.CREATE,
                ofType(),
                "/address_book/settings",
                "/address_book/settings",
                ofType(),
                fieldsWithNames(),
                fieldsWithNames(),
                fieldsWithNames("last_name_first", "encrypt_hero_name")
            )
            delegate.setEntity(
                SetAux.CREATE,
                ofType(),
                "/address_book/groups",
                "/address_book/groups/Group-0",
                ofType(),
                fieldsWithNames("name"),
                fieldsWithNames(),
                fieldsWithNames()
            )
            // NOTE: sub_group Group-0-1 is not visited because the delegate returns errors for the above parent group.
            delegate.setEntity(
                SetAux.CREATE,
                ofType(),
                "/address_book/groups",
                "/address_book/groups/Group-1",
                ofType(),
                fieldsWithNames("name"),
                fieldsWithNames(),
                fieldsWithNames()

            )
        }
    }

    @Test
    fun `set() must abort with errors if delegate end() returns errors`() {
        val modelJson = """
            |{
            |  "address_book__set_": "create",
            |  "address_book": {
            |    "name": "Super Heroes",
            |    "last_updated": "1587147731"
            |  }
            |}
        """.trimMargin()
        val model =
            getMainModelFromJsonString(
                addressBookMetaModel,
                modelJson,
                multiAuxDecodingStateMachineFactory = auxDecodingFactory
            )

        val delegate = mockk<SetDelegate>()
        every { delegate.begin() } returns Response.Success
        every {
            delegate.setEntity(
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType(),
                ofType()
            )
        } returns Response.Success
        every { delegate.end() } returns Response.ErrorList(
            ErrorCode.CLIENT_ERROR,
            listOf(ElementModelError("/", "delegate end error"))
        )
        val expectedResponse =
            Response.ErrorList(ErrorCode.CLIENT_ERROR, listOf(ElementModelError("/", "delegate end error")))

        val actualResponse = set(model, delegate, null)

        assertSetResponse(expectedResponse, actualResponse)
        verifySequence {
            delegate.begin()
            delegate.setEntity(
                SetAux.CREATE,
                ofType(),
                "/address_book",
                "/address_book",
                ofType(),
                fieldsWithNames(),
                fieldsWithNames(),
                fieldsWithNames("name", "last_updated")
            )
            delegate.end()
        }
    }
}