package org.treeWare.model.encoder

interface AuxEncoder {
    fun encode(fieldName: String?, auxName: String, aux: Any?, wireFormatEncoder: WireFormatEncoder)
}
