package org.treeWare.model.codec

import org.treeWare.metaModel.addressBookRootEntityFactory
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.MutablePrimitiveModel
import org.treeWare.model.core.MutableSetFieldModel
import org.treeWare.model.core.MutableSingleFieldModel
import org.treeWare.model.decodeJsonFileIntoEntity
import kotlin.test.Test
import kotlin.test.assertTrue

class JsonBlobDecodeTest {
    @Test
    fun `JSON decoder decodes blob values`() {
        val addressBook = addressBookRootEntityFactory(null)
        decodeJsonFileIntoEntity("model/address_book_1.json", entity = addressBook)
        val persons = addressBook.fields["persons"] as MutableSetFieldModel

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