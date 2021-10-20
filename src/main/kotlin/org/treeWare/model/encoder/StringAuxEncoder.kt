package org.treeWare.model.encoder

class StringAuxEncoder : AuxEncoder {
    override fun encode(fieldName: String?, auxName: String, aux: Any?, wireFormatEncoder: WireFormatEncoder) {
        aux?.also {
            val auxFieldName = wireFormatEncoder.getAuxFieldName(fieldName, auxName)
            wireFormatEncoder.encodeStringField(auxFieldName, it.toString())
        }
    }
}
