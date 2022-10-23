package org.treeWare.model.encoder

import okio.Sink
import org.treeWare.model.core.ElementModel
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach
import org.treeWare.util.buffered

fun encodeJson(
    element: ElementModel,
    sink: Sink,
    multiAuxEncoder: MultiAuxEncoder = MultiAuxEncoder(),
    encodePasswords: EncodePasswords = EncodePasswords.NONE,
    prettyPrint: Boolean = false,
    indentSizeInSpaces: Int = 2
): Boolean = sink.buffered().use { bufferedSink ->
    val wireFormatEncoder = JsonWireFormatEncoder(bufferedSink, prettyPrint, indentSizeInSpaces)
    val encodingVisitor = ModelEncodingVisitor(wireFormatEncoder, multiAuxEncoder, encodePasswords)
    forEach(element, encodingVisitor, true) != TraversalAction.ABORT_TREE
}

fun encodeJson(
    elements: List<ElementModel>,
    sink: Sink,
    multiAuxEncoder: MultiAuxEncoder = MultiAuxEncoder(),
    encodePasswords: EncodePasswords = EncodePasswords.NONE,
    prettyPrint: Boolean = false,
    indentSizeInSpaces: Int = 2
): Boolean = sink.buffered().use { bufferedSink ->
    val wireFormatEncoder = JsonWireFormatEncoder(bufferedSink, prettyPrint, indentSizeInSpaces)
    val encodingVisitor = ModelEncodingVisitor(wireFormatEncoder, multiAuxEncoder, encodePasswords)
    wireFormatEncoder.encodeListStart(null)
    elements.forEach { element ->
        val action = forEach(element, encodingVisitor, true)
        if (action == TraversalAction.ABORT_TREE) return false
    }
    wireFormatEncoder.encodeListEnd()
    true
}