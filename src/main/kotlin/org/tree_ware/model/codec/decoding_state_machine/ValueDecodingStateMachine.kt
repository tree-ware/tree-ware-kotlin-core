package org.tree_ware.model.codec.decoding_state_machine

import org.tree_ware.common.codec.DecodingStateMachine

interface ValueDecodingStateMachine<Aux> : DecodingStateMachine {
    fun setAux(aux: Aux)
}
