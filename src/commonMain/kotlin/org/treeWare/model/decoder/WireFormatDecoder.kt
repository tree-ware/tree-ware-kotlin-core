package org.treeWare.model.decoder

import okio.BufferedSource
import org.treeWare.model.decoder.stateMachine.DelegatingStateMachine

interface WireFormatDecoder {
    /**
     * @return an error message if there is an error.
     */
    fun decode(bufferedSource: BufferedSource, delegatingStateMachine: DelegatingStateMachine): String?
}