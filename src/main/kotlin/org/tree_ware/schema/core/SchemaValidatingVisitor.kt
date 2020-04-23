package org.tree_ware.schema.core

interface SchemaValidatingVisitor {
    val errors: List<String>

    fun finalizeValidation()
}
