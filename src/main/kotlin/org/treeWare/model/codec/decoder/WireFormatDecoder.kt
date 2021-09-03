package org.treeWare.model.codec.decoder

import org.treeWare.model.codec.decoder.stateMachine.DecodingStateMachine
import java.io.Reader

interface WireFormatDecoder {
    fun decode(reader: Reader, decodingStateMachine: DecodingStateMachine): Boolean
}
