package org.tree_ware.model.codec.aux_encoder

import org.tree_ware.common.codec.WireFormatEncoder

const val ERROR_KEY = "error"

class ErrorAuxEncoder : AuxEncoder {
    override fun encode(aux: Any?, wireFormatEncoder: WireFormatEncoder) {
        aux?.also { wireFormatEncoder.encodeStringField(ERROR_KEY, it.toString()) }
    }
}
