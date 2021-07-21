package org.treeWare.model.codec.decoding_state_machine

import org.treeWare.common.codec.DecodingStateMachine

interface ValueDecodingStateMachine<Aux> : DecodingStateMachine {
    fun setAux(aux: Aux)
}
