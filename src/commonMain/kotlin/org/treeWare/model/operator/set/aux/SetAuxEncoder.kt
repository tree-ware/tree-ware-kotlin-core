package org.treeWare.model.operator.set.aux

import org.treeWare.model.encoder.AuxEncoder
import org.treeWare.model.encoder.WireFormatEncoder

class SetAuxEncoder : AuxEncoder {
    override fun encode(fieldName: String?, auxName: String, aux: Any?, wireFormatEncoder: WireFormatEncoder) {
        aux?.also {
            val auxFieldName = wireFormatEncoder.getAuxFieldName(fieldName, auxName)
            val setAux = aux as SetAux
            wireFormatEncoder.encodeStringField(auxFieldName, setAux.name.lowercase())
        }
    }
}