package org.tree_ware.schema.visitor

import org.tree_ware.schema.core.MutableEnumerationSchema
import org.tree_ware.schema.core.MutableFieldSchema
import org.tree_ware.schema.core.MutableSchema
import org.tree_ware.schema.core.SchemaTraversalAction

// NOTE: validation is also done in other subclasses of AbstractMutableSchemaValidatingVisitor

class ValidationVisitor : AbstractMutableSchemaValidatingVisitor() {
    override fun mutableVisit(schema: MutableSchema): SchemaTraversalAction {
        if (schema._root == null) _errors.add("No root entity")
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableVisit(enumeration: MutableEnumerationSchema): SchemaTraversalAction {
        if (enumeration.values.isEmpty()) {
            _errors.add("No enumeration values: ${enumeration.fullName}")
        }
        return SchemaTraversalAction.CONTINUE
    }

    override fun mutableVisit(field: MutableFieldSchema): SchemaTraversalAction {
        val multiplicity = field.multiplicity

        if (field.isKey && !multiplicity.isRequired()) _errors.add(
            "Multiplicity is not [1, 1] for key field: ${field.fullName}"
        )
        if (multiplicity.min < 0) _errors.add("Multiplicity min is less than 0: ${field.fullName}")
        if (multiplicity.max < 0) _errors.add("Multiplicity max is less than 0: ${field.fullName}")
        if (multiplicity.max > 0 && multiplicity.max < multiplicity.min) _errors.add(
            "Multiplicity max is less than min: ${field.fullName}"
        )

        return SchemaTraversalAction.CONTINUE
    }
}
