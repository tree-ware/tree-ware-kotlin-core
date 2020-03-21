package org.tree_ware.core.schema.visitors

import org.tree_ware.core.schema.*

// NOTE: validation is also done in other subclasses of AbstractMutableSchemaValidatingVisitor

class ValidationVisitor : AbstractMutableSchemaValidatingVisitor() {
    override fun mutableVisit(pkg: MutablePackageSchema): Boolean {
        pkg.root?.also {
            if (this.root == null) this.root = pkg.root
            else _errors.add("Invalid additional root: ${it.fullName}")
        }
        return true
    }

    override fun mutableVisit(enumeration: MutableEnumerationSchema): Boolean {
        if (enumeration.values.isEmpty()) {
            _errors.add("No enumeration values: ${enumeration.fullName}")
        }
        return true
    }

    override fun mutableVisit(field: MutableFieldSchema): Boolean {
        if (!isValidMultiplicity(field.multiplicity)) {
            _errors.add("Invalid multiplicity: ${field.fullName}")
        }
        return true
    }

    override fun finalizeValidation() {
        if (root == null) {
            _errors.add("No root entity")
        }
    }

    private var root: MutableCompositionFieldSchema? = null
}

fun isValidMultiplicity(multiplicity: Multiplicity): Boolean {
    if (multiplicity.min < 0) return false
    if (multiplicity.max < 0) return false
    if (multiplicity.max < multiplicity.min) return false
    return true
}