package org.treeWare.model.decoder.stateMachine

interface AuxDecodingStateMachine : DecodingStateMachine {
    fun newAux()
    fun getAux(): Any?
}
