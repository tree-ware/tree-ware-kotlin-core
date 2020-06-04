package org.tree_ware.model.codec.aux_encoder

import org.tree_ware.common.codec.WireFormatEncoder

interface AuxEncoder {
    fun encode(aux: Any?, wireFormatEncoder: WireFormatEncoder)
}
