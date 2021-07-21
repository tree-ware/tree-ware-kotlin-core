package org.treeWare.model.codec.aux_encoder

import org.treeWare.common.codec.WireFormatEncoder

interface AuxEncoder {
    fun encode(aux: Any?, wireFormatEncoder: WireFormatEncoder)
}
