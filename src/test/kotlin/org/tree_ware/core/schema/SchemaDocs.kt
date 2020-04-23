package org.tree_ware.core.schema

import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertTrue

// TODO(deepak-nulu): make doc generation a gradle task

class SchemaDocs {
    @Test
    fun `Generate AddressBook schema docs`() {
        val schemaManager = SchemaManager()
        val errors = schemaManager.addPackages(listOf(getAddressBookPackage()))
        assertTrue(errors.isEmpty())

        val fileName = schemaManager.root.name
        val fileWriter = File("${fileName}_schema.dot").bufferedWriter()
        schemaManager.encodeDot(fileWriter)
        fileWriter.flush()

        try {
            Runtime.getRuntime().exec("dot -Tpng ${fileName}_schema.dot -o ${fileName}_schema.png").waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
