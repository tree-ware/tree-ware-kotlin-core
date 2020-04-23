package org.tree_ware.schema.codec

import org.tree_ware.common.codec.JsonWireFormatEncoder
import org.tree_ware.schema.codec.SchemaEncoder
import org.tree_ware.schema.codec.SchemaEncodingVisitor
import org.tree_ware.schema.core.ElementSchema
import java.io.Writer

class JsonSchemaEncoder(private val writer: Writer,
                        prettyPrint: Boolean = false,
                        indentSizeInSpaces: Int = 2
) : SchemaEncoder {
    private val wireFormatEncoder =
        JsonWireFormatEncoder(writer, prettyPrint, indentSizeInSpaces)
    private val encodingVisitor = SchemaEncodingVisitor(wireFormatEncoder)

    // SchemaEncoder methods

    override fun encode(element: ElementSchema): Boolean {
        wireFormatEncoder.encodeObjectStart(null)
        val completed = element.accept(encodingVisitor)
        wireFormatEncoder.encodeObjectEnd()
        return completed
    }
}
