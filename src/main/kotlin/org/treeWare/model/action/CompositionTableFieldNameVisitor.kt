package org.treeWare.model.action

import org.treeWare.model.core.*
import org.treeWare.model.operator.AbstractLeader1Follower0ModelVisitor
import org.treeWare.model.operator.dispatchVisit

// IMPLEMENTATION: ./Get.md

class CompositionTableFieldNameVisitor : AbstractLeader1Follower0ModelVisitor<Unit, List<String>>(listOf()) {
    // Scalar fields

    override fun visit(leaderField1: PrimitiveFieldModel<Unit>): List<String> {
        return listOf(leaderField1.schema.name)
    }

    override fun visit(leaderField1: AliasFieldModel<Unit>): List<String> {
        return listOf(leaderField1.schema.name)
    }

    override fun visit(leaderField1: EnumerationFieldModel<Unit>): List<String> {
        return listOf(leaderField1.schema.name)
    }

    override fun visit(leaderField1: AssociationFieldModel<Unit>): List<String> {
        return listOf(leaderField1.schema.name)
    }

    override fun visit(leaderField1: CompositionFieldModel<Unit>): List<String> {
        // Recurse into the composition (but only fields that are not composition-lists).
        val nested = leaderField1.value.fields.filter { it !is CompositionListFieldModel<*> }
            .flatMap { dispatchVisit(it, this) ?: listOf() }
        return nested.map { "${leaderField1.schema.name}/${it}" }
    }

    // List fields

    override fun visit(leaderField1: PrimitiveListFieldModel<Unit>): List<String> {
        return listOf(leaderField1.schema.name)
    }

    override fun visit(leaderField1: AliasListFieldModel<Unit>): List<String> {
        return listOf(leaderField1.schema.name)
    }

    override fun visit(leaderField1: EnumerationListFieldModel<Unit>): List<String> {
        return listOf(leaderField1.schema.name)
    }

    override fun visit(leaderField1: AssociationListFieldModel<Unit>): List<String> {
        return listOf(leaderField1.schema.name)
    }

    override fun visit(leaderField1: CompositionListFieldModel<Unit>): List<String> {
        // Composition list fields are not flattened, and so their field names
        // are not returned.
        return listOf()
    }
}
