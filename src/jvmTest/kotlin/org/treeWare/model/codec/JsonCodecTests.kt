package org.treeWare.model.codec

import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.decoder.stateMachine.StringAuxStateMachine
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.MultiAuxEncoder
import org.treeWare.model.encoder.StringAuxEncoder
import org.treeWare.model.testRoundTrip
import kotlin.test.Test

class JsonCodecTests {
    @Test
    fun `JSON codec data round trip must be lossless`() {
        testRoundTrip("model/address_book_1.json", encodePasswords = EncodePasswords.ALL)
    }

    @Test
    fun `JSON codec error-model round trip must be lossless`() {
        val auxName = "error"
        testRoundTrip(
            "model/address_book_error_all_model.json",
            multiAuxEncoder = MultiAuxEncoder(auxName to StringAuxEncoder()),
            encodePasswords = EncodePasswords.ALL,
            multiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory(
                auxName to { StringAuxStateMachine(it) }
            )
        )
    }

    @Test
    fun `JSON codec multiple-aux round trip must be lossless`() {
        val auxType1 = "aux1"
        val auxType2 = "aux2"
        testRoundTrip(
            "model/address_book_multi_aux.json",
            multiAuxEncoder = MultiAuxEncoder(
                auxType1 to StringAuxEncoder(),
                auxType2 to StringAuxEncoder()
            ),
            encodePasswords = EncodePasswords.ALL,
            multiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory(
                auxType1 to { StringAuxStateMachine(it) },
                auxType2 to { StringAuxStateMachine(it) }
            )
        )
    }

    @Test
    fun `JSON codec person filter-branch round trip must be lossless`() {
        testRoundTrip("model/address_book_filter_person_model.json")
    }

    @Test
    fun `JSON codec settings filter-branch round trip must be lossless`() {
        testRoundTrip("model/address_book_filter_settings_model.json")
    }

    @Test
    fun `JSON codec filter-all round trip must be lossless`() {
        testRoundTrip("model/address_book_filter_all_model.json")
    }
}
