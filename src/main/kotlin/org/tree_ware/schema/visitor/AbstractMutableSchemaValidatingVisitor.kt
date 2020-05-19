package org.tree_ware.schema.visitor

import org.tree_ware.schema.core.SchemaTraversalAction
import org.tree_ware.schema.core.SchemaValidatingVisitor

abstract class AbstractMutableSchemaValidatingVisitor
    : SchemaValidatingVisitor, AbstractMutableSchemaVisitor<SchemaTraversalAction>(SchemaTraversalAction.CONTINUE) {
    override val errors: List<String> get() = _errors
    protected val _errors = mutableListOf<String>()

    override fun finalizeValidation() {}
}
