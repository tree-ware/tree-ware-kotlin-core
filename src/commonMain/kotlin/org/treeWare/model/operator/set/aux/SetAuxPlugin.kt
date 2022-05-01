package org.treeWare.model.operator.set.aux

import org.treeWare.metaModel.aux.MetaModelAuxPlugin
import org.treeWare.model.core.MutableMainModel
import org.treeWare.model.decoder.stateMachine.AuxDecodingStateMachineFactory
import org.treeWare.model.encoder.AuxEncoder

class SetAuxPlugin : MetaModelAuxPlugin {
    override val auxName: String = SET_AUX_NAME
    override val auxDecodingStateMachineFactory: AuxDecodingStateMachineFactory = { SetAuxStateMachine(it) }
    override val auxEncoder: AuxEncoder = SetAuxEncoder()

    override fun validate(mainMeta: MutableMainModel): List<String> = emptyList()
}