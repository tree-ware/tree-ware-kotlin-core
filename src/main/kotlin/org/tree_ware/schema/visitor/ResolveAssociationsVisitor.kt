package org.tree_ware.schema.visitor

import org.tree_ware.schema.core.MutableAssociationFieldSchema
import org.tree_ware.schema.core.MutableRootSchema
import org.tree_ware.schema.core.SchemaTraversalAction
import org.tree_ware.schema.core.validate

class ResolveAssociationsVisitor(private val root: MutableRootSchema?) : AbstractMutableSchemaValidatingVisitor() {
    override fun mutableVisit(associationField: MutableAssociationFieldSchema): SchemaTraversalAction {
        // Set resolvedEntity
        if (associationField.entityPath.size < 2) {
            _errors.add("Association path is too short: ${associationField.fullName}")
            return SchemaTraversalAction.CONTINUE
        }
        root?.also {
            _errors.addAll(validate(associationField.entityPathSchema, root, associationField.fullName))
            if (associationField.multiplicity.isList() && associationField.keyEntities.isEmpty()) _errors.add(
                "Association list entity path does not have keys: ${associationField.fullName}"
            )
        }
        return SchemaTraversalAction.CONTINUE
    }
}
