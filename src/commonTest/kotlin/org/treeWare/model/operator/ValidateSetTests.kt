package org.treeWare.model.operator

import org.treeWare.model.AddressBookMutableEntityModelFactory
import org.treeWare.model.decodeJsonStringIntoEntity
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.operator.set.aux.SET_AUX_NAME
import org.treeWare.model.operator.set.aux.SetAuxStateMachine
import kotlin.test.Test
import kotlin.test.assertEquals

private val auxDecodingFactory = MultiAuxDecodingStateMachineFactory(SET_AUX_NAME to { SetAuxStateMachine(it) })

class ValidateSetTests {
    @Test
    fun `validateSet() must return errors if required fields are missing in create-request`() {
        val modelJson = """
            |{
            |  "set_": "create",
            |  "person": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "is_hero": true
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
            "/: required field not found: name",
            "/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f: required field not found: first_name",
            "/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f: required field not found: last_name",
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if required fields are specified in create-request`() {
        val modelJson = """
            |{
            |  "set_": "create",
            |  "name": "Super Heroes",
            |  "person": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "is_hero": true
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
    fun `validateSet() must return errors if required fields in an optional entity are missing in create-request`() {
        val modelJson = """
            |{
            |  "set_": "create",
            |  "name": "Super Heroes",
            |  "settings": {}
            |}
        """.trimMargin()
        val model = AddressBookMutableEntityModelFactory.create()
        decodeJsonStringIntoEntity(
            modelJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = model
        )

        val expectedErrors = listOf(
            "/settings: required field not found: last_name_first",
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if required fields are missing in update-request`() {
        val modelJson = """
            |{
            |  "set_": "update",
            |  "person": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "is_hero": true
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
    fun `validateSet() must return errors if required fields are missing in sub_tree granularity in update-request`() {
        val modelJson = """
            |{
            |  "set_": "update",
            |  "sub_tree_persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "is_hero": true
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
            "/sub_tree_persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f: required field not found: first_name",
            "/sub_tree_persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f: required field not found: last_name",
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if required fields are missing in delete-request`() {
        val modelJson = """
            |{
            |  "set_": "delete",
            |  "person": [
            |    {
            |      "set_": "delete",
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
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
    fun `validateSet() must return errors if string min_size constraint is not met`() {
        val modelJson = """
            |{
            |  "set_": "create",
            |  "name": "A",
            |  "last_updated": "1587147731"
            |}
        """.trimMargin()
        val model = AddressBookMutableEntityModelFactory.create()
        decodeJsonStringIntoEntity(
            modelJson,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = model
        )

        val expectedErrors = listOf("/name: length 1 of string 'A' is less than minimum size 2")
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if string max_size constraint is not met`() {
        val modelJson = """
            |{
            |  "set_": "create",
            |  "name": "Super Heroes",
            |  "groups": [
            |    {
            |      "name": "a0123456789"
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

        val expectedErrors =
            listOf("/groups/a0123456789/name: length 11 of string 'a0123456789' is more than maximum size 10")
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if string regex constraint is not met`() {
        val modelJson = """
            |{
            |  "set_": "create",
            |  "name": "Super Heroes",
            |  "person": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "is_hero": true,
            |      "email": [
            |        {
            |          "value": "valid@email.com"
            |        },
            |        {
            |          "value": "invalid_email_1"
            |        },
            |        {
            |          "value": "also-valid@another_email.com"
            |        },
            |        {
            |          "value": "invalid_email_2"
            |        }
            |      ]
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
            "/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/email/1: string 'invalid_email_1' does not match regex '[a-zA-Z\\.\\-_]+@[a-zA-Z\\.\\-_]+'",
            "/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/email/3: string 'invalid_email_2' does not match regex '[a-zA-Z\\.\\-_]+@[a-zA-Z\\.\\-_]+'"
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if constraints are not met in composition keys`() {
        val modelJson = """
            |{
            |  "set_": "create",
            |  "name": "Super Heroes",
            |  "city_info": [
            |    {
            |      "city": {
            |        "country": "U",
            |        "state": "New York",
            |        "name": "New York City"
            |      },
            |      "info": "One of the most populous and most densely populated major city in USA",
            |      "is_coastal_city": false
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

        val expectedErrors =
            listOf("/city_info/New York City/New York/U/city/country: length 1 of string 'U' is less than minimum size 2")
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if association is an empty path`() {
        val modelJson = """
            |{
            |  "set_": "create",
            |  "name": "Super Heroes",
            |  "person": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "is_hero": true,
            |      "group": {}
            |    }
            |  ],
            |  "city_info": [
            |    {
            |      "city": {
            |        "name": "New York City",
            |        "state": "New York",
            |        "country": "United States of America"
            |      },
            |      "info": "One of the most populous and most densely populated major city in USA",
            |      "is_coastal_city": false,
            |      "related_city_info": [
            |        {}
            |      ]
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
            "/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/group: association has an invalid target type",
            "/city_info/New York City/New York/United States of America/related_city_info/0: association has an invalid target type"
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if association target type is incorrect`() {
        val modelJson = """
            |{
            |  "set_": "create",
            |  "name": "Super Heroes",
            |  "person": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "is_hero": true,
            |      "group": {
            |        "person": [
            |          {
            |            "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
            |          }
            |        ]
            |      }
            |    }
            |  ],
            |  "city_info": [
            |    {
            |      "city": {
            |        "name": "New York City",
            |        "state": "New York",
            |        "country": "United States of America"
            |      },
            |      "info": "One of the most populous and most densely populated major city in USA",
            |      "is_coastal_city": false,
            |      "related_city_info": [
            |        {
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
            |      ]
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
            "/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/group: association has an invalid target type",
            "/city_info/New York City/New York/United States of America/related_city_info/0: association has an invalid target type"
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if association has non-key fields`() {
        val modelJson = """
            |{
            |  "set_": "create",
            |  "name": "Super Heroes",
            |  "person": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "is_hero": true,
            |      "group": {
            |        "name": "additional field for causing multiple paths",
            |        "groups": [
            |          {
            |            "name": "DC",
            |            "sub_groups": [
            |              {
            |                "name": "Superman",
            |                "info": "non-key field for causing error"
            |              }
            |            ]
            |          }
            |        ]
            |      }
            |    }
            |  ],
            |  "city_info": [
            |    {
            |      "city": {
            |        "name": "New York City",
            |        "state": "New York",
            |        "country": "United States of America"
            |      },
            |      "info": "One of the most populous and most densely populated major city in USA",
            |      "is_coastal_city": false,
            |      "related_city_info": [
            |        {
            |          "city_info": [
            |            {
            |              "info": "non-key field for causing error",
            |              "city": {
            |                "name": "Albany",
            |                "state": "New York",
            |                "country": "United States of America"
            |              }
            |            }
            |          ]
            |        }
            |      ]
            |    },
            |    {
            |      "related_city_info": [
            |        {
            |          "city_info": [
            |            {
            |              "city": {
            |                "name": "New York City",
            |                "state": "New York",
            |                "country": "United States of America"
            |              }
            |            }
            |          ],
            |          "name": "non-key field for causing error"
            |        }
            |      ],
            |      "info": "Capital of New York state",
            |      "is_coastal_city": false,
            |      "city": {
            |        "name": "Albany",
            |        "state": "New York",
            |        "country": "United States of America"
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
            "/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/group: association has non-key fields",
            "/city_info/New York City/New York/United States of America/related_city_info/0: association has non-key fields",
            "/city_info/Albany/New York/United States of America/related_city_info/0: association has non-key fields"
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if association has multiple paths`() {
        val modelJson = """
            |{
            |  "set_": "create",
            |  "name": "Super Heroes",
            |  "person": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "is_hero": true,
            |      "group": {
            |        "person": [],
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
            |  ],
            |  "city_info": [
            |    {
            |      "city": {
            |        "name": "New York City",
            |        "state": "New York",
            |        "country": "United States of America"
            |      },
            |      "info": "One of the most populous and most densely populated major city in USA",
            |      "is_coastal_city": false,
            |      "related_city_info": [
            |        {
            |          "person": [],
            |          "city_info": [
            |            {
            |              "city": {
            |                "name": "Albany",
            |                "state": "New York",
            |                "country": "United States of America"
            |              }
            |            }
            |          ]
            |        }
            |      ]
            |    },
            |    {
            |      "related_city_info": [
            |        {
            |          "city_info": [
            |            {
            |              "city": {
            |                "name": "New York City",
            |                "state": "New York",
            |                "country": "United States of America"
            |              }
            |            }
            |          ],
            |          "person": []
            |        }
            |      ],
            |      "info": "Capital of New York state",
            |      "is_coastal_city": false,
            |      "city": {
            |        "name": "Albany",
            |        "state": "New York",
            |        "country": "United States of America"
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
            "/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/group: association has multiple paths",
            "/city_info/New York City/New York/United States of America/related_city_info/0: association has multiple paths",
            "/city_info/Albany/New York/United States of America/related_city_info/0: association has multiple paths"
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if association is missing keys`() {
        val modelJson = """
            |{
            |  "set_": "create",
            |  "name": "Super Heroes",
            |  "person": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "group": {
            |        "groups": [
            |          {
            |            "sub_groups": [
            |              {
            |              }
            |            ]
            |          }
            |        ]
            |      }
            |    }
            |  ],
            |  "city_info": [
            |    {
            |      "city": {
            |        "name": "New York City",
            |        "state": "New York",
            |        "country": "United States of America"
            |      },
            |      "info": "One of the most populous and most densely populated major city in USA",
            |      "related_city_info": [
            |        {
            |          "city_info": [
            |            {
            |              "city": {
            |              }
            |            }
            |          ]
            |        }
            |      ]
            |    }
            |  ]
            |}
        """.trimMargin()
        val expectedDecodeErrors = listOf(
            "Missing key fields [name] in instance of /address_book.main/group",
            "Missing key fields [name] in instance of /address_book.main/group",
            "Missing key fields [name, state, country] in instance of /address_book.city/address_book_city",
        )
        val model =
            AddressBookMutableEntityModelFactory.create()
        // NOTE: The following function will assert if the decode errors do not match the above expectedDecodeErrors.
        decodeJsonStringIntoEntity(
            modelJson,
            expectedDecodeErrors = expectedDecodeErrors,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory,
            entity = model
        )
    }
}