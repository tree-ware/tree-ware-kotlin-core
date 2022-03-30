package org.treeWare.model.validation

import org.treeWare.model.core.*
import org.treeWare.model.traversal.AbstractLeader1ModelVisitor
import org.treeWare.model.traversal.TraversalAction

internal class AssociationValidationVisitor : AbstractLeader1ModelVisitor<TraversalAction>(TraversalAction.CONTINUE) {
    var hasMultiplePaths = false
        private set
    var hasNonKeyFields = false
        private set
    var targetEntity: EntityModel? = null
        private set

    private var hasEmptySetFields = false
    private var targetFound = false

    private fun checkForMultiplePaths(): TraversalAction =
        if (!hasEmptySetFields && !targetFound) TraversalAction.CONTINUE
        else {
            hasMultiplePaths = true
            TraversalAction.ABORT_TREE
        }

    override fun visitEntity(leaderEntity1: EntityModel): TraversalAction =
        if (isCompositionKey(leaderEntity1)) TraversalAction.ABORT_SUB_TREE else TraversalAction.CONTINUE

    override fun visitSingleField(leaderField1: SingleFieldModel): TraversalAction {
        if (!isKeyField(leaderField1)) {
            hasNonKeyFields = true
            return TraversalAction.ABORT_TREE
        }
        return checkForMultiplePaths()
    }

    override fun visitListField(leaderField1: ListFieldModel): TraversalAction {
        hasNonKeyFields = true
        return TraversalAction.ABORT_TREE
    }

    override fun visitSetField(leaderField1: SetFieldModel): TraversalAction = checkForMultiplePaths()

    override fun leaveSetField(leaderField1: SetFieldModel) {
        if (!targetFound) hasEmptySetFields = true
    }

    override fun leaveEntity(leaderEntity1: EntityModel) {
        // Record the last entity in the path.
        // The last entity in the path will be the first entity to be left, unless the entity is a composite key,
        // in which case it will be left first. The composite key should not be recorded since it is a key value,
        // not a target entity.
        if (targetEntity == null && !isCompositionKey(leaderEntity1)) {
            targetEntity = leaderEntity1
            targetFound = true
        }
    }
}