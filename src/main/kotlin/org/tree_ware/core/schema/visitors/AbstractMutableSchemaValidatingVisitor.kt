package org.tree_ware.core.schema.visitors

import org.tree_ware.core.schema.SchemaValidatingVisitor

abstract class AbstractMutableSchemaValidatingVisitor : SchemaValidatingVisitor, AbstractMutableSchemaVisitor() {
    override val errors: List<String> get() = _errors
    protected val _errors = mutableListOf<String>()

    override fun finalizeValidation() {}
}
