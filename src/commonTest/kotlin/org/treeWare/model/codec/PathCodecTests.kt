package org.treeWare.model.codec

import okio.Buffer
import org.treeWare.metaModel.getResolvedRootMeta
import org.treeWare.metaModel.newAddressBookMetaModel
import org.treeWare.model.core.ElementModel
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.decoder.ModelDecoderOptions
import org.treeWare.model.decodeJsonFileIntoEntity
import org.treeWare.model.encoder.EncodePasswords
import org.treeWare.model.encoder.MultiAuxEncoder
import org.treeWare.model.encoder.encodePaths
import org.treeWare.util.readFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PathCodecTests {
    @Test
    fun path_codec_data_encoding_must_generate_paths() {
        val inputFilePath = "model/address_book_1.json"
        val expectedFilePath = "model/codec/address_book_1.txt"
        val entityMeta = newAddressBookMetaModel(null, null).metaModel?.let { getResolvedRootMeta(it) }
            ?: throw IllegalStateException("Meta-model has validation errors")
        val entity: MutableEntityModel = MutableEntityModel(entityMeta, null)

        // Decode the JSON file into the entity
        decodeJsonFileIntoEntity(
            inputFilePath,
            ModelDecoderOptions(),
            emptyList(),
            entity = entity
        )

        // Encode to paths
        val actualPathsString = getEncodedPathsString(entity, EncodePasswords.ALL)

        // Verify it's not empty and contains expected path patterns
        assertTrue(actualPathsString.isNotEmpty(), "Encoded paths should not be empty")
        assertTrue(actualPathsString.contains("/name = \"Super Heroes\""), "Should contain root name field")
        assertTrue(actualPathsString.contains("/last_updated = 1587147731"), "Should contain last_updated field")
        assertTrue(actualPathsString.contains("/settings/last_name_first = true"), "Should contain settings fields")
        assertTrue(actualPathsString.contains("/groups/DC/name = \"DC\""), "Should contain group paths")
        assertTrue(actualPathsString.contains("/persons/cc477201-48ec-4367-83a4-7fdbd92f8a6f"), "Should contain person entity paths")

        // Verify no empty lines (except possibly a trailing newline)
        val lines = actualPathsString.lines()
        // Filter out the last line if it's empty (trailing newline is acceptable)
        val nonTrailingLines = if (lines.lastOrNull()?.trim()?.isEmpty() == true) {
            lines.dropLast(1)
        } else {
            lines
        }
        val emptyLineCount = nonTrailingLines.count { it.trim().isEmpty() }
        assertEquals(0, emptyLineCount, "There should be no empty lines in the output (except trailing newline)")

        println("Encoded paths output:")
        println(actualPathsString)
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
