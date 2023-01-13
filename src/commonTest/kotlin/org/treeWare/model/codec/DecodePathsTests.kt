package org.treeWare.model.codec

import okio.Buffer
import org.treeWare.metaModel.addressBookMetaModel
import org.treeWare.model.assertMatchesJsonString
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.decoder.DecodePathResult
import org.treeWare.model.decoder.decodePath
import org.treeWare.model.decoder.decodePaths
import org.treeWare.model.encoder.EncodePasswords
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

private const val ADDRESS_BOOK_MAIN_NAME = "address_book"

class DecodePathsTests {
    // region Multiple paths

    @Test
    fun `decodePaths() must decode paths`() {
        val paths = """
            |/address_book/name = Super Heroes
            |/address_book/last_updated = 1587147731
            |/address_book/settings/last_name_first = true
            |/address_book/settings/encrypt_hero_name = false
            |/address_book/settings/background_color = white
            |/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/first_name = Clark
            |/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/last_name = Kent
            |/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/hero_name = Superman
        """.trimMargin()

        val expectedJson = """
            |{
            |  "address_book": {
            |    "name": "Super Heroes",
            |    "last_updated": "1587147731",
            |    "settings": {
            |      "last_name_first": true,
            |      "encrypt_hero_name": false,
            |      "background_color": "white"
            |    },
            |    "person": [
            |      {
            |        "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |        "first_name": "Clark",
            |        "last_name": "Kent",
            |        "hero_name": "Superman"
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val errors = decodePaths(Buffer().writeUtf8(paths), mainModel)

        assertEquals("", errors.joinToString("\n"))
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Single paths

    // region 0 trailing wildcards

    @Test
    fun `decodePath() must decode a root path with 0 trailing wildcards`() {
        val path = "/address_book"
        val expectedJson = """
            {
              "address_book": {}
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    // region Fields in the root with 0 trailing wildcards

    @Test
    fun `decodePath() must decode a primitive-field in the root with 0 trailing wildcards`() {
        val path = "/address_book/name"
        val expectedJson = """
            {
              "address_book": {
                "name": null
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a composition-field in the root with 0 trailing wildcards`() {
        val path = "/address_book/settings"
        val expectedJson = """
            {
              "address_book": {
                "settings": null
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a set-field in the root with 0 trailing wildcards`() {
        val path = "/address_book/person"
        val expectedJson = """
            {
              "address_book": {
                "person": []
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Fields in a single-field entity with 0 trailing wildcards

    @Test
    fun `decodePath() must decode a primitive-field in a single-field entity with 0 trailing wildcards`() {
        val path = "/address_book/settings/last_name_first"
        val expectedJson = """
            {
              "address_book": {
                "settings": {
                  "last_name_first": null
                }
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a composition-field in a single-field entity with 0 trailing wildcards`() {
        val path = "/address_book/settings/advanced"
        val expectedJson = """
            {
              "address_book": {
                "settings": {
                  "advanced": null
                }
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Fields in a non-wildcard set-field entity with 0 trailing wildcards

    @Test
    fun `decodePath() must decode a primitive-field in a non-wildcard set-field entity with 0 trailing wildcards`() {
        val path = "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/first_name"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                    "first_name": null
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a composition-field in a non-wildcard set-field entity with 0 trailing wildcards`() {
        val path = "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/hero_details"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                    "hero_details": null
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a set-field in a non-wildcard set-field entity with 0 trailing wildcards`() {
        val path = "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                    "relation": []
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Fields in a wildcard set-field entity with 0 trailing wildcards

    @Test
    fun `decodePath() must decode a primitive-field in a wildcard set-field entity with 0 trailing wildcards`() {
        val path = "/address_book/person/*/first_name"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": null,
                    "first_name": null
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a composition-field in a wildcard set-field entity with 0 trailing wildcards`() {
        val path = "/address_book/person/*/hero_details"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": null,
                    "hero_details": null
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a set-field in a wildcard set-field entity with 0 trailing wildcards`() {
        val path = "/address_book/person/*/relation"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": null,
                    "relation": []
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Set-field entity paths with 0 trailing wildcards

    @Test
    fun `decodePath() must decode a non-wildcard set-field entity in the root with 0 trailing wildcards`() {
        val path = "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a non-wildcard set-field entity in another non-wildcard set-field entity with 0 trailing wildcards`() {
        val path =
            "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation/05ade278-4b44-43da-a0cc-14463854e397"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                    "relation": [
                      {
                        "id": "05ade278-4b44-43da-a0cc-14463854e397"
                      }
                    ]
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a non-wildcard set-field entity in another wildcard set-field entity with 0 trailing wildcards`() {
        val path = "/address_book/person/*/relation/05ade278-4b44-43da-a0cc-14463854e397"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": null,
                    "relation": [
                      {
                        "id": "05ade278-4b44-43da-a0cc-14463854e397"
                      }
                    ]
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Escaped paths with 0 trailing wildcards

    @Test
    fun `decodePath() must decode a field path with escaped keys with 0 trailing wildcards`() {
        val path = "/address_book/groups/Group\\/1\\\\/sub_groups/Group\\/1\\\\\\/1/info"
        val expectedJson = """
            {
              "address_book": {
                "groups": [
                  {
                    "name": "Group/1\\",
                    "sub_groups": [
                      {
                        "name": "Group/1\\/1",
                        "info": null
                      }
                    ]
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a set-field entity path with escaped keys with 0 trailing wildcards`() {
        val path = "/address_book/groups/Group\\/1\\\\/sub_groups/Group\\/1\\\\\\/1"
        val expectedJson = """
            {
              "address_book": {
                "groups": [
                  {
                    "name": "Group/1\\",
                    "sub_groups": [
                      {
                        "name": "Group/1\\/1"
                      }
                    ]
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode an escaped wildcard key in a field path with 0 trailing wildcards`() {
        val path = "/address_book/person/\\*/first_name"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": "*",
                    "first_name": null
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode an escaped wildcard key in a set-field entity path with 0 trailing wildcards`() {
        val path = "/address_book/person/\\*"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": "*"
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // endregion

    // region 1 trailing wildcard

    @Test
    fun `decodePath() must decode a root path with 1 trailing wildcard`() {
        val path = "/address_book/*"
        val expectedJson = """
            {
              "address_book": {}
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    // region Fields in the root with 1 trailing wildcard

    @Test
    fun `decodePath() must return an error for a primitive-field in the root with 1 trailing wildcard`() {
        val path = "/address_book/name/*"

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals(
            "Intermediate field `name` at index 2 in `$path` must be a composition",
            result.error
        )
    }

    @Test
    fun `decodePath() must decode a composition-field in the root with 1 trailing wildcard`() {
        val path = "/address_book/settings/*"
        val expectedJson = """
            {
              "address_book": {
                "settings": null
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a set-field in the root with 1 trailing wildcard`() {
        val path = "/address_book/person/*"
        val expectedJson = """
            {
              "address_book": {
                "person": []
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Fields in a single-field entity with 1 trailing wildcard

    @Test
    fun `decodePath() must return an error for a primitive-field in a single-field entity with 1 trailing wildcard`() {
        val path = "/address_book/settings/last_name_first/*"

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Intermediate field `last_name_first` at index 3 in `$path` must be a composition", result.error)
    }

    @Test
    fun `decodePath() must decode a composition-field in a single-field entity with 1 trailing wildcard`() {
        val path = "/address_book/settings/advanced/*"
        val expectedJson = """
            {
              "address_book": {
                "settings": {
                  "advanced": null
                }
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Fields in a non-wildcard set-field entity with 1 trailing wildcard

    @Test
    fun `decodePath() must return an error for a primitive-field in a non-wildcard set-field entity with 1 trailing wildcard`() {
        val path = "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/first_name/*"

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Intermediate field `first_name` at index 4 in `$path` must be a composition", result.error)
    }

    @Test
    fun `decodePath() must decode a composition-field in a non-wildcard set-field entity with 1 trailing wildcard`() {
        val path = "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/hero_details/*"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                    "hero_details": null
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a set-field in a non-wildcard set-field entity with 1 trailing wildcard`() {
        val path = "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation/*"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                    "relation": []
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Fields in a wildcard set-field entity with 1 trailing wildcard

    @Test
    fun `decodePath() must return an error for a primitive-field in a wildcard set-field entity with 1 trailing wildcard`() {
        val path = "/address_book/person/*/first_name/*"

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Intermediate field `first_name` at index 4 in `$path` must be a composition", result.error)
    }

    @Test
    fun `decodePath() must decode a composition-field in a wildcard set-field entity with 1 trailing wildcard`() {
        val path = "/address_book/person/*/hero_details/*"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": null,
                    "hero_details": null
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a set-field in a wildcard set-field entity with 1 trailing wildcard`() {
        val path = "/address_book/person/*/relation/*"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": null,
                    "relation": []
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Set-field entity paths with 1 trailing wildcard

    @Test
    fun `decodePath() must decode a non-wildcard set-field entity in the root with 1 trailing wildcard`() {
        val path = "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/*"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a non-wildcard set-field entity in another non-wildcard set-field entity with 1 trailing wildcard`() {
        val path =
            "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation/05ade278-4b44-43da-a0cc-14463854e397/*"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                    "relation": [
                      {
                        "id": "05ade278-4b44-43da-a0cc-14463854e397"
                      }
                    ]
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a non-wildcard set-field entity in another wildcard set-field entity with 1 trailing wildcard`() {
        val path = "/address_book/person/*/relation/05ade278-4b44-43da-a0cc-14463854e397/*"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": null,
                    "relation": [
                      {
                        "id": "05ade278-4b44-43da-a0cc-14463854e397"
                      }
                    ]
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Escaped paths with 1 trailing wildcard

    @Test
    fun `decodePath() must return an error for a primitive-field path with escaped keys with 1 trailing wildcard`() {
        val path = "/address_book/groups/Group\\/1\\\\/sub_groups/Group\\/1\\\\\\/1/info/*"

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Intermediate field `info` at index 6 in `$path` must be a composition", result.error)
    }

    @Test
    fun `decodePath() must decode a set-field entity path with escaped keys with 1 trailing wildcard`() {
        val path = "/address_book/groups/Group\\/1\\\\/sub_groups/Group\\/1\\\\\\/1/*"
        val expectedJson = """
            {
              "address_book": {
                "groups": [
                  {
                    "name": "Group/1\\",
                    "sub_groups": [
                      {
                        "name": "Group/1\\/1"
                      }
                    ]
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must return an error for an escaped wildcard key in a primitive-field path with 1 trailing wildcard`() {
        val path = "/address_book/person/\\*/first_name/*"

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Intermediate field `first_name` at index 4 in `$path` must be a composition", result.error)
    }

    @Test
    fun `decodePath() must decode an escaped wildcard key in a set-field entity path with 1 trailing wildcard`() {
        val path = "/address_book/person/\\*/*"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": "*"
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // endregion

    // region 2 trailing wildcards

    @Test
    fun `decodePath() must decode a root path with 2 trailing wildcards`() {
        val path = "/address_book/**"
        val expectedJson = """
            {
              "address_book": {}
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    // region Fields in the root with 2 trailing wildcards

    @Test
    fun `decodePath() must return an error for a primitive-field in the root with 2 trailing wildcards`() {
        val path = "/address_book/name/**"

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Intermediate field `name` at index 2 in `$path` must be a composition", result.error)
    }

    @Test
    fun `decodePath() must decode a composition-field in the root with 2 trailing wildcards`() {
        val path = "/address_book/settings/**"
        val expectedJson = """
            {
              "address_book": {
                "settings": null
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a set-field in the root with 2 trailing wildcards`() {
        val path = "/address_book/person/**"
        val expectedJson = """
            {
              "address_book": {
                "person": []
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Fields in a single-field entity with 2 trailing wildcards

    @Test
    fun `decodePath() must return an error for a primitive-field in a single-field entity with 2 trailing wildcards`() {
        val path = "/address_book/settings/last_name_first/**"

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Intermediate field `last_name_first` at index 3 in `$path` must be a composition", result.error)
    }

    @Test
    fun `decodePath() must decode a composition-field in a single-field entity with 2 trailing wildcards`() {
        val path = "/address_book/settings/advanced/**"
        val expectedJson = """
            {
              "address_book": {
                "settings": {
                  "advanced": null
                }
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Fields in a non-wildcard set-field entity with 2 trailing wildcards

    @Test
    fun `decodePath() must return an error for a primitive-field in a non-wildcard set-field entity with 2 trailing wildcards`() {
        val path = "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/first_name/**"

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Intermediate field `first_name` at index 4 in `$path` must be a composition", result.error)
    }

    @Test
    fun `decodePath() must decode a composition-field in a non-wildcard set-field entity with 2 trailing wildcards`() {
        val path = "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/hero_details/**"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                    "hero_details": null
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a set-field in a non-wildcard set-field entity with 2 trailing wildcards`() {
        val path = "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation/**"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                    "relation": []
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Fields in a wildcard set-field entity with 2 trailing wildcards

    @Test
    fun `decodePath() must return an error for a primitive-field in a wildcard set-field entity with 2 trailing wildcards`() {
        val path = "/address_book/person/*/first_name/**"

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Intermediate field `first_name` at index 4 in `$path` must be a composition", result.error)
    }

    @Test
    fun `decodePath() must decode a composition-field in a wildcard set-field entity with 2 trailing wildcards`() {
        val path = "/address_book/person/*/hero_details/**"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": null,
                    "hero_details": null
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a set-field in a wildcard set-field entity with 2 trailing wildcards`() {
        val path = "/address_book/person/*/relation/**"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": null,
                    "relation": []
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Set-field entity paths with 2 trailing wildcards

    @Test
    fun `decodePath() must decode a non-wildcard set-field entity in the root with 2 trailing wildcards`() {
        val path = "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/**"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a non-wildcard set-field entity in another non-wildcard set-field entity with 2 trailing wildcards`() {
        val path =
            "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relation/05ade278-4b44-43da-a0cc-14463854e397/**"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                    "relation": [
                      {
                        "id": "05ade278-4b44-43da-a0cc-14463854e397"
                      }
                    ]
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a non-wildcard set-field entity in another wildcard set-field entity with 2 trailing wildcards`() {
        val path = "/address_book/person/*/relation/05ade278-4b44-43da-a0cc-14463854e397/**"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": null,
                    "relation": [
                      {
                        "id": "05ade278-4b44-43da-a0cc-14463854e397"
                      }
                    ]
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Escaped paths with 2 trailing wildcards

    @Test
    fun `decodePath() must return an error for a primitive-field path with escaped keys with 2 trailing wildcards`() {
        val path = "/address_book/groups/Group\\/1\\\\/sub_groups/Group\\/1\\\\\\/1/info/**"

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Intermediate field `info` at index 6 in `$path` must be a composition", result.error)
    }

    @Test
    fun `decodePath() must decode a set-field entity path with escaped keys with 2 trailing wildcards`() {
        val path = "/address_book/groups/Group\\/1\\\\/sub_groups/Group\\/1\\\\\\/1/**"
        val expectedJson = """
            {
              "address_book": {
                "groups": [
                  {
                    "name": "Group/1\\",
                    "sub_groups": [
                      {
                        "name": "Group/1\\/1"
                      }
                    ]
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must return an error for an escaped wildcard key in a primitive-field path with 2 trailing wildcards`() {
        val path = "/address_book/person/\\*/first_name/**"

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Intermediate field `first_name` at index 4 in `$path` must be a composition", result.error)
    }

    @Test
    fun `decodePath() must decode an escaped wildcard key in a set-field entity path with 2 trailing wildcards`() {
        val path = "/address_book/person/\\*/**"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": "*"
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // endregion

    // region Key field paths

    @Test
    fun `decodePath() must decode a non-wildcard key-field path`() {
        val path = "/address_book/person/cc477201-48ec-4367-83a4-7fdbd92f8a6f/id"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a wildcard key-field path`() {
        val path = "/address_book/person/*/id"
        val expectedJson = """
            {
              "address_book": {
                "person": [
                  {
                    "id": null
                  }
                ]
              }
            }
        """.trimIndent()

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Miscellaneous errors

    @Test
    fun `decodePath() must return an error if the path contains unknown elements`() {
        val path = "/address_book/unknown_entity/unknown_field_name"

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Unknown field `unknown_entity` at index 2 in `$path`", result.error)
    }

    @Test
    fun `decodePath() must return an error if a non-composition field is in the middle of a path`() {
        val path = "/address_book/name/invalid_field_name"

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Intermediate field `name` at index 2 in `$path` must be a composition", result.error)
    }

    @Test
    fun `decodePath() must return an error if a wildcard is used as a field-name in the middle of a path`() {
        val path = "/address_book/*/*"

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Unknown field `*` at index 2 in `$path`", result.error)
    }

    @Test
    fun `decodePath() must return an error if a sub-tree-wildcard is used as a field-name in the middle of a path`() {
        val path = "/address_book/**/*"

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Unknown field `**` at index 2 in `$path`", result.error)
    }

    @Test
    fun `decodePath() must return an error if a sub-tree-wildcard is used as a key-value in the middle of a path`() {
        val path = "/address_book/person/**/first_name"

        val mainModel = MutableMainModel(addressBookMetaModel)
        val result = decodePath(path, null, mainModel, ADDRESS_BOOK_MAIN_NAME)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Sub-tree wildcard `**` at index 3 in `$path` is invalid in the middle of a path", result.error)
    }

    // endregion

    // endregion
}