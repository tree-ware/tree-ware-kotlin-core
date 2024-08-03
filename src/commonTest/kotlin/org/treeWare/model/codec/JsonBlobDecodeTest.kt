package org.treeWare.model.codec

import org.treeWare.metaModel.addressBookMetaModel
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.MutablePrimitiveModel
import org.treeWare.model.core.MutableSetFieldModel
import org.treeWare.model.core.MutableSingleFieldModel
import org.treeWare.model.getMainModelFromJsonFile
import kotlin.test.Test
import kotlin.test.assertTrue

class JsonBlobDecodeTest {
    @Test
    fun `JSON decoder decodes blob values`() {
        val model = getMainModelFromJsonFile(addressBookMetaModel, "model/address_book_1_main_model.json")
        val persons = model.root?.fields?.get("person") as MutableSetFieldModel

        val clarkKent = persons.values.first() as MutableEntityModel
        val clarkKentPictureField = clarkKent.fields["picture"] as MutableSingleFieldModel
        val clarkKentPicturePrimitive = clarkKentPictureField.value as MutablePrimitiveModel
        val clarkKentPicture = clarkKentPicturePrimitive.value as ByteArray
        assertTrue("Picture of Clark Kent".toByteArray().contentEquals(clarkKentPicture))

        val loisLane = persons.values.last() as MutableEntityModel
        val loisLanePictureField = loisLane.fields["picture"] as MutableSingleFieldModel
        val loisLanePicturePrimitive = loisLanePictureField.value as MutablePrimitiveModel
        val loisLanePicture = loisLanePicturePrimitive.value as ByteArray
        assertTrue("Picture of Lois Lane".toByteArray().contentEquals(loisLanePicture))
    }
}