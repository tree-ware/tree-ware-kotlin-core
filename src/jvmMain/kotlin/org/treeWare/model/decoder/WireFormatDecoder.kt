package org.treeWare.model.decoder

import org.treeWare.model.decoder.stateMachine.DecodingStateMachine
import java.io.Reader

interface WireFormatDecoder {
    /**
     * @return an error message if there is an error.
     */
    fun decode(reader: Reader, decodingStateMachine: DecodingStateMachine): String?
}