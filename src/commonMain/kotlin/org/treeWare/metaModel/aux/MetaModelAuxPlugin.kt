package org.treeWare.metaModel.aux

import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.decoder.stateMachine.AuxDecodingStateMachineFactory
import org.treeWare.model.encoder.AuxEncoder

interface MetaModelAuxPlugin {
    val auxName: String
    val auxDecodingStateMachineFactory: AuxDecodingStateMachineFactory
    val auxEncoder: AuxEncoder?

    /** Validates the specified meta-model and returns a list of errors. */
    fun validate(mainMeta: MutableMainModel): List<String>
}