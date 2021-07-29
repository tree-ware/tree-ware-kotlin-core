package org.treeWare.model.codec.decoder.stateMachine

import org.treeWare.common.codec.DecodingStateMachine

interface ValueDecodingStateMachine<Aux> : DecodingStateMachine {
    fun setAux(aux: Aux)
}
