package org.treeWare.schema.core

interface SchemaValidatingVisitor {
    val errors: List<String>

    fun finalizeValidation()
}
