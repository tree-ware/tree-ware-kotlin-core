package org.treeWare.model.codec

import org.treeWare.metaModel.newAddressBookMetaModel
import org.treeWare.model.assertMatchesJsonString
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.decoder.decodePaths
import org.treeWare.model.encoder.EncodePasswords
import java.io.StringReader
import kotlin.test.Test
import kotlin.test.assertEquals

class DecodePathsTests {
    @Test
    fun `DecodePath must decode paths into a mutable model`() {
        val paths = """
            |/address_book/name = Super Heroes
            |/address_book/last_updated = 1587147731
            |/address_book/settings/last_name_first = true
            |/address_book/settings/encrypt_hero_name = false
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
            |      "encrypt_hero_name": false
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

        val mainModel = MutableMainModel(newAddressBookMetaModel(null, null).metaModel)
        val errors = decodePaths(StringReader(paths), mainModel)

        assertEquals("", errors.joinToString("\n"))
        assertMatchesJsonString(mainModel, expectedJson, EncodePasswords.ALL)
    }
}