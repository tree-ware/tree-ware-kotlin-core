package org.treeWare.metaModel.validation

import org.treeWare.metaModel.FieldType
import org.treeWare.metaModel.Granularity
import org.treeWare.metaModel.getFieldTypeMeta
import org.treeWare.metaModel.getGranularityMeta
import org.treeWare.metaModel.traversal.AbstractLeader1MetaModelVisitor
import org.treeWare.metaModel.traversal.metaModelForEach
import org.treeWare.model.core.EntityModel
import org.treeWare.model.core.getMetaModelResolved
import org.treeWare.model.traversal.TraversalAction

/**
 * Validates granularity.
 * Returns a list of errors. Returns an empty list if there are no errors.
 *
 * Side effects: none
 */
fun validateGranularity(meta: EntityModel): List<String> {
    val visitor = ValidateGranularityVisitor()
    metaModelForEach(meta, visitor)
    return visitor.errors
}

private class ValidateGranularityVisitor : AbstractLeader1MetaModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    val errors = mutableListOf<String>()

    override fun visitFieldMeta(leaderFieldMeta1: EntityModel): TraversalAction {
        val fieldFullName = getMetaModelResolved(leaderFieldMeta1)?.fullName ?: ""
        val granularity = getGranularityMeta(leaderFieldMeta1)
        if (granularity == Granularity.ENTITY) {
            errors.add("$fieldFullName: `entity` granularity is not yet supported")
        } else if (granularity == Granularity.SUB_TREE && getFieldTypeMeta(leaderFieldMeta1) != FieldType.COMPOSITION) {
            // TODO(deepak-nulu): use exists_if for the "granularity" field in the meta-meta-model.
            errors.add("$fieldFullName: `sub_tree` granularity is supported only for composition fields")
        }
        return TraversalAction.CONTINUE
    }
}