package org.treeWare.model.codec

import org.treeWare.model.decoder.ModelDecoderOptions
import org.treeWare.model.decoder.OnDuplicateKeys
import org.treeWare.model.testRoundTrip
import kotlin.test.Test

class DecoderDuplicateKeysTests {
    @Test
    fun `OnDuplicateKeys SKIP_WITH_ERRORS must skip and report errors for all entities with duplicate keys`() {
        val expectedDecodeErrors = listOf(
            "Entity with duplicate keys: /org.tree_ware.test.address_book.main/address_book_relation: [05ade278-4b44-43da-a0cc-14463854e397]",
            "Entity with duplicate keys: /org.tree_ware.test.address_book.main/address_book_person: [cc477201-48ec-4367-83a4-7fdbd92f8a6f]",
            "Entity with duplicate keys: /org.tree_ware.test.address_book.city/address_book_city_info: [New York City, New York, United States of America]"
        )
        testRoundTrip(
            "model/address_book_duplicate_keys.json",
            "model/address_book_duplicate_keys_skipped.json",
            options = ModelDecoderOptions(onDuplicateKeys = OnDuplicateKeys.SKIP_WITH_ERRORS),
            expectedDecodeErrors = expectedDecodeErrors
        )
    }

    @Test
    fun `OnDuplicateKeys OVERWRITE must overwrite existing entities`() {
        val expectedDecodeErrors = listOf("")
        testRoundTrip(
            "model/address_book_duplicate_keys.json",
            "model/address_book_duplicate_keys_overwritten.json",
            options = ModelDecoderOptions(onDuplicateKeys = OnDuplicateKeys.OVERWRITE),
            expectedDecodeErrors = expectedDecodeErrors
        )
    }
}
