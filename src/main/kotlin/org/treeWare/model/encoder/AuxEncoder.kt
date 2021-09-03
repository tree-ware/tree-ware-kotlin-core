package org.treeWare.model.encoder

interface AuxEncoder {
    fun encode(fieldName: String?, aux: Any?, wireFormatEncoder: WireFormatEncoder)
}
