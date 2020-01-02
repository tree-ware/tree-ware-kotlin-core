package org.tree_ware.core.codec.json

import org.tree_ware.core.codec.common.SchemaEncoder
import org.tree_ware.core.codec.common.SchemaEncodingVisitor
import org.tree_ware.core.schema.ElementSchema
import java.io.Writer

class JsonSchemaEncoder(private val writer: Writer,
                        prettyPrint: Boolean = false,
                        indentSizeInSpaces: Int = 2
) : SchemaEncoder {
    private val wireFormatEncoder = JsonWireFormatEncoder(writer, prettyPrint, indentSizeInSpaces)
    private val encodingVisitor = SchemaEncodingVisitor(wireFormatEncoder)

    // SchemaEncoder methods

    override fun encode(element: ElementSchema): Boolean {
        wireFormatEncoder.encodeObjectStart(null)
        val completed = element.accept(encodingVisitor)
        wireFormatEncoder.encodeObjectEnd()
        return completed
    }

    override fun encode(elements: Collection<ElementSchema>): Boolean {
        wireFormatEncoder.encodeListStart(null)
        val completed = elements.all { encode(it) }
        wireFormatEncoder.encodeListEnd()
        return completed
    }
}
