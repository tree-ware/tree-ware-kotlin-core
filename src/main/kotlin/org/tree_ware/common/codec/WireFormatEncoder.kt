package org.tree_ware.common.codec

interface WireFormatEncoder {
    fun encodeObjectStart(name: String?)
    fun encodeObjectEnd()
    fun encodeListStart(name: String?)
    fun encodeListEnd()
    fun encodeStringField(name: String, value: String)
    fun <T : Number> encodeNumericField(name: String, value: T)
    fun encodeBooleanField(name: String, value: Boolean)
}
