package org.treeWare.metaModel.aux

import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.decoder.stateMachine.AuxDecodingStateMachineFactory
import org.treeWare.model.encoder.AuxEncoder

// TODO(cleanup): rename to ModelAuxPlugin and move to model/core directory
interface MetaModelAuxPlugin {
    val auxName: String
    val auxDecodingStateMachineFactory: AuxDecodingStateMachineFactory
    val auxEncoder: AuxEncoder?

    // TODO(deepak-nulu): is this useful only for meta-models or for models as well?
    // TODO(deepak-nulu): if this is useful for models, might need to compose multiple in a single model traversal.
    /** Validates the specified meta-model and returns a list of errors. */
    fun validate(metaModel: MutableEntityModel): List<String>
}