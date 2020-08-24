package org.tree_ware.model.action

import org.tree_ware.model.core.*
import org.tree_ware.model.visitor.AbstractModelVisitor

// IMPLEMENTATION: ./Get.md

class CompositionTableFieldNameVisitor : AbstractModelVisitor<Unit, List<String>>(listOf()) {
    // Scalar fields

    override fun visit(field: PrimitiveFieldModel<Unit>): List<String> {
        return listOf(field.schema.name)
    }

    override fun visit(field: AliasFieldModel<Unit>): List<String> {
        return listOf(field.schema.name)
    }

    override fun visit(field: EnumerationFieldModel<Unit>): List<String> {
        return listOf(field.schema.name)
    }

    override fun visit(field: AssociationFieldModel<Unit>): List<String> {
        return listOf(field.schema.name)
    }

    override fun visit(field: CompositionFieldModel<Unit>): List<String> {
        // Recurse into the composition (but only fields that are not composition-lists).
        val nested = field.value.fields.filter { it !is CompositionListFieldModel<*> }.flatMap { it.dispatch(this) }
        return nested.map { "${field.schema.name}/${it}" }
    }

    // List fields

    override fun visit(field: PrimitiveListFieldModel<Unit>): List<String> {
        return listOf(field.schema.name)
    }

    override fun visit(field: AliasListFieldModel<Unit>): List<String> {
        return listOf(field.schema.name)
    }

    override fun visit(field: EnumerationListFieldModel<Unit>): List<String> {
        return listOf(field.schema.name)
    }

    override fun visit(field: AssociationListFieldModel<Unit>): List<String> {
        return listOf(field.schema.name)
    }

    override fun visit(field: CompositionListFieldModel<Unit>): List<String> {
        // Composition list fields are not flattened, and so their field names
        // are not returned.
        return listOf()
    }
}
