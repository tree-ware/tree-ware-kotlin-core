package org.treeWare.model.codec

import okio.buffer
import org.treeWare.metaModel.addressBookMetaModel
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.decoder.ModelDecoderOptions
import org.treeWare.model.decoder.OnMissingKeys
import org.treeWare.model.decoder.decodeJson
import org.treeWare.model.testRoundTrip
import org.treeWare.util.getFileSource
import kotlin.test.Test
import kotlin.test.assertEquals

class DecoderMissingKeysTests {
    @Test
    fun `OnMissingKeys SKIP_WITH_ERRORS must skip and report errors for all missing keys`() {
        val expectedDecodeErrors = listOf(
            "Missing key fields [id] in instance of /address_book.main/address_book_relation",
            "Missing key fields [id] in instance of /address_book.main/address_book_person",
            "Missing key fields [city] in instance of /address_book.city/address_book_city_info",
            "Missing key fields [name, state, country] in instance of /address_book.city/address_book_city",
            "Missing key fields [state, country] in instance of /address_book.city/address_book_city",
        )
        testRoundTrip(
            "model/address_book_missing_keys.json",
            "model/address_book_missing_keys_skipped.json",
            options = ModelDecoderOptions(onMissingKeys = OnMissingKeys.SKIP_WITH_ERRORS),
            expectedDecodeErrors = expectedDecodeErrors
        )
    }

    @Test
    fun `OnMissingKeys ABORT_WITH_ERROR must abort and report an error when keys are missing`() {
        val addressBook = MutableMainModel(addressBookMetaModel)
        val decodeErrors = getFileSource("model/address_book_missing_keys.json").use {
            decodeJson(
                it.buffer(),
                addressBook,
                ModelDecoderOptions(onMissingKeys = OnMissingKeys.ABORT_WITH_ERROR)
            )
        }

        val expectedDecodeErrors =
            listOf("Missing key fields [id] in instance of /address_book.main/address_book_relation")
        assertEquals(expectedDecodeErrors.joinToString("\n"), decodeErrors.joinToString("\n"))
    }
}
