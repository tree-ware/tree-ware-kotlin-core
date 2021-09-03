package org.treeWare.model.codec.encoder

private const val AUX_KEY = "error"

class ErrorAuxEncoder : AuxEncoder {
    override fun encode(fieldName: String?, aux: Any?, wireFormatEncoder: WireFormatEncoder) {
        aux?.also {
            val auxFieldName = wireFormatEncoder.getAuxFieldName(fieldName, AUX_KEY)
            wireFormatEncoder.encodeStringField(auxFieldName, it.toString())
        }
    }
}
