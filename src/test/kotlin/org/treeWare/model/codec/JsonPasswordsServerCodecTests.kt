package org.treeWare.model.codec

import org.treeWare.model.TestCipherV1
import org.treeWare.model.TestHasherV1
import org.treeWare.model.decoder.stateMachine.StringAuxStateMachine
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.ErrorAuxEncoder
import org.treeWare.model.testRoundTrip
import kotlin.test.Test

class JsonPasswordsServerCodecTests {
    @Test
    fun `Server decoder must create model with hashed and encrypted passwords`() {
        testRoundTrip(
            "model/address_book_passwords_and_secrets.json",
            "model/address_book_passwords_and_secrets_server.json",
            auxEncoder = ErrorAuxEncoder(),
            encodePasswords = EncodePasswords.ALL,
            hasher = TestHasherV1(),
            cipher = TestCipherV1()
        ) { StringAuxStateMachine(it) }
    }
}
