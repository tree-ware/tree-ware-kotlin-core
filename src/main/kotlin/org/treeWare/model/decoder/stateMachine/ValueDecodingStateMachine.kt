package org.treeWare.model.decoder.stateMachine

interface ValueDecodingStateMachine<Aux> : DecodingStateMachine {
    fun setAux(aux: Aux)
}
