package org.treeWare.model.operator

import org.treeWare.metaModel.Granularity
import org.treeWare.metaModel.getGranularityMeta
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.traversal.AbstractLeader1MutableModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.mutableForEach

/**
 * Populate the sub-trees of sub-tree granularity entities that are being fetched.
 * A sub-tree is populated only if the get-request specifies just the root of the sub-tree.
 * If the get-request specifies paths under the sub-tree root, then that sub-tree is not populated.
 */
fun populateSubTreeGranularityGetRequest(getRequest: MutableEntityModel) {
    val visitor = PopulateSubTreeGranularityGetRequestVisitor()
    mutableForEach(getRequest, visitor, false)
}

private class PopulateSubTreeGranularityGetRequestVisitor :
    AbstractLeader1MutableModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    override fun visitMutableEntity(leaderEntity1: MutableEntityModel): TraversalAction {
        val parentField = leaderEntity1.parent ?: return TraversalAction.CONTINUE
        val parentFieldMeta = requireNotNull(parentField.meta)
        val granularityMeta = getGranularityMeta(parentFieldMeta)
        if (granularityMeta != Granularity.SUB_TREE) return TraversalAction.CONTINUE

        populateSubTree(leaderEntity1, true)
        return TraversalAction.ABORT_SUB_TREE
    }
}