package org.tree_ware.model

import org.tree_ware.model.codec.decodeJson
import org.tree_ware.model.core.MutableModel
import org.tree_ware.schema.core.newAddressBookSchema
import org.tree_ware.schema.core.validate
import java.io.File
import java.io.FileReader
import kotlin.test.assertTrue

internal fun getModel(inputFilePath: String): MutableModel<out Any> {
    val schema = newAddressBookSchema()
    val errors = validate(schema)
    assertTrue(errors.isEmpty())

    val inputFile = File(inputFilePath)
    assertTrue(inputFile.exists())

    val jsonReader = FileReader(inputFile)
    val model = decodeJson(jsonReader, schema)
    jsonReader.close()
    assertTrue(model != null)
    return model
}