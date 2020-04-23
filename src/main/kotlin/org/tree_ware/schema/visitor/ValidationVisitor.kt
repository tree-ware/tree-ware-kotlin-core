package org.tree_ware.schema.visitor

import org.tree_ware.schema.core.MutableEnumerationSchema
import org.tree_ware.schema.core.MutableFieldSchema
import org.tree_ware.schema.core.MutablePackageSchema
import org.tree_ware.schema.core.MutableRootSchema

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
        val multiplicity = field.multiplicity

        if (field.isKey && !multiplicity.isRequired()) _errors.add(
            "Multiplicity is not [1, 1] for key field: ${field.fullName}"
        )
        if (multiplicity.min < 0) _errors.add("Multiplicity min is less than 0: ${field.fullName}")
        if (multiplicity.max < 0) _errors.add("Multiplicity max is less than 0: ${field.fullName}")
        if (multiplicity.max > 0 && multiplicity.max < multiplicity.min) _errors.add(
            "Multiplicity max is less than min: ${field.fullName}"
        )

        return true
    }

    override fun finalizeValidation() {
        if (root == null) {
            _errors.add("No root entity")
        }
    }

    var root: MutableRootSchema? = null
        private set
}
