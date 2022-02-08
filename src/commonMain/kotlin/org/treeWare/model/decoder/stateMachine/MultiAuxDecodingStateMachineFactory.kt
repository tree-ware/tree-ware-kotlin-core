package org.treeWare.model.decoder.stateMachine

typealias AuxDecodingStateMachineFactory = (stack: DecodingStack) -> AuxDecodingStateMachine?

class MultiAuxDecodingStateMachineFactory(vararg mappings: Pair<String, AuxDecodingStateMachineFactory>) {
    private val mapping = mapOf(*mappings)

    fun newAuxDecodingStateMachine(auxName: String, stack: DecodingStack): AuxDecodingStateMachine? =
        mapping[auxName]?.let { it(stack) }
}
