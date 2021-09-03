package org.treeWare.model.decoder.stateMachine

interface AuxDecodingStateMachine<Aux> : DecodingStateMachine {
    fun newAux()
    fun getAux(): Aux?
}
