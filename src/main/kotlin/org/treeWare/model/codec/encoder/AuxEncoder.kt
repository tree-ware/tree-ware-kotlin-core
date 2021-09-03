package org.treeWare.model.codec.encoder

interface AuxEncoder {
    fun encode(fieldName: String?, aux: Any?, wireFormatEncoder: WireFormatEncoder)
}
