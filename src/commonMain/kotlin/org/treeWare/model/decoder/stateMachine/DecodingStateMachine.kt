package org.treeWare.model.decoder.stateMachine

interface DecodingStateMachine {
    fun decodeObjectStart(): Boolean
    fun decodeObjectEnd(): Boolean
    fun decodeListStart(): Boolean
    fun decodeListEnd(): Boolean
    fun decodeKey(name: String): Boolean
    fun decodeNullValue(): Boolean
    fun decodeStringValue(value: String): Boolean
    fun decodeNumericValue(value: String): Boolean
    fun decodeBooleanValue(value: Boolean): Boolean
}
