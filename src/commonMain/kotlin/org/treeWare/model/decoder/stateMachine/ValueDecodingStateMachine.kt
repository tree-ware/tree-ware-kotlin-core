package org.treeWare.model.decoder.stateMachine

interface ValueDecodingStateMachine : DecodingStateMachine {
    fun setAux(auxName: String, aux: Any?)
}
