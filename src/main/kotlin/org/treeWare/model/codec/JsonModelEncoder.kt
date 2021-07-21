package org.treeWare.model.codec

import org.treeWare.common.codec.JsonWireFormatEncoder
import org.treeWare.model.codec.aux_encoder.AuxEncoder
import org.treeWare.model.core.ElementModel
import org.treeWare.model.operator.forEach
import org.treeWare.common.traversal.TraversalAction
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
