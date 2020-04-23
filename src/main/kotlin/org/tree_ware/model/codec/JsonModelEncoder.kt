package org.tree_ware.model.codec

import org.tree_ware.common.codec.JsonWireFormatEncoder
import org.tree_ware.model.core.ElementModel
import java.io.Writer

fun encode(
    element: ElementModel,
    writer: Writer,
    prettyPrint: Boolean = false,
    indentSizeInSpaces: Int = 2
): Boolean {
    val wireFormatEncoder =
        JsonWireFormatEncoder(writer, prettyPrint, indentSizeInSpaces)
    val encodingVisitor = ModelEncodingVisitor(wireFormatEncoder)
    return element.accept(encodingVisitor)
}
