package org.treeWare.schema.visitor

import org.treeWare.common.traversal.TraversalAction
import org.treeWare.schema.core.SchemaValidatingVisitor

abstract class AbstractMutableSchemaValidatingVisitor
    : SchemaValidatingVisitor, AbstractMutableSchemaVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    override val errors: List<String> get() = _errors
    protected val _errors = mutableListOf<String>()

    override fun finalizeValidation() {}
}
