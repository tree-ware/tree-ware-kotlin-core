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
    fun json_codec_data_round_trip_must_be_lossless() {
        testRoundTrip("model/address_book_1.json", encodePasswords = EncodePasswords.ALL)
    }

    @Test
    fun json_codec_error_model_round_trip_must_be_lossless() {
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
    fun json_codec_multiple_aux_round_trip_must_be_lossless() {
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
    fun json_codec_empty_root_round_trip_must_be_lossless() {
        testRoundTrip("model/address_book_empty_root.json")
    }

    @Test
    fun json_codec_empty_composition_round_trip_must_be_lossless() {
        testRoundTrip("model/address_book_empty_composition.json")
    }

    @Test
    fun json_codec_empty_list_round_trip_must_be_lossless() {
        testRoundTrip("model/address_book_empty_list.json")
    }
}