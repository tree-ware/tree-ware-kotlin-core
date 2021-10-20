package org.treeWare.model.encoder

interface AuxEncoder {
    val auxType: String
    fun encode(fieldName: String?, aux: Any?, wireFormatEncoder: WireFormatEncoder)
}
