package org.treeWare.model.codec

import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.decoder.stateMachine.StringAuxStateMachine
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.MultiAuxEncoder
import org.treeWare.model.encoder.StringAuxEncoder
import org.treeWare.model.testRoundTrip
import kotlin.test.Test

class JsonPasswordsClientCodecTests {
    @Test
    fun `EncodePasswords NONE must not encode passwords`() {
        val auxName = "error"
        testRoundTrip(
            "model/address_book_passwords_and_secrets.json",
            "model/address_book_passwords_and_secrets_none.json",
            multiAuxEncoder = MultiAuxEncoder(auxName to StringAuxEncoder()),
            encodePasswords = EncodePasswords.NONE,
            multiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory(
                auxName to { StringAuxStateMachine(it) }
            )
        )
    }

    @Test
    fun `EncodePasswords HASHED_AND_ENCRYPTED must encode only hashed and encrypted passwords`() {
        val auxName = "error"
        testRoundTrip(
            "model/address_book_passwords_and_secrets.json",
            "model/address_book_passwords_and_secrets_hashed_and_encrypted.json",
            multiAuxEncoder = MultiAuxEncoder(auxName to StringAuxEncoder()),
            encodePasswords = EncodePasswords.HASHED_AND_ENCRYPTED,
            multiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory(
                auxName to { StringAuxStateMachine(it) }
            )
        )
    }

    @Test
    fun `EncodePasswords ALL must encode all passwords`() {
        val auxName = "error"
        testRoundTrip(
            "model/address_book_passwords_and_secrets.json",
            multiAuxEncoder = MultiAuxEncoder(auxName to StringAuxEncoder()),
            encodePasswords = EncodePasswords.ALL,
            multiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory(
                auxName to { StringAuxStateMachine(it) }
            )
        )
    }
}
