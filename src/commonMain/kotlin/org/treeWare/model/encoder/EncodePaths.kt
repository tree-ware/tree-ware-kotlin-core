package org.treeWare.model.encoder

import okio.Sink
import org.treeWare.model.core.ElementModel
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.forEach
import org.treeWare.util.buffered

fun encodePaths(
    element: ElementModel,
    sink: Sink,
    multiAuxEncoder: MultiAuxEncoder = MultiAuxEncoder(),
    encodePasswords: EncodePasswords = EncodePasswords.NONE
): Boolean = sink.buffered().use { bufferedSink ->
    val encodingVisitor = PathEncodingVisitor(bufferedSink)
    forEach(element, encodingVisitor, true) != TraversalAction.ABORT_TREE
}
