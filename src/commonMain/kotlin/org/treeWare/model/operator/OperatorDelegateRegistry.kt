package org.treeWare.model.operator

typealias DelegateRegistry<Delegate> = Map<String, Delegate>

class OperatorDelegateRegistry {
    fun <Delegate> add(entityFullName: String, operatorId: OperatorId<Delegate>, delegate: Delegate) {
        val delegateRegistry =
            operatorRegistry.getOrPut(operatorId) { HashMap<String, Delegate>() } as HashMap<String, Delegate>
        delegateRegistry[entityFullName] = delegate
    }

    fun <Delegate> get(operatorId: OperatorId<Delegate>): DelegateRegistry<Delegate>? =
        operatorRegistry[operatorId] as? DelegateRegistry<Delegate>

    private val operatorRegistry = HashMap<Any, Map<String, *>>()
}