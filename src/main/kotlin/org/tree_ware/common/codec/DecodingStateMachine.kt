package org.tree_ware.common.codec

import java.math.BigDecimal

interface DecodingStateMachine {
    fun decodeObjectStart(): Boolean
    fun decodeObjectEnd(): Boolean
    fun decodeListStart(): Boolean
    fun decodeListEnd(): Boolean
    fun decodeKey(name: String): Boolean
    fun decodeStringValue(value: String): Boolean
    fun decodeNumericValue(value: BigDecimal): Boolean
    fun decodeBooleanValue(value: Boolean): Boolean
}
