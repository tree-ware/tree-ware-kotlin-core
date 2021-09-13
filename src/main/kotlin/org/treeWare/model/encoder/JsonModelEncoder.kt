package org.treeWare.model.encoder

import org.treeWare.model.core.ElementModel
import org.treeWare.model.operator.TraversalAction
import org.treeWare.model.operator.forEach
import java.io.Writer

fun <Aux> encodeJson(
    element: ElementModel<Aux>,
    auxEncoder: AuxEncoder?,
    writer: Writer,
    encodePasswords: EncodePasswords = EncodePasswords.NONE,
    prettyPrint: Boolean = false,
    indentSizeInSpaces: Int = 2
): Boolean {
    val wireFormatEncoder = JsonWireFormatEncoder(writer, prettyPrint, indentSizeInSpaces)
    val encodingVisitor = ModelEncodingVisitor<Aux>(wireFormatEncoder, auxEncoder)
    return forEach(element, encodingVisitor) != TraversalAction.ABORT_TREE
}
