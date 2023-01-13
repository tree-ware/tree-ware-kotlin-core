package org.treeWare.model.codec

import org.treeWare.model.assertMatchesJson
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.MultiAuxEncoder
import org.treeWare.model.encoder.StringAuxEncoder
import org.treeWare.model.newAddressBook
import kotlin.test.Test

private const val AUX_NAME = "aux"

class JsonEncoderTests {
    @Test
    fun `JSON encoding must be correct`() {
        val model = newAddressBook(AUX_NAME)
        assertMatchesJson(
            model,
            "model/address_book_encoder_test.json",
            EncodePasswords.ALL,
            MultiAuxEncoder(AUX_NAME to StringAuxEncoder())
        )
    }
}