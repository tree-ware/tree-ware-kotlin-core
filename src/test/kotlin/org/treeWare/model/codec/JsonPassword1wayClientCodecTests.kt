package org.treeWare.model.codec

import org.treeWare.model.testRoundTrip
import kotlin.test.Test

class JsonPassword1wayClientCodecTests {
    @Test
    fun `EncodePasswords ALL must encode all password1way values`() {
        testRoundTrip<Unit>("model/address_book_passwords_and_secrets.json")
    }
}
