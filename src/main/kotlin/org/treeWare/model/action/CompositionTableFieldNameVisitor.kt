package org.treeWare.model.action

import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.ListFieldModel
import org.treeWare.model.core.SingleFieldModel
import org.treeWare.model.operator.AbstractLeader1Follower0ModelVisitor
import org.treeWare.model.operator.dispatchVisit
import org.treeWare.schema.core.CompositionFieldSchema

// IMPLEMENTATION: ./Get.md

class CompositionTableFieldNameVisitor : AbstractLeader1Follower0ModelVisitor<Unit, List<String>>(listOf()) {
    // Fields

    override fun visit(leaderField1: SingleFieldModel<Unit>): List<String> =
        if (leaderField1.schema is CompositionFieldSchema) {
            // Recurse into the composition (but only fields that are not composition-lists).
            val entity1 = leaderField1.value as EntityModel<Unit>
            val nested = entity1.fields.filter { !isCompositionListField(it) }
                .flatMap { dispatchVisit(it, this) ?: listOf() }
            nested.map { "${leaderField1.schema.name}/${it}" }
        } else listOf(leaderField1.schema.name)

    override fun visit(leaderField1: ListFieldModel<Unit>): List<String> =
        if (leaderField1.schema is CompositionFieldSchema) {
            // Composition list fields are not flattened, and so their field names
            // are not returned.
            listOf()
        } else listOf(leaderField1.schema.name)
}
