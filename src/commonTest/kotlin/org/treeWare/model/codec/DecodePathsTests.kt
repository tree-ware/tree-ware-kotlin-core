package org.treeWare.model.codec

import okio.Buffer
import org.treeWare.metaModel.addressBookRootEntityFactory
import org.treeWare.model.assertMatchesJsonString
import org.treeWare.model.decoder.DecodePathResult
import org.treeWare.model.decoder.decodePath
import org.treeWare.model.decoder.decodePaths
import org.treeWare.model.encoder.EncodePasswords
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DecodePathsTests {
    // region Multiple paths

    @Test
    fun `decodePaths() must decode paths`() {
        val paths = """
            |/name = Super Heroes
            |/last_updated = 1587147731
            |/settings/last_name_first = true
            |/settings/encrypt_hero_name = false
            |/settings/background_color = white
            |/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/first_name = Clark
            |/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/last_name = Kent
            |/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/hero_name = Superman
        """.trimMargin()

        val expectedJson = """
            |{
            |  "name": "Super Heroes",
            |  "last_updated": "1587147731",
            |  "settings": {
            |    "last_name_first": true,
            |    "encrypt_hero_name": false,
            |    "background_color": "white"
            |  },
            |  "persons": [
            |    {
            |      "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
            |      "first_name": "Clark",
            |      "last_name": "Kent",
            |      "hero_name": "Superman"
            |    }
            |  ]
            |}
        """.trimMargin()

        val model = addressBookRootEntityFactory(null)
        val errors = decodePaths(Buffer().writeUtf8(paths), model)

        assertEquals("", errors.joinToString("\n"))
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Single paths

    // region 0 trailing wildcards

    @Test
    fun `decodePath() must decode a root path with 0 trailing wildcards`() {
        val path = "/"
        val expectedJson = """
            {}
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    // region Fields in the root with 0 trailing wildcards

    @Test
    fun `decodePath() must decode a primitive-field in the root with 0 trailing wildcards`() {
        val path = "/name"
        val expectedJson = """
            {
              "name": null
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a composition-field in the root with 0 trailing wildcards`() {
        val path = "/settings"
        val expectedJson = """
            {
              "settings": null
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a set-field in the root with 0 trailing wildcards`() {
        val path = "/persons"
        val expectedJson = """
            {
              "persons": []
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Fields in a single-field entity with 0 trailing wildcards

    @Test
    fun `decodePath() must decode a primitive-field in a single-field entity with 0 trailing wildcards`() {
        val path = "/settings/last_name_first"
        val expectedJson = """
            {
              "settings": {
                "last_name_first": null
              }
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a composition-field in a single-field entity with 0 trailing wildcards`() {
        val path = "/settings/advanced"
        val expectedJson = """
            {
              "settings": {
                "advanced": null
              }
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Fields in a non-wildcard set-field entity with 0 trailing wildcards

    @Test
    fun `decodePath() must decode a primitive-field in a non-wildcard set-field entity with 0 trailing wildcards`() {
        val path = "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/first_name"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                  "first_name": null
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a composition-field in a non-wildcard set-field entity with 0 trailing wildcards`() {
        val path = "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/hero_details"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                  "hero_details": null
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a set-field in a non-wildcard set-field entity with 0 trailing wildcards`() {
        val path = "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relations"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                  "relations": []
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Fields in a wildcard set-field entity with 0 trailing wildcards

    @Test
    fun `decodePath() must decode a primitive-field in a wildcard set-field entity with 0 trailing wildcards`() {
        val path = "/persons/*/first_name"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": null,
                  "first_name": null
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a composition-field in a wildcard set-field entity with 0 trailing wildcards`() {
        val path = "/persons/*/hero_details"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": null,
                  "hero_details": null
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a set-field in a wildcard set-field entity with 0 trailing wildcards`() {
        val path = "/persons/*/relations"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": null,
                  "relations": []
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Set-field entity paths with 0 trailing wildcards

    @Test
    fun `decodePath() must decode a non-wildcard set-field entity in the root with 0 trailing wildcards`() {
        val path = "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a non-wildcard set-field entity in another non-wildcard set-field entity with 0 trailing wildcards`() {
        val path =
            "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relations/05ade278-4b44-43da-a0cc-14463854e397"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                  "relations": [
                    {
                      "id": "05ade278-4b44-43da-a0cc-14463854e397"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a non-wildcard set-field entity in another wildcard set-field entity with 0 trailing wildcards`() {
        val path = "/persons/*/relations/05ade278-4b44-43da-a0cc-14463854e397"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": null,
                  "relations": [
                    {
                      "id": "05ade278-4b44-43da-a0cc-14463854e397"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Escaped paths with 0 trailing wildcards

    @Test
    fun `decodePath() must decode a field path with escaped keys with 0 trailing wildcards`() {
        val path = "/groups/Group\\/1\\\\/sub_groups/Group\\/1\\\\\\/1/info"
        val expectedJson = """
            {
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
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a set-field entity path with escaped keys with 0 trailing wildcards`() {
        val path = "/groups/Group\\/1\\\\/sub_groups/Group\\/1\\\\\\/1"
        val expectedJson = """
            {
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
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode an escaped wildcard key in a field path with 0 trailing wildcards`() {
        val path = "/persons/\\*/first_name"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": "*",
                  "first_name": null
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode an escaped wildcard key in a set-field entity path with 0 trailing wildcards`() {
        val path = "/persons/\\*"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": "*"
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // endregion

    // region 1 trailing wildcard

    @Test
    fun `decodePath() must decode a root path with 1 trailing wildcard`() {
        val path = "/*"
        val expectedJson = """
            {}
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    // region Fields in the root with 1 trailing wildcard

    @Test
    fun `decodePath() must return an error for a primitive-field in the root with 1 trailing wildcard`() {
        val path = "/name/*"

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals(
            "Intermediate field `name` at index 1 in `$path` must be a composition",
            result.error
        )
    }

    @Test
    fun `decodePath() must decode a composition-field in the root with 1 trailing wildcard`() {
        val path = "/settings/*"
        val expectedJson = """
            {
              "settings": null
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a set-field in the root with 1 trailing wildcard`() {
        val path = "/persons/*"
        val expectedJson = """
            {
              "persons": []
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Fields in a single-field entity with 1 trailing wildcard

    @Test
    fun `decodePath() must return an error for a primitive-field in a single-field entity with 1 trailing wildcard`() {
        val path = "/settings/last_name_first/*"

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Intermediate field `last_name_first` at index 2 in `$path` must be a composition", result.error)
    }

    @Test
    fun `decodePath() must decode a composition-field in a single-field entity with 1 trailing wildcard`() {
        val path = "/settings/advanced/*"
        val expectedJson = """
            {
              "settings": {
                "advanced": null
              }
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Fields in a non-wildcard set-field entity with 1 trailing wildcard

    @Test
    fun `decodePath() must return an error for a primitive-field in a non-wildcard set-field entity with 1 trailing wildcard`() {
        val path = "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/first_name/*"

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Intermediate field `first_name` at index 3 in `$path` must be a composition", result.error)
    }

    @Test
    fun `decodePath() must decode a composition-field in a non-wildcard set-field entity with 1 trailing wildcard`() {
        val path = "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/hero_details/*"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                  "hero_details": null
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a set-field in a non-wildcard set-field entity with 1 trailing wildcard`() {
        val path = "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relations/*"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                  "relations": []
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Fields in a wildcard set-field entity with 1 trailing wildcard

    @Test
    fun `decodePath() must return an error for a primitive-field in a wildcard set-field entity with 1 trailing wildcard`() {
        val path = "/persons/*/first_name/*"

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Intermediate field `first_name` at index 3 in `$path` must be a composition", result.error)
    }

    @Test
    fun `decodePath() must decode a composition-field in a wildcard set-field entity with 1 trailing wildcard`() {
        val path = "/persons/*/hero_details/*"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": null,
                  "hero_details": null
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a set-field in a wildcard set-field entity with 1 trailing wildcard`() {
        val path = "/persons/*/relations/*"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": null,
                  "relations": []
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Set-field entity paths with 1 trailing wildcard

    @Test
    fun `decodePath() must decode a non-wildcard set-field entity in the root with 1 trailing wildcard`() {
        val path = "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/*"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a non-wildcard set-field entity in another non-wildcard set-field entity with 1 trailing wildcard`() {
        val path =
            "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relations/05ade278-4b44-43da-a0cc-14463854e397/*"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                  "relations": [
                    {
                      "id": "05ade278-4b44-43da-a0cc-14463854e397"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a non-wildcard set-field entity in another wildcard set-field entity with 1 trailing wildcard`() {
        val path = "/persons/*/relations/05ade278-4b44-43da-a0cc-14463854e397/*"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": null,
                  "relations": [
                    {
                      "id": "05ade278-4b44-43da-a0cc-14463854e397"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Escaped paths with 1 trailing wildcard

    @Test
    fun `decodePath() must return an error for a primitive-field path with escaped keys with 1 trailing wildcard`() {
        val path = "/groups/Group\\/1\\\\/sub_groups/Group\\/1\\\\\\/1/info/*"

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Intermediate field `info` at index 5 in `$path` must be a composition", result.error)
    }

    @Test
    fun `decodePath() must decode a set-field entity path with escaped keys with 1 trailing wildcard`() {
        val path = "/groups/Group\\/1\\\\/sub_groups/Group\\/1\\\\\\/1/*"
        val expectedJson = """
            {
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
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must return an error for an escaped wildcard key in a primitive-field path with 1 trailing wildcard`() {
        val path = "/persons/\\*/first_name/*"

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Intermediate field `first_name` at index 3 in `$path` must be a composition", result.error)
    }

    @Test
    fun `decodePath() must decode an escaped wildcard key in a set-field entity path with 1 trailing wildcard`() {
        val path = "/persons/\\*/*"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": "*"
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(1, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // endregion

    // region 2 trailing wildcards

    @Test
    fun `decodePath() must decode a root path with 2 trailing wildcards`() {
        val path = "/**"
        val expectedJson = """
            {}
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    // region Fields in the root with 2 trailing wildcards

    @Test
    fun `decodePath() must return an error for a primitive-field in the root with 2 trailing wildcards`() {
        val path = "/name/**"

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Intermediate field `name` at index 1 in `$path` must be a composition", result.error)
    }

    @Test
    fun `decodePath() must decode a composition-field in the root with 2 trailing wildcards`() {
        val path = "/settings/**"
        val expectedJson = """
            {
              "settings": null
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a set-field in the root with 2 trailing wildcards`() {
        val path = "/persons/**"
        val expectedJson = """
            {
              "persons": []
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Fields in a single-field entity with 2 trailing wildcards

    @Test
    fun `decodePath() must return an error for a primitive-field in a single-field entity with 2 trailing wildcards`() {
        val path = "/settings/last_name_first/**"

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Intermediate field `last_name_first` at index 2 in `$path` must be a composition", result.error)
    }

    @Test
    fun `decodePath() must decode a composition-field in a single-field entity with 2 trailing wildcards`() {
        val path = "/settings/advanced/**"
        val expectedJson = """
            {
              "settings": {
                "advanced": null
              }
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Fields in a non-wildcard set-field entity with 2 trailing wildcards

    @Test
    fun `decodePath() must return an error for a primitive-field in a non-wildcard set-field entity with 2 trailing wildcards`() {
        val path = "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/first_name/**"

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Intermediate field `first_name` at index 3 in `$path` must be a composition", result.error)
    }

    @Test
    fun `decodePath() must decode a composition-field in a non-wildcard set-field entity with 2 trailing wildcards`() {
        val path = "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/hero_details/**"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                  "hero_details": null
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a set-field in a non-wildcard set-field entity with 2 trailing wildcards`() {
        val path = "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relations/**"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                  "relations": []
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Fields in a wildcard set-field entity with 2 trailing wildcards

    @Test
    fun `decodePath() must return an error for a primitive-field in a wildcard set-field entity with 2 trailing wildcards`() {
        val path = "/persons/*/first_name/**"

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Intermediate field `first_name` at index 3 in `$path` must be a composition", result.error)
    }

    @Test
    fun `decodePath() must decode a composition-field in a wildcard set-field entity with 2 trailing wildcards`() {
        val path = "/persons/*/hero_details/**"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": null,
                  "hero_details": null
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a set-field in a wildcard set-field entity with 2 trailing wildcards`() {
        val path = "/persons/*/relations/**"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": null,
                  "relations": []
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Set-field entity paths with 2 trailing wildcards

    @Test
    fun `decodePath() must decode a non-wildcard set-field entity in the root with 2 trailing wildcards`() {
        val path = "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/**"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a non-wildcard set-field entity in another non-wildcard set-field entity with 2 trailing wildcards`() {
        val path =
            "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/relations/05ade278-4b44-43da-a0cc-14463854e397/**"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f",
                  "relations": [
                    {
                      "id": "05ade278-4b44-43da-a0cc-14463854e397"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a non-wildcard set-field entity in another wildcard set-field entity with 2 trailing wildcards`() {
        val path = "/persons/*/relations/05ade278-4b44-43da-a0cc-14463854e397/**"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": null,
                  "relations": [
                    {
                      "id": "05ade278-4b44-43da-a0cc-14463854e397"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Escaped paths with 2 trailing wildcards

    @Test
    fun `decodePath() must return an error for a primitive-field path with escaped keys with 2 trailing wildcards`() {
        val path = "/groups/Group\\/1\\\\/sub_groups/Group\\/1\\\\\\/1/info/**"

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Intermediate field `info` at index 5 in `$path` must be a composition", result.error)
    }

    @Test
    fun `decodePath() must decode a set-field entity path with escaped keys with 2 trailing wildcards`() {
        val path = "/groups/Group\\/1\\\\/sub_groups/Group\\/1\\\\\\/1/**"
        val expectedJson = """
            {
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
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must return an error for an escaped wildcard key in a primitive-field path with 2 trailing wildcards`() {
        val path = "/persons/\\*/first_name/**"

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Intermediate field `first_name` at index 3 in `$path` must be a composition", result.error)
    }

    @Test
    fun `decodePath() must decode an escaped wildcard key in a set-field entity path with 2 trailing wildcards`() {
        val path = "/persons/\\*/**"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": "*"
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Entity>(result, result.toString())
        assertEquals(2, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // endregion

    // region Key field paths

    @Test
    fun `decodePath() must decode a non-wildcard key-field path`() {
        val path = "/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f/id"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": "cc477201-48ec-4367-83a4-7fdbd92f8a6f"
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    @Test
    fun `decodePath() must decode a wildcard key-field path`() {
        val path = "/persons/*/id"
        val expectedJson = """
            {
              "persons": [
                {
                  "id": null
                }
              ]
            }
        """.trimIndent()

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Field>(result, result.toString())
        assertEquals(0, result.trailingWildcards)
        assertMatchesJsonString(model, expectedJson, EncodePasswords.ALL)
    }

    // endregion

    // region Miscellaneous errors

    @Test
    fun `decodePath() must return an error if the path ends with a slash`() {
        val path = "/name/"

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("`$path` must not end with /", result.error)
    }

    @Test
    fun `decodePath() must return an error if the path contains unknown elements`() {
        val path = "/unknown_entity/unknown_field_name"

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Unknown field `unknown_entity` at index 1 in `$path`", result.error)
    }

    @Test
    fun `decodePath() must return an error if a non-composition field is in the middle of a path`() {
        val path = "/name/invalid_field_name"

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Intermediate field `name` at index 1 in `$path` must be a composition", result.error)
    }

    @Test
    fun `decodePath() must return an error if a wildcard is used as a field-name in the middle of a path`() {
        val path = "/*/*"

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Unknown field `*` at index 1 in `$path`", result.error)
    }

    @Test
    fun `decodePath() must return an error if a sub-tree-wildcard is used as a field-name in the middle of a path`() {
        val path = "/**/*"

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Unknown field `**` at index 1 in `$path`", result.error)
    }

    @Test
    fun `decodePath() must return an error if a sub-tree-wildcard is used as a key-value in the middle of a path`() {
        val path = "/persons/**/first_name"

        val model = addressBookRootEntityFactory(null)
        val result = decodePath(path, null, model)

        assertIs<DecodePathResult.Error>(result, result.toString())
        assertEquals("Sub-tree wildcard `**` at index 2 in `$path` is invalid in the middle of a path", result.error)
    }

    // endregion

    // endregion
}