package org.treeWare.model.codec.decoding_state_machine

import org.treeWare.common.codec.DecodingStateMachine

interface AuxDecodingStateMachine<Aux> : DecodingStateMachine {
    fun newAux()
    fun getAux(): Aux?
}
