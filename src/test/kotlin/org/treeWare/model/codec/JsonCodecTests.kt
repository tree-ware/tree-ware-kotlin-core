package org.treeWare.model.codec

import org.treeWare.model.decoder.stateMachine.StringAuxStateMachine
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.ErrorAuxEncoder
import org.treeWare.model.testRoundTrip
import kotlin.test.Test

class JsonCodecTests {
    @Test
    fun `JSON codec data round trip must be lossless`() {
        testRoundTrip<Unit>("model/address_book_1.json", encodePasswords = EncodePasswords.ALL)
    }

    @Test
    fun `JSON codec error-model round trip must be lossless`() {
        testRoundTrip(
            "model/address_book_error_all_model.json",
            auxEncoder = ErrorAuxEncoder(),
            expectedModelType = "error"
        ) { StringAuxStateMachine(it) }
    }

    @Test
    fun `JSON codec person filter-branch round trip must be lossless`() {
        testRoundTrip<Unit>("model/address_book_filter_person_model.json")
    }

    @Test
    fun `JSON codec settings filter-branch round trip must be lossless`() {
        testRoundTrip<Unit>("model/address_book_filter_settings_model.json")
    }

    @Test
    fun `JSON codec filter-all round trip must be lossless`() {
        testRoundTrip<Unit>("model/address_book_filter_all_model.json")
    }
}
