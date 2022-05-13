package org.treeWare.model.decoder

import org.treeWare.model.decoder.stateMachine.DelegatingStateMachine
import java.io.Reader

interface WireFormatDecoder {
    /**
     * @return an error message if there is an error.
     */
    fun decode(reader: Reader, delegatingStateMachine: DelegatingStateMachine): String?
}