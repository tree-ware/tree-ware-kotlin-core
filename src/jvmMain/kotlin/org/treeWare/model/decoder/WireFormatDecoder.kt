package org.treeWare.model.decoder

import org.treeWare.model.decoder.stateMachine.DecodingStateMachine
import java.io.Reader

interface WireFormatDecoder {
    fun decode(reader: Reader, decodingStateMachine: DecodingStateMachine): Boolean
}
