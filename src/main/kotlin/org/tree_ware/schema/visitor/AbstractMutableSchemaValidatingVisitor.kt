package org.tree_ware.schema.visitor

import org.tree_ware.common.traversal.TraversalAction
import org.tree_ware.schema.core.SchemaValidatingVisitor

abstract class AbstractMutableSchemaValidatingVisitor
    : SchemaValidatingVisitor, AbstractMutableSchemaVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    override val errors: List<String> get() = _errors
    protected val _errors = mutableListOf<String>()

    override fun finalizeValidation() {}
}
