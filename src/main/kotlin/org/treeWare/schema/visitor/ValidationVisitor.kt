package org.treeWare.schema.visitor

import org.treeWare.schema.core.MutableEnumerationSchema
import org.treeWare.schema.core.MutableFieldSchema
import org.treeWare.schema.core.MutableSchema
import org.treeWare.common.traversal.TraversalAction

// NOTE: validation is also done in other subclasses of AbstractMutableSchemaValidatingVisitor

class ValidationVisitor : AbstractMutableSchemaValidatingVisitor() {
    override fun mutableVisit(schema: MutableSchema): TraversalAction {
        if (schema._root == null) _errors.add("No root entity")
        return TraversalAction.CONTINUE
    }

    override fun mutableVisit(enumeration: MutableEnumerationSchema): TraversalAction {
        if (enumeration.values.isEmpty()) {
            _errors.add("No enumeration values: ${enumeration.fullName}")
        }
        return TraversalAction.CONTINUE
    }

    override fun mutableVisit(field: MutableFieldSchema): TraversalAction {
        val multiplicity = field.multiplicity

        if (field.isKey && !multiplicity.isRequired()) _errors.add(
            "Multiplicity is not [1, 1] for key field: ${field.fullName}"
        )
        if (multiplicity.min < 0) _errors.add("Multiplicity min is less than 0: ${field.fullName}")
        if (multiplicity.max < 0) _errors.add("Multiplicity max is less than 0: ${field.fullName}")
        if (multiplicity.max > 0 && multiplicity.max < multiplicity.min) _errors.add(
            "Multiplicity max is less than min: ${field.fullName}"
        )

        return TraversalAction.CONTINUE
    }
}
