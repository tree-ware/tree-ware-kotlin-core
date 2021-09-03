package org.treeWare.model.codec.decoder.stateMachine

interface AuxDecodingStateMachine<Aux> : DecodingStateMachine {
    fun newAux()
    fun getAux(): Aux?
}
