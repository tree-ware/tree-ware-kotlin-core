package org.tree_ware.common.codec

import java.io.Reader

interface WireFormatDecoder {
    fun decode(reader: Reader, decodingStateMachine: DecodingStateMachine): Boolean
}
