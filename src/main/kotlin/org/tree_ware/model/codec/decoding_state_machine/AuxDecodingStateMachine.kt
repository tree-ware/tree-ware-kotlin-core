package org.tree_ware.model.codec.decoding_state_machine

import org.tree_ware.common.codec.DecodingStateMachine

interface AuxDecodingStateMachine<Aux> : DecodingStateMachine {
    fun newAux()
    fun getAux(): Aux?
}
