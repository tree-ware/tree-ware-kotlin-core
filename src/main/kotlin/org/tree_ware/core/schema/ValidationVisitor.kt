package org.tree_ware.core.schema

// NOTE: validation is also done in other subclasses of AbstractMutableSchemaValidatingVisitor

class ValidationVisitor : AbstractMutableSchemaValidatingVisitor() {
    override fun mutableVisit(field: MutableFieldSchema): Boolean {
        if (!isValidMultiplicity(field.multiplicity)) {
            _errors.add("Invalid multiplicity: ${field.fullName}")
        }
        return true
    }
}

fun isValidMultiplicity(multiplicity: Multiplicity): Boolean {
    if (multiplicity.min < 0) return false
    if (multiplicity.max < 0) return false
    if (multiplicity.max < multiplicity.min) return false
    return true
}