package org.treeWare.model.encoder

class ErrorAuxEncoder : AuxEncoder {
    override val auxType = "error"
    override fun encode(fieldName: String?, aux: Any?, wireFormatEncoder: WireFormatEncoder) {
        aux?.also {
            val auxFieldName = wireFormatEncoder.getAuxFieldName(fieldName, auxType)
            wireFormatEncoder.encodeStringField(auxFieldName, it.toString())
        }
    }
}
