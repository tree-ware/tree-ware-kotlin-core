package org.treeWare.model.codec.decoder.stateMachine

import org.treeWare.common.codec.DecodingStateMachine

interface AuxDecodingStateMachine<Aux> : DecodingStateMachine {
    fun newAux()
    fun getAux(): Aux?
}
