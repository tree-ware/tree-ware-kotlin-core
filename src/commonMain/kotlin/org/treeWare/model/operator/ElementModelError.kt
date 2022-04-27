package org.treeWare.model.operator

data class ElementModelError(val path: String, val error: String) {
    override fun toString(): String = "$path: $error"
}