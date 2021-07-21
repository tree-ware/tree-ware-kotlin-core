package org.treeWare.schema.codec

import org.treeWare.common.codec.JsonWireFormatEncoder
import org.treeWare.schema.core.ElementSchema
import org.treeWare.common.traversal.TraversalAction
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
    val traversalAction = element.traverse(encodingVisitor)
    wireFormatEncoder.encodeObjectEnd()

    return traversalAction != TraversalAction.ABORT_TREE
}
