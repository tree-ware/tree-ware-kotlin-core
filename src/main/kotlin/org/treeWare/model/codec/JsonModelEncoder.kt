package org.treeWare.model.codec

import org.treeWare.common.codec.JsonWireFormatEncoder
import org.treeWare.common.traversal.TraversalAction
import org.treeWare.model.codec.encoder.AuxEncoder
import org.treeWare.model.codec.encoder.ModelEncodingVisitor
import org.treeWare.model.core.ElementModel
import org.treeWare.model.operator.forEach
import java.io.Writer

fun <Aux> encodeJson(
    element: ElementModel<Aux>,
    auxEncoder: AuxEncoder?,
    writer: Writer,
    prettyPrint: Boolean = false,
    indentSizeInSpaces: Int = 2
): Boolean {
    val wireFormatEncoder = JsonWireFormatEncoder(writer, prettyPrint, indentSizeInSpaces)
    val encodingVisitor = ModelEncodingVisitor<Aux>(wireFormatEncoder, auxEncoder)
    return forEach(element, encodingVisitor) != TraversalAction.ABORT_TREE
}
