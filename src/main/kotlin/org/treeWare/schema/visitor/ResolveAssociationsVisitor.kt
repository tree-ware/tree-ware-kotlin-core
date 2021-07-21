package org.treeWare.schema.visitor

import org.treeWare.schema.core.MutableAssociationFieldSchema
import org.treeWare.schema.core.MutableRootSchema
import org.treeWare.common.traversal.TraversalAction
import org.treeWare.schema.core.validate

class ResolveAssociationsVisitor(private val root: MutableRootSchema?) : AbstractMutableSchemaValidatingVisitor() {
    override fun mutableVisit(associationField: MutableAssociationFieldSchema): TraversalAction {
        // Set resolvedEntity
        if (associationField.entityPath.size < 2) {
            _errors.add("Association path is too short: ${associationField.fullName}")
            return TraversalAction.CONTINUE
        }
        root?.also {
            _errors.addAll(validate(associationField.entityPathSchema, root, associationField.fullName))
            if (associationField.multiplicity.isList() && associationField.keyEntities.isEmpty()) _errors.add(
                "Association list entity path does not have keys: ${associationField.fullName}"
            )
        }
        return TraversalAction.CONTINUE
    }
}
