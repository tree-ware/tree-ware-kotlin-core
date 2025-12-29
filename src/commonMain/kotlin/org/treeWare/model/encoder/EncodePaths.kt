package org.treeWare.model.encoder

import okio.Sink
import org.treeWare.model.core.ElementModel
import org.treeWare.model.encoder.MultiAuxEncoder
import org.treeWare.model.encoder.EncodePasswords

fun encodePaths(
    element: ElementModel,
    sink: Sink,
    multiAuxEncoder: MultiAuxEncoder = MultiAuxEncoder(),
    encodePasswords: EncodePasswords = EncodePasswords.NONE

): Boolean {
    return false
}

