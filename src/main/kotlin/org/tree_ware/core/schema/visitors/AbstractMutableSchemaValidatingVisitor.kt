package org.tree_ware.core.schema.visitors

abstract class AbstractMutableSchemaValidatingVisitor : AbstractMutableSchemaVisitor() {
    val errors: List<String> get() = _errors
    protected val _errors = mutableListOf<String>()
}
