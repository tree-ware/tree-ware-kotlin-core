package org.treeWare.model.encoder

class MultiAuxEncoder(vararg mappings: Pair<String, AuxEncoder>) {
    private val mapping = mapOf(*mappings)

    fun encode(fieldName: String?, auxName: String, aux: Any?, wireFormatEncoder: WireFormatEncoder) {
        mapping[auxName]?.also { it.encode(fieldName, auxName, aux, wireFormatEncoder) }
    }
}
