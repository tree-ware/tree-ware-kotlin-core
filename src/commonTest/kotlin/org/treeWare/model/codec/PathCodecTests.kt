package org.treeWare.model.codec

import okio.Buffer
import org.treeWare.metaModel.getResolvedRootMeta
import org.treeWare.metaModel.newAddressBookMetaModel
import org.treeWare.model.core.ElementModel
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.decoder.ModelDecoderOptions
import org.treeWare.model.decodeJsonFileIntoEntity
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.MultiAuxEncoder
import org.treeWare.model.encoder.encodePaths
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PathCodecTests {
    @Test
    fun path_codec_data_encoding_must_generate_paths() {
        val inputFilePath = "model/address_book_1.json"
        val expectedFilePath = "C:\\Users\\tamil\\tree-ware-kotlin-core\\address_book_1.txt"
        val entityMeta = newAddressBookMetaModel(null, null).metaModel?.let { getResolvedRootMeta(it) }
            ?: throw IllegalStateException("Meta-model has validation errors")
        val entity = MutableEntityModel(entityMeta, null)

        // Decode the JSON file into the entity
        decodeJsonFileIntoEntity(
            inputFilePath,
            ModelDecoderOptions(),
            emptyList(),
            entity = entity
        )

        // Encode to paths
        val actualPathsString = getEncodedPathsString(entity, EncodePasswords.ALL)

        // Read the expected output
        val expectedPathsString = File(expectedFilePath).readText()

        // Compare actual vs expected (trim to avoid trailing newline differences)
        assertEquals(
            expectedPathsString.trimEnd(),
            actualPathsString.trimEnd(),
            "Encoded paths should match expected output"
        )

        // Also verify basic structural invariants
        assertTrue(actualPathsString.isNotEmpty(), "Encoded paths should not be empty")

        // Verify no empty lines (except possibly a trailing newline)
        val lines = actualPathsString.lines()
        val nonTrailingLines = if (lines.lastOrNull()?.isEmpty() == true) {
            lines.dropLast(1)
        } else {
            lines
        }
        val emptyLineCount = nonTrailingLines.count { it.trim().isEmpty() }
        assertEquals(0, emptyLineCount, "There should be no empty lines in the output (except trailing newline)")
    }
}

/** Encode the model element to paths and return as string. */
fun getEncodedPathsString(
    element: ElementModel,
    encodePasswords: EncodePasswords,
    multiAuxEncoder: MultiAuxEncoder = MultiAuxEncoder()
): String {
    val buffer = Buffer()
    val isEncoded = try {
        encodePaths(element, buffer, multiAuxEncoder, encodePasswords)
    } catch (e: Throwable) {
        e.printStackTrace()
        println("Encoded so far:")
        println(buffer.readUtf8())
        println("End of encoded")
        false
    }
    assertTrue(isEncoded)
    return buffer.readUtf8()
}