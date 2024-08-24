package org.treeWare.model.codec

import org.treeWare.model.TestCipherV1
import org.treeWare.model.TestHasherV1
import org.treeWare.model.decoder.stateMachine.MultiAuxDecodingStateMachineFactory
import org.treeWare.model.decoder.stateMachine.StringAuxStateMachine
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.MultiAuxEncoder
import org.treeWare.model.encoder.StringAuxEncoder
import org.treeWare.model.testRoundTrip
import kotlin.test.Test

class JsonPasswordsServerCodecTests {
    @Test
    fun `Server decoder must create model with hashed and encrypted passwords`() {
        val auxName = "error"
        testRoundTrip(
            "model/address_book_passwords_and_secrets.json",
            "model/address_book_passwords_and_secrets_server.json",
            multiAuxEncoder = MultiAuxEncoder(auxName to StringAuxEncoder()),
            encodePasswords = EncodePasswords.ALL,
            hasher = TestHasherV1(),
            cipher = TestCipherV1(),
            multiAuxDecodingStateMachineFactory = MultiAuxDecodingStateMachineFactory(
                auxName to { StringAuxStateMachine(it) }
            )
        )
    }
}
