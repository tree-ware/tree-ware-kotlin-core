package org.treeWare.model.codec

import okio.Buffer
import org.treeWare.model.encoder.encodePaths
import kotlin.test.Test
import kotlin.test.assertFalse

class PathCodecTests {

    @Test
    fun path_codec_encode_returns_false_until_implemented() {
        val sink = Buffer()

        val result = encodePaths(
            emptyList(),
            sink
        )

        // TDD: encodePaths is not implemented yet
        assertFalse(result)
    }
}
