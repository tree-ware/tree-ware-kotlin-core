package org.treeWare.model.codec

import okio.Buffer
import org.treeWare.model.decoder.decodeJsonToModel
import org.treeWare.model.encoder.encodePaths
import org.treeWare.model.test.readResource
import kotlin.test.Test
import kotlin.test.assertEquals

class PathCodecTests {

    @Test
    fun path_codec_data_round_trip_must_be_lossless() {
        // 1. Read JSON input (same file used in JsonCodecTests)
        val json = readResource("model/address_book_1.json")

        // 2. Deserialize JSON -> Tree-Ware model
        val model = decodeJsonToModel(json)

        // 3. Encode paths
        val sink = Buffer()
        encodePaths(model, sink)

        // 4. Read actual paths output
        val actualPaths = sink.readUtf8()

        // 5. Read expected paths file (already committed earlier)
        val expectedPaths = readResource("model/address_book_1.txt")

        // 6. Compare (EXPECTED TO FAIL until encodePaths is implemented)
        assertEquals(expectedPaths, actualPaths)
    }
}
