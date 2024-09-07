package org.treeWare.model.codec

import org.treeWare.model.AddressBookMutableEntityModelFactory
import org.treeWare.model.decodeJsonStringIntoEntity
import org.treeWare.model.testRoundTrip
import kotlin.test.Test

class DecoderNullTests {

    @Test
    fun `Decoding must fail for null objects`() {
        val modelJson = """
            |{
            |  "settings": null
            |}
        """.trimMargin()
        val expectedDecodeErrors = listOf("Entities must not be null; use empty object {} instead")
        val model = AddressBookMutableEntityModelFactory.create()
        decodeJsonStringIntoEntity(modelJson, expectedDecodeErrors = expectedDecodeErrors, entity = model)
    }

    @Test
    fun `Decoding must fail for null composition-keys`() {
        val modelJson = """
            |{
            |  "city_info": [
            |    {
            |      "city": null
            |    }
            |  ]
            |}
        """.trimMargin()
        val expectedDecodeErrors = listOf("Entities must not be null; use empty object {} instead")
        val model = AddressBookMutableEntityModelFactory.create()
        decodeJsonStringIntoEntity(modelJson, expectedDecodeErrors = expectedDecodeErrors, entity = model)
    }

    @Test
    fun `Decoding must fail for null composition-lists`() {
        val modelJson = """
            |{
            |  "person": null
            |}
        """.trimMargin()
        val expectedDecodeErrors = listOf("Lists must not be null; use empty array [] instead")
        val model = AddressBookMutableEntityModelFactory.create()
        decodeJsonStringIntoEntity(modelJson, expectedDecodeErrors = expectedDecodeErrors, entity = model)
    }

    @Test
    fun `Decoding must succeed for null single fields`() {
        val model = AddressBookMutableEntityModelFactory.create()
        testRoundTrip("model/address_book_null_fields.json", entity = model)
    }
}