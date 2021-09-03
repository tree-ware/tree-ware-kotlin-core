package org.treeWare.model.codec.decoder.stateMachine

interface ValueDecodingStateMachine<Aux> : DecodingStateMachine {
    fun setAux(aux: Aux)
}
