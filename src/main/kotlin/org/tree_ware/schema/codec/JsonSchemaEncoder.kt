package org.tree_ware.schema.codec

import org.tree_ware.common.codec.JsonWireFormatEncoder
import org.tree_ware.schema.core.ElementSchema
import java.io.Writer

fun encodeJson(
    element: ElementSchema,
    writer: Writer,
    prettyPrint: Boolean = false,
    indentSizeInSpaces: Int = 2
): Boolean {
    val wireFormatEncoder = JsonWireFormatEncoder(writer, prettyPrint, indentSizeInSpaces)
    val encodingVisitor = SchemaEncodingVisitor(wireFormatEncoder)

    wireFormatEncoder.encodeObjectStart(null)
    val completed = element.accept(encodingVisitor)
    wireFormatEncoder.encodeObjectEnd()
    return completed
}
