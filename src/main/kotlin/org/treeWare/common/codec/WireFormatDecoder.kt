package org.treeWare.common.codec

import java.io.Reader

interface WireFormatDecoder {
    fun decode(reader: Reader, decodingStateMachine: DecodingStateMachine): Boolean
}
