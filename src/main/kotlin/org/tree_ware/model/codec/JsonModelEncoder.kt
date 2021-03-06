package org.tree_ware.model.codec

import org.tree_ware.common.codec.JsonWireFormatEncoder
import org.tree_ware.model.codec.aux_encoder.AuxEncoder
import org.tree_ware.model.core.ElementModel
import org.tree_ware.model.operator.forEach
import org.tree_ware.schema.core.SchemaTraversalAction
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
    return forEach(element, encodingVisitor) != SchemaTraversalAction.ABORT_TREE
}
