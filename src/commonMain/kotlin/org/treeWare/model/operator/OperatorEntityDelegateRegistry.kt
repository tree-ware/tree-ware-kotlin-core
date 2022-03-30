package org.treeWare.model.operator

typealias EntityDelegateRegistry<Delegate> = Map<String, Delegate>

class OperatorEntityDelegateRegistry {
    fun <Delegate> add(entityFullName: String, operatorId: OperatorId<Delegate>, delegate: Delegate) {
        val delegateRegistry =
            operatorRegistry.getOrPut(operatorId) { HashMap<String, Delegate>() } as HashMap<String, Delegate>
        delegateRegistry[entityFullName] = delegate
    }

    fun <Delegate> get(operatorId: OperatorId<Delegate>): EntityDelegateRegistry<Delegate>? =
        operatorRegistry[operatorId] as? EntityDelegateRegistry<Delegate>

    private val operatorRegistry = HashMap<Any, Map<String, *>>()
}