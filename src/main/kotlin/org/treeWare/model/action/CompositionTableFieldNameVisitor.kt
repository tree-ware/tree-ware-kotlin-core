package org.treeWare.model.action

import org.treeWare.metaModel.getMetaName
import org.treeWare.metaModel.isCompositionFieldMeta
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.ListFieldModel
import org.treeWare.model.core.SingleFieldModel
import org.treeWare.model.traversal.AbstractLeader1Follower0ModelVisitor
import org.treeWare.model.traversal.dispatchVisit

// IMPLEMENTATION: ./Get.md

class CompositionTableFieldNameVisitor : AbstractLeader1Follower0ModelVisitor<Unit, List<String>>(listOf()) {
    // Fields

    override fun visit(leaderField1: SingleFieldModel<Unit>): List<String> =
        if (isCompositionFieldMeta(leaderField1.meta)) {
            // Recurse into the composition (but only fields that are not composition-lists).
            val entity1 = leaderField1.value as EntityModel<Unit>
            val nested = entity1.fields.values.filter { !isCompositionSetField(it) }
                .flatMap { dispatchVisit(it, this) ?: listOf() }
            nested.map { "${getMetaName(leaderField1.meta)}/${it}" }
        } else listOf(getMetaName(leaderField1.meta))

    override fun visit(leaderField1: ListFieldModel<Unit>): List<String> =
        if (isCompositionFieldMeta(leaderField1.meta)) {
            // Composition list fields are not flattened, and so their field names
            // are not returned.
            listOf()
        } else listOf(getMetaName(leaderField1.meta))
}
