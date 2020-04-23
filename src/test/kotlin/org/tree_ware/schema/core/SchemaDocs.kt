package org.tree_ware.schema.core

import org.junit.jupiter.api.Test
import org.tree_ware.schema.codec.encodeDot
import java.io.File
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

        try {
            Runtime.getRuntime().exec("dot -Tpng ${fileName}_schema.dot -o ${fileName}_schema.png").waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
