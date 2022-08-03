package org.treeWare.model.operator

import org.treeWare.metaModel.addressBookMetaModel
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.getMainModelFromJsonString
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
            |  "address_book__set_": "create",
            |  "address_book": {
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
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
            "/address_book: missing required fields for `create`: [name]",
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]: missing required fields for `create`: [first_name, last_name]",
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must not return errors if required fields are specified in create-request`() {
        val modelJson = """
            |{
            |  "address_book__set_": "create",
            |  "address_book": {
            |    "name": "Super Heroes",
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

        val expectedErrors = emptyList<String>()
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if required fields are missing in update-request`() {
        val modelJson = """
            |{
            |  "address_book__set_": "update",
            |  "address_book": {
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
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

    @Test
    fun `validateSet() must return errors if required fields are missing in delete-request`() {
        val modelJson = """
            |{
            |  "address_book": {
            |    "set_": "delete",
            |    "person": [
            |      {
            |        "set_": "delete",
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
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

    @Test
    fun `validateSet() must return errors if string min_size constraint is not met`() {
        val modelJson = """
            |{
            |  "address_book__set_": "create",
            |  "address_book": {
            |    "name": "A",
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

        val expectedErrors = listOf("/address_book/name: length 1 of string 'A' is less than minimum size 2")
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if string max_size constraint is not met`() {
        val modelJson = """
            |{
            |  "address_book__set_": "create",
            |  "address_book": {
            |    "name": "Super Heroes",
            |    "groups": [
            |      {
            |        "name": "a0123456789"
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

        val expectedErrors =
            listOf("/address_book/groups[a0123456789]/name: length 11 of string 'a0123456789' is more than maximum size 10")
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if string regex constraint is not met`() {
        val modelJson = """
            |{
            |  "address_book__set_": "create",
            |  "address_book": {
            |    "name": "Super Heroes",
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent",
            |        "email": [
            |          {
            |            "value": "valid@email.com"
            |          },
            |          {
            |            "value": "invalid_email_1"
            |          },
            |          {
            |            "value": "also-valid@another_email.com"
            |          },
            |          {
            |            "value": "invalid_email_2"
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

        val expectedErrors = listOf(
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/email[1]: string 'invalid_email_1' does not match regex '[a-zA-Z\\.\\-_]+@[a-zA-Z\\.\\-_]+'",
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/email[3]: string 'invalid_email_2' does not match regex '[a-zA-Z\\.\\-_]+@[a-zA-Z\\.\\-_]+'"
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if constraints are not met in composition keys`() {
        val modelJson = """
            |{
            |  "address_book__set_": "create",
            |  "address_book": {
            |    "name": "Super Heroes",
            |    "city_info": [
            |      {
            |        "city": {
            |          "country": "U",
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

        val expectedErrors =
            listOf("/address_book/city_info[New York City,New York,U]/city/country: length 1 of string 'U' is less than minimum size 2")
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if association is an empty path`() {
        val modelJson = """
            |{
            |  "address_book__set_": "create",
            |  "address_book": {
            |    "name": "Super Heroes",
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent",
            |        "group": {}
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
            |          {}
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

        val expectedErrors = listOf(
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/group: association has an invalid target type",
            "/address_book/city_info[New York City,New York,United States of America]/related_city_info[0]: association has an invalid target type"
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if association target type is incorrect`() {
        val modelJson = """
            |{
            |  "address_book__set_": "create",
            |  "address_book": {
            |    "name": "Super Heroes",
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent",
            |        "group": {
            |          "person": [
            |            {
            |              "id": "a8aacf55-7810-4b43-afe5-4344f25435fd"
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
            |            "groups": [
            |              {
            |                "name": "DC",
            |                "sub_groups": [
            |                  {
            |                    "name": "Superman"
            |                  }
            |                ]
            |              }
            |            ]
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

        val expectedErrors = listOf(
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/group: association has an invalid target type",
            "/address_book/city_info[New York City,New York,United States of America]/related_city_info[0]: association has an invalid target type"
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if association has non-key fields`() {
        val modelJson = """
            |{
            |  "address_book__set_": "create",
            |  "address_book": {
            |    "name": "Super Heroes",
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent",
            |        "group": {
            |          "name": "additional field for causing multiple paths",
            |          "groups": [
            |            {
            |              "name": "DC",
            |              "sub_groups": [
            |                {
            |                  "name": "Superman",
            |                  "info": "non-key field for causing error"
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
            |                "info": "non-key field for causing error",
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
            |            ],
            |            "name": "non-key field for causing error"
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

        val expectedErrors = listOf(
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/group: association has non-key fields",
            "/address_book/city_info[New York City,New York,United States of America]/related_city_info[0]: association has non-key fields",
            "/address_book/city_info[Albany,New York,United States of America]/related_city_info[0]: association has non-key fields"
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if association has multiple paths`() {
        val modelJson = """
            |{
            |  "address_book__set_": "create",
            |  "address_book": {
            |    "name": "Super Heroes",
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent",
            |        "group": {
            |          "person": [],
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
            |            "person": [],
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
            |            ],
            |            "person": []
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

        val expectedErrors = listOf(
            "/address_book/person[cc477201-48ec-4367-83a4-7fdbd92f8a6f]/group: association has multiple paths",
            "/address_book/city_info[New York City,New York,United States of America]/related_city_info[0]: association has multiple paths",
            "/address_book/city_info[Albany,New York,United States of America]/related_city_info[0]: association has multiple paths"
        )
        val actualErrors = validateSet(model)
        assertEquals(expectedErrors.joinToString("\n"), actualErrors.joinToString("\n"))
    }

    @Test
    fun `validateSet() must return errors if association is missing keys`() {
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
            |              "sub_groups": [
            |                {
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
            |                }
            |              }
            |            ]
            |          }
            |        ]
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val expectedDecodeErrors = listOf(
            "Missing key fields [name] in instance of /address_book.main/group",
            "Missing key fields [name] in instance of /address_book.main/group",
            "Missing key fields [name, state, country] in instance of /address_book.city/address_book_city",
        )
        // NOTE: The following function will assert if the decode errors do not match the above expectedDecodeErrors.
        getMainModelFromJsonString(
            addressBookMetaModel,
            modelJson,
            expectedDecodeErrors = expectedDecodeErrors,
            multiAuxDecodingStateMachineFactory = auxDecodingFactory
        )
    }
}