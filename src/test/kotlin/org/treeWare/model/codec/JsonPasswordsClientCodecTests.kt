package org.treeWare.model.codec

import org.treeWare.model.decoder.stateMachine.StringAuxStateMachine
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.ErrorAuxEncoder
import org.treeWare.model.testRoundTrip
import kotlin.test.Test

class JsonPasswordsClientCodecTests {
    @Test
    fun `EncodePasswords NONE must not encode passwords`() {
        testRoundTrip(
            "model/address_book_passwords_and_secrets.json",
            "model/address_book_passwords_and_secrets_none.json",
            auxEncoder = ErrorAuxEncoder(),
            encodePasswords = EncodePasswords.NONE
        ) { StringAuxStateMachine(it) }
    }

    @Test
    fun `EncodePasswords HASHED_AND_ENCRYPTED must encode only hashed and encrypted passwords`() {
        testRoundTrip(
            "model/address_book_passwords_and_secrets.json",
            "model/address_book_passwords_and_secrets_hashed_and_encrypted.json",
            auxEncoder = ErrorAuxEncoder(),
            encodePasswords = EncodePasswords.HASHED_AND_ENCRYPTED
        ) { StringAuxStateMachine(it) }
    }

    @Test
    fun `EncodePasswords ALL must encode all passwords`() {
        testRoundTrip(
            "model/address_book_passwords_and_secrets.json",
            auxEncoder = ErrorAuxEncoder(),
            encodePasswords = EncodePasswords.ALL
        ) { StringAuxStateMachine(it) }
    }
}
