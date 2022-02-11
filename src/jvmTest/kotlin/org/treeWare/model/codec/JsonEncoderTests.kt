package org.treeWare.model.codec

import org.treeWare.metaModel.newAddressBookMetaModel
import org.treeWare.model.assertMatchesJson
import org.treeWare.model.core.*
import org.treeWare.model.encoder.EncodePasswords
import kotlin.test.Test

class JsonEncoderTests {
    @Test
    fun `JSON encoding must be correct`() {
        val model = newAddressBook1()
        assertMatchesJson(model, "model/address_book_encoder_test_1.json", EncodePasswords.ALL)
    }

}

private fun newAddressBook1(): MainModel {
    val addressBookMetaModel = newAddressBookMetaModel(null, null).metaModel
        ?: throw IllegalStateException("Meta-model has validation errors")
    val main = MutableMainModel(addressBookMetaModel)
    val root = main.getOrNewRoot()
    setStringSingleField(root, "name", "Encoder Test 1")
    setTimestampSingleField(root, "last_updated", 1587147731L)

    val settings = getOrNewMutableSingleEntity(root, "settings")
    setBooleanSingleField(settings, "last_name_first", true)
    setBooleanSingleField(settings, "encrypt_hero_name", false)
    setEnumerationListField(settings, "card_colors", "orange", "green", "blue")

    val persons = getOrNewMutableSetField(root, "person")
    val clark = getNewMutableSetEntity(persons)
    setUuidSingleField(clark, "id", "cc477201-48ec-4367-83a4-7fdbd92f8a6f")
    setStringSingleField(clark, "first_name", "Clark")
    setStringSingleField(clark, "last_name", "Kent")
    setStringSingleField(clark, "hero_name", "Superman")
    persons.addValue(clark)

    val lois = getNewMutableSetEntity(persons)
    setUuidSingleField(lois, "id", "a8aacf55-7810-4b43-afe5-4344f25435fd")
    setStringSingleField(lois, "first_name", "Lois")
    setStringSingleField(lois, "last_name", "Lane")
    persons.addValue(lois)

    return main
}