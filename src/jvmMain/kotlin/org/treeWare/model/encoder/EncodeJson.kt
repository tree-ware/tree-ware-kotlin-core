package org.treeWare.model.encoder

import org.treeWare.model.core.ElementModel
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach
import java.io.Writer

fun encodeJson(
    element: ElementModel,
    writer: Writer,
    multiAuxEncoder: MultiAuxEncoder = MultiAuxEncoder(),
    encodePasswords: EncodePasswords = EncodePasswords.NONE,
    prettyPrint: Boolean = false,
    indentSizeInSpaces: Int = 2
): Boolean {
    val wireFormatEncoder = JsonWireFormatEncoder(writer, prettyPrint, indentSizeInSpaces)
    val encodingVisitor = ModelEncodingVisitor(wireFormatEncoder, multiAuxEncoder, encodePasswords)
    return forEach(element, encodingVisitor, true) != TraversalAction.ABORT_TREE
}