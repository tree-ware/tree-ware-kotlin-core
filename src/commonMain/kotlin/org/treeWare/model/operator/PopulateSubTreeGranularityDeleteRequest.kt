package org.treeWare.model.operator

import org.treeWare.metaModel.Granularity
import org.treeWare.metaModel.getGranularityMeta
import org.treeWare.model.core.MutableEntityModel
import org.treeWare.model.core.MutableSetFieldModel
import org.treeWare.model.core.MutableSingleFieldModel
import org.treeWare.model.core.isCompositionKey
import org.treeWare.model.operator.set.aux.SetAux
import org.treeWare.model.operator.set.aux.getSetAux
import org.treeWare.model.operator.set.aux.setSetAux
import org.treeWare.model.traversal.AbstractLeader1MutableModelVisitor
import org.treeWare.model.traversal.TraversalAction
import org.treeWare.model.traversal.mutableForEach

/**
 * Populate the sub-trees of sub-tree granularity entities that are being deleted.
 *
 * @return a list of errors, if any. If there are errors, any sub-trees populated without errors will remain populated.
 */
fun populateSubTreeGranularityDeleteRequest(setRequest: MutableEntityModel): List<ElementModelError> {
    val visitor = PopulateSubTreeGranularityDeleteRequestVisitor()
    mutableForEach(setRequest, visitor, false)
    return visitor.errors
}

private const val SUB_TREE_NOT_EMPTY_ERROR =
    "A delete-request must only specify the root of a sub-tree with sub_tree granularity"

private class PopulateSubTreeGranularityDeleteRequestVisitor :
    AbstractLeader1MutableModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    val errors = mutableListOf<ElementModelError>()

    private val modelPathStack = ModelPathStack()

    override fun visitMutableEntity(leaderEntity1: MutableEntityModel): TraversalAction {
        modelPathStack.pushEntity(leaderEntity1, isCompositionKey(leaderEntity1))
        val parentField = leaderEntity1.parent
        val setAux = getSetAux(parentField) ?: getSetAux(leaderEntity1)
        if (setAux != SetAux.DELETE) return TraversalAction.CONTINUE

        val parentFieldMeta = requireNotNull(parentField?.meta)
        val granularityMeta = getGranularityMeta(parentFieldMeta)
        if (granularityMeta != Granularity.SUB_TREE) return TraversalAction.CONTINUE

        val success = populateSubTree(leaderEntity1, false) { setSetAux(it, SetAux.DELETE) }
        if (!success) errors.add(ElementModelError(modelPathStack.peekModelPath(), SUB_TREE_NOT_EMPTY_ERROR))
        return TraversalAction.ABORT_SUB_TREE
    }

    override fun leaveMutableEntity(leaderEntity1: MutableEntityModel) {
        modelPathStack.popEntity()
    }

    override fun visitMutableSingleField(leaderField1: MutableSingleFieldModel): TraversalAction {
        modelPathStack.pushField(leaderField1)
        return TraversalAction.CONTINUE
    }

    override fun leaveMutableSingleField(leaderField1: MutableSingleFieldModel) {
        modelPathStack.popField()
    }

    override fun visitMutableSetField(leaderField1: MutableSetFieldModel): TraversalAction {
        modelPathStack.pushField(leaderField1)
        return TraversalAction.CONTINUE
    }

    override fun leaveMutableSetField(leaderField1: MutableSetFieldModel) {
        modelPathStack.popField()
    }
}