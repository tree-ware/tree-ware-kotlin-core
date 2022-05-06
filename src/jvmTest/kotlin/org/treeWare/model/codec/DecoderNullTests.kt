package org.treeWare.model.codec

import org.treeWare.metaModel.newAddressBookMetaModel
import org.treeWare.model.getMainModelFromJsonString
import org.treeWare.model.testRoundTrip
import kotlin.test.Test

private val metaModel = newAddressBookMetaModel(null, null).metaModel
    ?: throw IllegalStateException("Meta-model has validation errors")

class DecoderNullTests {
    @Test
    fun `Decoding must fail for null root`() {
        val modelJson = """
            |{
            |  "address_book": null
            |}
        """.trimMargin()
        val expectedDecodeErrors = listOf("Root entities must not be null; use empty object {} instead")
        getMainModelFromJsonString(metaModel, modelJson, expectedDecodeErrors = expectedDecodeErrors)
    }

    @Test
    fun `Decoding must fail for null objects`() {
        val modelJson = """
            |{
            |  "address_book": {
            |    "settings": null
            |  }
            |}
        """.trimMargin()
        val expectedDecodeErrors = listOf("Entities must not be null; use empty object {} instead")
        getMainModelFromJsonString(metaModel, modelJson, expectedDecodeErrors = expectedDecodeErrors)
    }

    @Test
    fun `Decoding must fail for null composition-keys`() {
        val modelJson = """
            |{
            |  "address_book": {
            |    "city_info": [
            |      {
            |        "city": null
            |      }
            |    ]
            |  }
            |}
        """.trimMargin()
        val expectedDecodeErrors = listOf("Entities must not be null; use empty object {} instead")
        getMainModelFromJsonString(metaModel, modelJson, expectedDecodeErrors = expectedDecodeErrors)
    }

    @Test
    fun `Decoding must fail for null primitive-lists`() {
        val modelJson = """
            |{
            |  "address_book": {
            |    "settings": {
            |      "card_colors": null
            |    }
            |  }
            |}
        """.trimMargin()
        val expectedDecodeErrors = listOf("Lists must not be null; use empty array [] instead")
        getMainModelFromJsonString(metaModel, modelJson, expectedDecodeErrors = expectedDecodeErrors)
    }

    @Test
    fun `Decoding must fail for null composition-lists`() {
        val modelJson = """
            |{
            |  "address_book": {
            |    "person": null
            |  }
            |}
        """.trimMargin()
        val expectedDecodeErrors = listOf("Lists must not be null; use empty array [] instead")
        getMainModelFromJsonString(metaModel, modelJson, expectedDecodeErrors = expectedDecodeErrors)
    }

    @Test
    fun `Decoding must succeed for null single fields`() {
        testRoundTrip("model/address_book_null_fields.json", metaModel = metaModel)
    }
}