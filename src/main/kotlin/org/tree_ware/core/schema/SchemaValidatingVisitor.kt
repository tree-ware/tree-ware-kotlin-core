package org.tree_ware.core.schema

interface SchemaValidatingVisitor {
    val errors: List<String>

    fun finalizeValidation()
}
