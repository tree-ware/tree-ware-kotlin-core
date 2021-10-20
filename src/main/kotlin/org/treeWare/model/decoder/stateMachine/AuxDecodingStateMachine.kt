package org.treeWare.model.decoder.stateMachine

interface AuxDecodingStateMachine : DecodingStateMachine {
    val auxType: String
    fun newAux()
    fun getAux(): Any?
}
