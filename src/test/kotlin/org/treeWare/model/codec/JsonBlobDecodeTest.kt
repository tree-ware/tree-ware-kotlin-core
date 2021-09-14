package org.treeWare.model.codec

import org.treeWare.metaModel.newAddressBookMetaModel
import org.treeWare.metaModel.validation.validate
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.MutableListFieldModel
import org.treeWare.model.core.MutablePrimitiveModel
import org.treeWare.model.core.MutableSingleFieldModel
import org.treeWare.model.getMainModel
import kotlin.test.Test
import kotlin.test.assertTrue

class JsonBlobDecodeTest {
    @Test
    fun `JSON decoder decodes blob values`() {
        val metaModel = newAddressBookMetaModel()
        val metaModelErrors = validate(metaModel)
        assertTrue(metaModelErrors.isEmpty())

        val model = getMainModel<Unit>(metaModel, "model/address_book_1.json", "data", null, null) { null }
        val persons = model.root.fields["person"] as MutableListFieldModel<Unit>

        val clarkKent = persons.values[0] as MutableEntityModel<Unit>
        val clarkKentPictureField = clarkKent.fields["picture"] as MutableSingleFieldModel<Unit>
        val clarkKentPicturePrimitive = clarkKentPictureField.value as MutablePrimitiveModel<Unit>
        val clarkKentPicture = clarkKentPicturePrimitive.value as ByteArray
        assertTrue("Picture of Clark Kent".toByteArray().contentEquals(clarkKentPicture))

        val loisLane = persons.values[1] as MutableEntityModel<Unit>
        val loisLanePictureField = loisLane.fields["picture"] as MutableSingleFieldModel<Unit>
        val loisLanePicturePrimitive = loisLanePictureField.value as MutablePrimitiveModel<Unit>
        val loisLanePicture = loisLanePicturePrimitive.value as ByteArray
        assertTrue("Picture of Lois Lane".toByteArray().contentEquals(loisLanePicture))
    }
}
