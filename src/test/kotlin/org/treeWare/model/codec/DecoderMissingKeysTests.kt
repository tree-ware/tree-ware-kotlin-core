package org.treeWare.model.codec

import org.treeWare.metaModel.newAddressBookMetaModel
import org.treeWare.model.decoder.ModelDecoderOptions
import org.treeWare.model.decoder.OnMissingKeys
import org.treeWare.model.decoder.decodeJson
import org.treeWare.model.getFileReader
import org.treeWare.model.testRoundTrip
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DecoderMissingKeysTests {
    @Test
    fun `OnMissingKeys SKIP_WITH_ERRORS must skip and report errors for all missing keys`() {
        val expectedDecodeErrors = listOf(
            "Missing key fields: [id]",
            "Missing key fields: [id]",
            "Missing key fields: [city]",
            "Missing key fields: [name, state, country]",
            "Missing key fields: [state, country]"
        )
        testRoundTrip<Unit>(
            "model/address_book_missing_keys.json",
            "model/address_book_missing_keys_skipped.json",
            options = ModelDecoderOptions(onMissingKeys = OnMissingKeys.SKIP_WITH_ERRORS),
            expectedDecodeErrors = expectedDecodeErrors
        )
    }

    @Test
    fun `OnMissingKeys ABORT_WITH_ERROR must abort and report an error when keys are missing`() {
        val fileReader = getFileReader("model/address_book_missing_keys.json")
        val metaModel = newAddressBookMetaModel(null, null)
        val (mainModel, decodeErrors) = decodeJson<Unit>(
            fileReader,
            metaModel,
            "data",
            ModelDecoderOptions(onMissingKeys = OnMissingKeys.ABORT_WITH_ERROR)
        ) { null }
        fileReader.close()

        val expectedDecodeErrors = listOf("Missing key fields: [id]")
        assertNull(mainModel)
        assertEquals(expectedDecodeErrors.joinToString("\n"), decodeErrors.joinToString("\n"))
    }
}
