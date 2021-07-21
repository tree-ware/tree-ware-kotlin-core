package org.treeWare.schema.core

import org.treeWare.schema.codec.encodeDot
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

// TODO(deepak-nulu): make doc generation a gradle task

class SchemaDocs {
    @Test
    fun `Generate AddressBook schema docs`() {
        val schema = newAddressBookSchema()
        val errors = validate(schema)
        assertTrue(errors.isEmpty())

        val fileName = schema.root.name
        val fileWriter = File("${fileName}_schema.dot").bufferedWriter()
        val encoded = encodeDot(schema, fileWriter)
        fileWriter.flush()
        assertTrue(encoded)

        Runtime.getRuntime().exec("dot -Tpng ${fileName}_schema.dot -o ${fileName}_schema.png").waitFor()
    }
}
