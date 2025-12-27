package org.treeWare.model.encoder

import okio.Sink
import org.treeWare.model.core.ElementModel
import org.treeWare.model.encoder.MultiAuxEncoder
import org.treeWare.model.encoder.EncodePasswords

fun encodePaths(
    element: ElementModel,
    sink: Sink,
    multiAuxEncoder: MultiAuxEncoder = MultiAuxEncoder(),
    encodePasswords: EncodePasswords = EncodePasswords.NONE,
    prettyPrint: Boolean = false,
    indentSizeInSpaces: Int = 2
): Boolean {
    return false
}

fun encodePaths(
    elements: List<ElementModel>,
    sink: Sink,
    multiAuxEncoder: MultiAuxEncoder = MultiAuxEncoder(),
    encodePasswords: EncodePasswords = EncodePasswords.NONE,
    prettyPrint: Boolean = false,
    indentSizeInSpaces: Int = 2
): Boolean {
    return false
}
