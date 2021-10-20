package org.treeWare.model.decoder.stateMachine

interface ValueDecodingStateMachine : DecodingStateMachine {
    fun setAux(auxType: String, aux: Any?)
}
