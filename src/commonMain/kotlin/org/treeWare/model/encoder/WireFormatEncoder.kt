package org.treeWare.model.encoder

interface WireFormatEncoder {
    fun getAuxFieldName(fieldName: String?, auxName: String): String

    fun encodeObjectStart(name: String?)
    fun encodeObjectEnd()
    fun encodeListStart(name: String?)
    fun encodeListEnd()
    fun encodeNullField(name: String)
    fun encodeStringField(name: String, value: String)
    fun <T> encodeNumericField(name: String, value: T)
    fun encodeBooleanField(name: String, value: Boolean)
}
